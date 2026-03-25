package com.selegic.encye.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.PostDto
import com.selegic.encye.home.viewmodel.HomeComposerUiState
import com.selegic.encye.home.viewmodel.HomeViewModel
import com.selegic.encye.home.viewmodel.PostLikeUiState
import com.selegic.encye.home.viewmodel.SelectedComposerImage
import com.selegic.encye.ui.component.CommentsBottomSheet
import com.selegic.encye.ui.component.PostCreate
import com.selegic.encye.ui.component.TextFocusPostCard
import com.selegic.encye.util.copyUriToCacheFile

@Composable
fun Home(
    onProfileClick: (String?) -> Unit,
    onLogoutClick: () -> Unit
) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val posts = homeViewModel.posts.collectAsLazyPagingItems()
    val postLikeUiState by homeViewModel.postLikeUiState.collectAsState()
    val composerUiState by homeViewModel.composerUiState.collectAsState()

    HomeScreen(
        posts = posts,
        postLikeUiState = postLikeUiState,
        composerUiState = composerUiState,
        onRefresh = { posts.refresh() },
        onTogglePostLike = homeViewModel::togglePostLike,
        onDraftContentChange = homeViewModel::updateDraftContent,
        onComposerImagesChange = homeViewModel::setComposerImages,
        onRemoveComposerImage = homeViewModel::removeComposerImage,
        onSubmitPost = homeViewModel::submitPost,
        onEditPost = homeViewModel::startEditingPost,
        onDeletePost = homeViewModel::deletePost,
        onResetComposer = homeViewModel::resetComposer,
        onClearComposerFeedback = homeViewModel::clearComposerFeedback,
        onLogoutClick = onLogoutClick,
        onProfileClick = { onProfileClick(null) },
        onPostAuthorClick = onProfileClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    posts: LazyPagingItems<PostDto>,
    postLikeUiState: Map<String, PostLikeUiState>,
    composerUiState: HomeComposerUiState,
    onRefresh: () -> Unit,
    onTogglePostLike: (PostDto) -> Unit,
    onDraftContentChange: (String) -> Unit,
    onComposerImagesChange: (List<SelectedComposerImage>) -> Unit,
    onRemoveComposerImage: (Int) -> Unit,
    onSubmitPost: () -> Unit,
    onEditPost: (PostDto) -> Unit,
    onDeletePost: (String) -> Unit,
    onResetComposer: () -> Unit,
    onClearComposerFeedback: () -> Unit,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPostAuthorClick: (String) -> Unit,
) {
    var showCommentSheet by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<PostDto?>(null) }
    var showComposerSheet by remember { mutableStateOf(false) }
    var postPendingDelete by remember { mutableStateOf<PostDto?>(null) }
    var showTopBarMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val selectedUris = remember { mutableStateListOf<Uri>() }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris.clear()
            selectedUris.addAll(uris)
            onComposerImagesChange(
                uris.mapNotNull { uri ->
                    copyUriToCacheFile(context = context, uri = uri, prefix = "post")
                        ?.let { file ->
                            SelectedComposerImage(
                                file = file,
                                displayName = uri.lastPathSegment ?: file.name
                            )
                        }
                }
            )
            showComposerSheet = true
        }
    }

    LaunchedEffect(composerUiState.successMessage) {
        composerUiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            showComposerSheet = false
            selectedUris.clear()
            onResetComposer()
            onClearComposerFeedback()
            posts.refresh()
        }
    }

    LaunchedEffect(composerUiState.errorMessage) {
        composerUiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onClearComposerFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = { showTopBarMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                    DropdownMenu(
                        expanded = showTopBarMenu,
                        onDismissRequest = { showTopBarMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log out") },
                            onClick = {
                                showTopBarMenu = false
                                onLogoutClick()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            modifier = Modifier.padding(paddingValues),
            onRefresh = onRefresh,
            isRefreshing = posts.loadState.refresh is LoadState.Loading
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    PostCreate(
                        modifier = Modifier.fillMaxWidth(),
                        userDisplayName = composerUiState.currentUserName,
                        userAvatarUrl = composerUiState.currentUserAvatarUrl,
                        attachmentCount = composerUiState.selectedImages.size,
                        isSubmitting = composerUiState.isSubmitting,
                        onPostClick = {
                            onResetComposer()
                            selectedUris.clear()
                            showComposerSheet = true
                        },
                        onPhotoClick = { imagePicker.launch("image/*") }
                    )
                }
                items(posts.itemCount) { index ->
                    posts[index]?.let { post ->
                        TextFocusPostCard(
                            post = post,
                            isLiked = postLikeUiState[post.id]?.isLiked ?: post.isLiked,
                            likeCount = postLikeUiState[post.id]?.likeCount ?: post.likeCount,
                            onLikeClick = { onTogglePostLike(post) },
                            onCommentClick = {
                                selectedPost = post
                                showCommentSheet = true
                            },
                            onProfileClick = { onPostAuthorClick(post.createdBy.id) },
                            canManagePost = composerUiState.currentUserId == post.createdBy.id,
                            onEditClick = {
                                onEditPost(post)
                                selectedUris.clear()
                                showComposerSheet = true
                            },
                            onDeleteClick = {
                                postPendingDelete = post
                            }
                        )
                    }
                }
            }
            if (postPendingDelete != null) {
                AlertDialog(
                    onDismissRequest = { postPendingDelete = null },
                    title = { Text("Delete post") },
                    text = { Text("This will permanently remove the post from the feed.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                onDeletePost(postPendingDelete!!.id)
                                postPendingDelete = null
                            }
                        ) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { postPendingDelete = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            if (showCommentSheet && selectedPost != null) {
                CommentsBottomSheet(
                    onModel = "Post",
                    itemId = selectedPost!!.id,
                    showSheet = true,
                    onDismiss = { showCommentSheet = false },
                    currentUserAvatar = composerUiState.currentUserAvatarUrl
                )
            }
            if (showComposerSheet) {
                CreatePostBottomSheet(
                    uiState = composerUiState,
                    selectedUris = selectedUris,
                    onDismiss = {
                        if (!composerUiState.isSubmitting) {
                            onResetComposer()
                            selectedUris.clear()
                            showComposerSheet = false
                        }
                    },
                    onContentChange = onDraftContentChange,
                    onPickImages = { imagePicker.launch("image/*") },
                    onRemoveImage = { index ->
                        selectedUris.removeAt(index)
                        onRemoveComposerImage(index)
                    },
                    onSubmit = onSubmitPost
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostBottomSheet(
    uiState: HomeComposerUiState,
    selectedUris: List<Uri>,
    onDismiss: () -> Unit,
    onContentChange: (String) -> Unit,
    onPickImages: () -> Unit,
    onRemoveImage: (Int) -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.navigationBarsPadding(),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (!uiState.currentUserAvatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = uiState.currentUserAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = uiState.currentUserName.firstOrNull()?.uppercase() ?: "Y",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.editingPostId != null) "Edit post" else "Create post",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = uiState.currentUserName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ) {
                OutlinedTextField(
                    value = uiState.draftContent,
                    onValueChange = onContentChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = {
                        Text("What do you want your network to see?")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
            }

            if (selectedUris.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Attached photos",
                        style = MaterialTheme.typography.titleSmall
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(selectedUris) { index, uri ->
                            Box(
                                modifier = Modifier
                                    .width(156.dp)
                                    .aspectRatio(1f)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { onRemoveImage(index) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove image"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = selectedUris.isNotEmpty(),
                    onClick = onPickImages,
                    label = { Text(if (selectedUris.isEmpty()) "Add photos" else "Add more photos") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null
                        )
                    }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = {
                        Text(
                            if (uiState.editingPostId != null) {
                                "Updating as ${uiState.currentUserName}"
                            } else {
                                "Posting as ${uiState.currentUserName}"
                            }
                        )
                    }
                )
            }

            uiState.errorMessage?.let { message ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (uiState.isSubmitting) {
                HorizontalDivider()
            }

            Button(
                onClick = onSubmit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (uiState.isSubmitting) {
                        if (uiState.editingPostId != null) "Saving..." else "Posting..."
                    } else {
                        if (uiState.editingPostId != null) "Save changes" else "Post to feed"
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
