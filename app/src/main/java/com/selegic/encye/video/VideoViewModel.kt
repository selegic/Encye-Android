package com.selegic.encye.video

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.selegic.encye.data.remote.dto.VideoDto
import com.selegic.encye.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    // Vertical short-video feed: prefetch a couple of items ahead to keep swipes smooth.
    val videos: Flow<PagingData<VideoDto>> = videoRepository
        .getVideoFeed(category = "short_video", pageSize = 8, prefetchDistance = 2)
        .cachedIn(viewModelScope)
}
