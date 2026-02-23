package com.selegic.encye.home

import com.selegic.encye.ui.component.CommentsBottomSheet
import com.selegic.encye.ui.component.TextFocusPostCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.home.viewmodel.HomeViewModel
import com.selegic.encye.ui.component.PostCreate

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
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<PostDto?>(null) }

    PullToRefreshBox(
        modifier = Modifier,
        onRefresh = onRefresh,
        isRefreshing = false
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp) // Generous spacing between cards
        ) {
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
                posts[index]?.let { post ->
                    TextFocusPostCard(
                        post = post,
//                        onLikeClick = {},
                        onCommentClick = {
                            selectedPost = post
                            showCommentSheet = !showCommentSheet
                        }
                    )
                }
            }
        }
        if (showCommentSheet && selectedPost != null) {
            CommentsBottomSheet(
                onModel = "Post",
                itemId = selectedPost!!.id,
                showSheet = true,
                onDismiss = { showCommentSheet = false },
                currentUserAvatar = "https://your-user-avatar-url.jpg" // Replace with logged in user data
            )
        }
    }
}
