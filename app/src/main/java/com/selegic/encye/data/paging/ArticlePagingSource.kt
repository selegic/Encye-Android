package com.selegic.encye.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.selegic.encye.data.remote.ArticleApiService
import com.selegic.encye.data.remote.dto.ArticleDto
import javax.inject.Inject

class ArticlePagingSource @Inject constructor(
    private val apiService: ArticleApiService
) : PagingSource<Int, ArticleDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArticleDto> {
        val page = params.key ?: 1
        return try {
            val response = apiService.getAllArticles(
                page = page,
                limit = params.loadSize
            )
            val articles = response.data ?: emptyList()
            LoadResult.Page(
                data = articles,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (articles.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArticleDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
