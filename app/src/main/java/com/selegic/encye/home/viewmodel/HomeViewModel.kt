package com.selegic.encye.home.viewmodel

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostLikeUiState(
    val isLiked: Boolean,
    val likeCount: Int
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postApiService: PostApiService,
    private val likeRepository: LikeRepository
) : ViewModel() {

    val posts: Flow<PagingData<PostDto>> = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { PostPagingSource(postApiService) }
    ).flow.cachedIn(viewModelScope)

    private val _postLikeUiState = MutableStateFlow<Map<String, PostLikeUiState>>(emptyMap())
    val postLikeUiState: StateFlow<Map<String, PostLikeUiState>> = _postLikeUiState.asStateFlow()

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
}
