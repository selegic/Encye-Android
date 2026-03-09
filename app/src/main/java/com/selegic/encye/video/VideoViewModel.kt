package com.selegic.encye.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.selegic.encye.data.remote.dto.VideoDto
import com.selegic.encye.data.repository.LikeRepository
import com.selegic.encye.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoLikeUiState(
    val isLiked: Boolean,
    val likeCount: Int
)

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val likeRepository: LikeRepository
) : ViewModel() {

    // Vertical short-video feed: prefetch a couple of items ahead to keep swipes smooth.
    val videos: Flow<PagingData<VideoDto>> = videoRepository
        .getVideoFeed(category = "short_video", pageSize = 8, prefetchDistance = 2)
        .cachedIn(viewModelScope)

    private val _videoLikeUiState = MutableStateFlow<Map<String, VideoLikeUiState>>(emptyMap())
    val videoLikeUiState: StateFlow<Map<String, VideoLikeUiState>> = _videoLikeUiState.asStateFlow()

    fun toggleVideoLike(video: VideoDto) {
        viewModelScope.launch {
            val videoId = video.mongoId.ifBlank { video.id }
            val current = _videoLikeUiState.value[videoId]
                ?: VideoLikeUiState(isLiked = video.isLiked, likeCount = video.likeCount)
            val optimisticLiked = !current.isLiked
            val optimisticCount = if (optimisticLiked) {
                current.likeCount + 1
            } else {
                (current.likeCount - 1).coerceAtLeast(0)
            }

            _videoLikeUiState.update {
                it + (videoId to VideoLikeUiState(isLiked = optimisticLiked, likeCount = optimisticCount))
            }

            runCatching {
                likeRepository.toggleLike(onModel = "Video", itemId = videoId)
            }.onSuccess { response ->
                val correctedCount = if (response.liked == optimisticLiked) {
                    optimisticCount
                } else if (response.liked) {
                    current.likeCount + 1
                } else {
                    (current.likeCount - 1).coerceAtLeast(0)
                }
                _videoLikeUiState.update {
                    it + (videoId to VideoLikeUiState(isLiked = response.liked, likeCount = correctedCount))
                }
            }.onFailure {
                _videoLikeUiState.update {
                    it + (videoId to current)
                }
            }
        }
    }
}
