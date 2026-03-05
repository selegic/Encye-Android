package com.selegic.encye.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.selegic.encye.data.remote.VideoApiService
import com.selegic.encye.data.remote.dto.VideoDto
import javax.inject.Inject

class VideoPagingSource @Inject constructor(
    private val apiService: VideoApiService,
    private val category: String? = "short_video",
    private val limit: Int = 8
) : PagingSource<Int, VideoDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VideoDto> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getAllVideos(page = page, limit = limit)
            val allVideos = response.data.orEmpty()
            val videos = if (category.isNullOrBlank()) {
                allVideos
            } else {
                allVideos.filter { it.category == category }
            }

            val endOfPaginationReached = allVideos.isEmpty() || allVideos.size < limit

            LoadResult.Page(
                data = videos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (endOfPaginationReached) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, VideoDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
