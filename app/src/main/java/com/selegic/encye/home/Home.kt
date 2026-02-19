package com.selegic.encye.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.home.viewmodel.HomeViewModel
import com.selegic.encye.ui.component.PostCreate
import com.selegic.encye.ui.component.PostItem

@Composable
fun Home() {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val posts = homeViewModel.posts.collectAsLazyPagingItems()

    HomeScreen(
        posts = posts,
        onRefresh = { posts.refresh() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: LazyPagingItems<PostDto>,
    onRefresh: () -> Unit,
) {
    PullToRefreshBox(
        modifier = Modifier,
        onRefresh = onRefresh,
        isRefreshing = false
    ) {
        LazyColumn {
            item {
                PostCreate(
                    onFeelingClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    onPostClick = {},
                    onPhotoClick = {},
                    onVideoClick = {}
                )
            }
            items(posts.itemCount) { index ->
                posts[index]?.let {
                    PostItem(
                        post = it,
                        onLikeClick = {},
                        onCommentClick = {}
                    )
                }
            }
        }
    }
}
