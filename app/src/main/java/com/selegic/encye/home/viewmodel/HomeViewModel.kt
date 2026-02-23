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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postApiService: PostApiService
) : ViewModel() {

    val posts: Flow<PagingData<PostDto>> = Pager(
        config = PagingConfig(pageSize = 10),
        pagingSourceFactory = { PostPagingSource(postApiService) }
    ).flow.cachedIn(viewModelScope)

}
