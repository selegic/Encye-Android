package com.selegic.encye.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.selegic.encye.data.PostPagingSource
import com.selegic.encye.data.remote.PostApiService
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.data.repository.LikeRepository
import com.selegic.encye.data.repository.PostRepository
import com.selegic.encye.data.repository.UserRepository
import com.selegic.encye.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

data class PostLikeUiState(
    val isLiked: Boolean,
    val likeCount: Int
)

data class SelectedComposerImage(
    val file: File,
    val displayName: String
)

data class HomeComposerUiState(
    val currentUserName: String = "Share with your network",
    val currentUserAvatarUrl: String? = null,
    val draftContent: String = "",
    val selectedImages: List<SelectedComposerImage> = emptyList(),
    val isLoadingProfile: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canSubmit: Boolean
        get() = draftContent.isNotBlank() || selectedImages.isNotEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postApiService: PostApiService,
    private val postRepository: PostRepository,
    private val likeRepository: LikeRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val posts: Flow<PagingData<PostDto>> = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { PostPagingSource(postApiService) }
    ).flow.cachedIn(viewModelScope)

    private val _postLikeUiState = MutableStateFlow<Map<String, PostLikeUiState>>(emptyMap())
    val postLikeUiState: StateFlow<Map<String, PostLikeUiState>> = _postLikeUiState.asStateFlow()

    private val _composerUiState = MutableStateFlow(HomeComposerUiState())
    val composerUiState: StateFlow<HomeComposerUiState> = _composerUiState.asStateFlow()

    init {
        loadCurrentUserProfile()
    }

    fun updateDraftContent(value: String) {
        _composerUiState.update {
            it.copy(
                draftContent = value,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun setComposerImages(images: List<SelectedComposerImage>) {
        _composerUiState.update {
            it.copy(
                selectedImages = images,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun removeComposerImage(index: Int) {
        _composerUiState.update { state ->
            state.copy(
                selectedImages = state.selectedImages.filterIndexed { currentIndex, _ ->
                    currentIndex != index
                }
            )
        }
    }

    fun clearComposerFeedback() {
        _composerUiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun submitPost() {
        val state = _composerUiState.value
        if (state.isSubmitting) {
            Log.d(TAG, "submitPost ignored because a request is already in progress")
            return
        }

        if (!state.canSubmit) {
            Log.d(TAG, "submitPost blocked by validation: empty content and no images")
            _composerUiState.update {
                it.copy(errorMessage = "Write something or add at least one photo before posting")
            }
            return
        }

        Log.d(
            TAG,
            "submitPost starting: contentLength=${state.draftContent.length}, imageCount=${state.selectedImages.size}"
        )

        viewModelScope.launch {
            _composerUiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            runCatching {
                postRepository.createPost(
                    content = state.draftContent.trim().ifBlank { null },
                    images = state.selectedImages.map { image -> image.file }
                )
            }.onSuccess { response ->
                Log.d(TAG, "submitPost response: success=${response.success}, message=${response.msg}")
                _composerUiState.update {
                    if (response.success) {
                        it.copy(
                            draftContent = "",
                            selectedImages = emptyList(),
                            isSubmitting = false,
                            successMessage = response.msg.ifBlank { "Post created" }
                        )
                    } else {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = response.msg.ifBlank { "Unable to create post" }
                        )
                    }
                }
            }.onFailure { error ->
                Log.e(TAG, "submitPost failed", error)
                _composerUiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Unable to create post"
                    )
                }
            }
        }
    }

    fun togglePostLike(post: PostDto) {
        viewModelScope.launch {
            val postId = post.id
            val current = _postLikeUiState.value[postId]
                ?: PostLikeUiState(isLiked = post.isLiked, likeCount = post.likeCount)
            val optimisticLiked = !current.isLiked
            val optimisticCount = if (optimisticLiked) {
                current.likeCount + 1
            } else {
                (current.likeCount - 1).coerceAtLeast(0)
            }

            _postLikeUiState.update {
                it + (postId to PostLikeUiState(isLiked = optimisticLiked, likeCount = optimisticCount))
            }

            runCatching {
                likeRepository.toggleLike(onModel = "Post", itemId = postId)
            }.onSuccess { response ->
                val correctedCount = if (response.liked == optimisticLiked) {
                    optimisticCount
                } else if (response.liked) {
                    current.likeCount + 1
                } else {
                    (current.likeCount - 1).coerceAtLeast(0)
                }
                _postLikeUiState.update {
                    it + (postId to PostLikeUiState(isLiked = response.liked, likeCount = correctedCount))
                }
            }.onFailure {
                _postLikeUiState.update {
                    it + (postId to current)
                }
            }
        }
    }

    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            val cachedProfile = sessionManager.getCachedUserProfile()
            _composerUiState.update {
                it.copy(
                    currentUserName = cachedProfile.displayName,
                    currentUserAvatarUrl = cachedProfile.profilePicture,
                    isLoadingProfile = true
                )
            }

            val userId = sessionManager.getCurrentUserId()
            if (userId.isNullOrBlank()) {
                _composerUiState.update { it.copy(isLoadingProfile = false) }
                return@launch
            }

            runCatching {
                userRepository.getProfileById(userId).data.userDetails
            }.onSuccess { user ->
                val displayName = listOfNotNull(user.firstName, user.lastName)
                    .joinToString(" ")
                    .ifBlank { user.email ?: "You" }

                sessionManager.saveCachedUserProfile(
                    firstName = user.firstName,
                    lastName = user.lastName,
                    profilePicture = user.profilePicture
                )

                _composerUiState.update {
                    it.copy(
                        isLoadingProfile = false,
                        currentUserName = displayName,
                        currentUserAvatarUrl = user.profilePicture
                    )
                }
            }.onFailure {
                _composerUiState.update { state -> state.copy(isLoadingProfile = false) }
            }
        }
    }
}
