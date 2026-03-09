package com.selegic.encye.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.CommentDto
import com.selegic.encye.data.repository.CommentRepository
import com.selegic.encye.data.repository.LikeRepository
import com.selegic.encye.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommentLikeUiState(
    val isLiked: Boolean,
    val likeCount: Int
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments.asStateFlow()

    private val _commentCount = MutableStateFlow(0)
    val commentCount: StateFlow<Int> = _commentCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _submissionVersion = MutableStateFlow(0)
    val submissionVersion: StateFlow<Int> = _submissionVersion.asStateFlow()

    private val _commentLikeUiState = MutableStateFlow<Map<String, CommentLikeUiState>>(emptyMap())
    val commentLikeUiState: StateFlow<Map<String, CommentLikeUiState>> = _commentLikeUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _isSignedIn.value = !sessionManager.getAuthToken().isNullOrBlank()
        }
    }

    fun fetchComments(
        onModel: String,
        itemId: String,
        clearExisting: Boolean = true,
        showLoading: Boolean = true
    ) {
        viewModelScope.launch {
            if (showLoading) {
                _isLoading.value = true
            }
            if (clearExisting) {
                _comments.value = emptyList()
                _commentCount.value = 0
                _commentLikeUiState.value = emptyMap()
            }
            try {
                val response = commentRepository.getComments(onModel, itemId, 1, 50)
                _comments.value = response.comments
                _commentCount.value = response.totalComments
                _commentLikeUiState.value = response.comments.associate { comment ->
                    comment.id to CommentLikeUiState(
                        isLiked = comment.isLiked,
                        likeCount = comment.likeCount
                    )
                }
            } catch (e: Exception) {
                // Ignore for now
            } finally {
                if (showLoading) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun addComment(onModel: String, itemId: String, text: String) {
        if (!_isSignedIn.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val response = commentRepository.createComment(onModel, itemId, text)
                if (response.success) {
                    response.data?.let { newComment ->
                        _comments.value = listOf(newComment) + _comments.value
                        _commentCount.value += 1
                        _commentLikeUiState.update {
                            it + (newComment.id to CommentLikeUiState(
                                isLiked = newComment.isLiked,
                                likeCount = newComment.likeCount
                            ))
                        }
                    }
                    _submissionVersion.value += 1
                }
            } catch (e: Exception) {
                // Ignore for now
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun toggleCommentLike(comment: CommentDto) {
        if (!_isSignedIn.value) return

        viewModelScope.launch {
            val commentId = comment.id
            val current = _commentLikeUiState.value[commentId]
                ?: CommentLikeUiState(isLiked = comment.isLiked, likeCount = comment.likeCount)
            val optimisticLiked = !current.isLiked
            val optimisticCount = if (optimisticLiked) {
                current.likeCount + 1
            } else {
                (current.likeCount - 1).coerceAtLeast(0)
            }

            _commentLikeUiState.update {
                it + (commentId to CommentLikeUiState(isLiked = optimisticLiked, likeCount = optimisticCount))
            }

            runCatching {
                likeRepository.toggleLike(onModel = "Comment", itemId = commentId)
            }.onSuccess { response ->
                val correctedCount = if (response.liked == optimisticLiked) {
                    optimisticCount
                } else if (response.liked) {
                    current.likeCount + 1
                } else {
                    (current.likeCount - 1).coerceAtLeast(0)
                }

                _commentLikeUiState.update {
                    it + (commentId to CommentLikeUiState(isLiked = response.liked, likeCount = correctedCount))
                }
            }.onFailure {
                _commentLikeUiState.update {
                    it + (commentId to current)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    onModel: String,
    itemId: String,
    showSheet: Boolean,
    onDismiss: () -> Unit,
    currentUserAvatar: String?, // To show in the input box
    viewModel: CommentViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false // Allows a half-open state, expanding to full when dragged
    )

    val comments by viewModel.comments.collectAsState()
    val commentCount by viewModel.commentCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val isSignedIn by viewModel.isSignedIn.collectAsState()
    val submissionVersion by viewModel.submissionVersion.collectAsState()
    val commentLikeUiState by viewModel.commentLikeUiState.collectAsState()

    LaunchedEffect(itemId, showSheet) {
        if (showSheet) {
            viewModel.fetchComments(onModel, itemId)
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            // Allow full-screen height in expanded state.
            modifier = Modifier.fillMaxHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header
                CommentsHeader(commentCount = if (isLoading) 0 else commentCount, onDismiss = onDismiss)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

                // 2. Comments List (Scrollable)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Takes up all remaining space above the input field
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    if (isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (comments.isEmpty()) {
                        item {
                            EmptyCommentsState()
                        }
                    } else {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                isLiked = commentLikeUiState[comment.id]?.isLiked ?: comment.isLiked,
                                likeCount = commentLikeUiState[comment.id]?.likeCount ?: comment.likeCount,
                                onLikeClick = { viewModel.toggleCommentLike(comment) }
                            )
                        }
                    }
                }

                // 3. Sticky Bottom Input Area
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                if (isSignedIn) {
                    CommentInputField(
                        currentUserAvatar = currentUserAvatar,
                        isSubmitting = isSubmitting,
                        clearSignal = submissionVersion,
                        applyImePadding = false,
                        onSend = { text ->
                            viewModel.addComment(onModel, itemId, text)
                        }
                    )
                } else {
                    SignInToCommentPrompt()
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CommentsHeader(commentCount: Int, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Comments ${if (commentCount > 0) "($commentCount)" else ""}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CommentItem(
    comment: CommentDto,
    isLiked: Boolean = false,
    likeCount: Int = comment.likeCount,
    onLikeClick: () -> Unit = {}
) {
    val authorName = "${comment.createdBy.firstName} ${comment.createdBy.lastName}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Avatar
        AsyncImage(
            model = comment.createdBy.profilePicture,
            contentDescription = "Author Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Comment Content
        Column(modifier = Modifier.weight(1f)) {
            // Bubble / Container for the text
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(
                    topStart = 4.dp, // Sharp corner near the avatar
                    topEnd = 16.dp,
                    bottomEnd = 16.dp,
                    bottomStart = 16.dp
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = comment.content,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Sub-actions (Time, Reply)
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2h", // In a real app, parse comment.createdAt here
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Reply",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { /* Handle Reply */ }
                )
            }
        }

        // Like Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            IconButton(
                onClick = onLikeClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like Comment",
                    tint = if (isLiked) Color(0xFFFF5252) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (likeCount > 0) {
                Text(
                    text = likeCount.toString(),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CommentInputField(
    currentUserAvatar: String?,
    isSubmitting: Boolean = false,
    clearSignal: Int = 0,
    applyImePadding: Boolean = true,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val trimmedText = text.trim()

    LaunchedEffect(clearSignal) {
        if (clearSignal > 0) {
            text = ""
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        // Using WindowInsets to ensure the field sits above the keyboard smoothly
        modifier = modifier
            .fillMaxWidth()
            .then(if (applyImePadding) Modifier.imePadding() else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = currentUserAvatar,
                contentDescription = "Your Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Add a comment...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 100.dp), // Grows slightly if multi-line
                enabled = !isSubmitting,
                shape = RoundedCornerShape(20.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                maxLines = 4
            )

            when {
                isSubmitting -> {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                trimmedText.isNotBlank() -> {
                IconButton(onClick = {
                    onSend(trimmedText)
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            }
        }
    }
}

@Composable
fun EmptyCommentsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No comments yet",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Be the first to share your thoughts.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SignInToCommentPrompt() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign in to add a comment",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "You can still read the discussion without an account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
