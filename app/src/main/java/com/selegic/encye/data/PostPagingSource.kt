package com.selegic.encye.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.selegic.encye.data.remote.PostApiService
import com.selegic.encye.data.remote.dto.PostDto
import javax.inject.Inject

class PostPagingSource @Inject constructor(
    private val apiService: PostApiService
) : PagingSource<Int, PostDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostDto> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getPosts(
                page = page,
                limit = params.loadSize
            )
            val posts = response.data ?: emptyList()
            LoadResult.Page(
                data = posts,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (posts.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PostDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
