package com.selegic.encye.video

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.selegic.encye.data.remote.dto.VideoDto
import com.selegic.encye.ui.component.CommentsBottomSheet

@Composable
fun VideoScreen() {
    val viewModel: VideoViewModel = hiltViewModel()
    val videos = viewModel.videos.collectAsLazyPagingItems()
    val itemCount = videos.itemCount
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var showCommentSheet by remember { mutableStateOf(false) }
    var commentTargetVideoId by remember { mutableStateOf<String?>(null) }
    var playerErrorMessage by remember { mutableStateOf<String?>(null) }
    var loadedVideoId by remember { mutableStateOf<String?>(null) }
    var isPausedByUser by remember { mutableStateOf(false) }
    var shouldResumeOnStart by remember { mutableStateOf(false) }
    val pausedByUserState = rememberUpdatedState(isPausedByUser)
    val hasLoadedVideoState = rememberUpdatedState(loadedVideoId != null)
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        1500, // minBufferMs
                        5000, // maxBufferMs
                        250,  // bufferForPlaybackMs
                        500   // bufferForPlaybackAfterRebufferMs
                    )
                    .build()
            )
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                playWhenReady = false
            }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playerErrorMessage = "Video unavailable. Swipe to continue."
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    shouldResumeOnStart = exoPlayer.isPlaying && !pausedByUserState.value
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                }

                Lifecycle.Event.ON_START -> {
                    if (shouldResumeOnStart && !pausedByUserState.value && hasLoadedVideoState.value) {
                        exoPlayer.playWhenReady = true
                        exoPlayer.play()
                    }
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when {
        videos.loadState.refresh is LoadState.Loading && itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        videos.loadState.refresh is LoadState.Error && itemCount == 0 -> {
            val error = (videos.loadState.refresh as LoadState.Error).error
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Could not load videos", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = error.localizedMessage ?: "Unknown error",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap to retry",
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { videos.retry() }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        itemCount == 0 -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No videos available")
            }
        }

        else -> {
            val pagerState = rememberPagerState(pageCount = { videos.itemCount })
            val activeVideo = videos[pagerState.currentPage]

            LaunchedEffect(activeVideo?.id, activeVideo?.url) {
                val url = activeVideo?.url?.trim().orEmpty()
                if (url.isBlank()) {
                    exoPlayer.stop()
                    loadedVideoId = null
                    playerErrorMessage = "Video unavailable. Swipe to continue."
                    isPausedByUser = false
                    return@LaunchedEffect
                }
                if (loadedVideoId != activeVideo?.id) {
                    loadedVideoId = activeVideo?.id
                    playerErrorMessage = null
                    isPausedByUser = false
                    exoPlayer.setMediaItem(MediaItem.fromUri(url))
                    exoPlayer.prepare()
                }
                if (isPausedByUser) {
                    exoPlayer.playWhenReady = false
                    exoPlayer.pause()
                } else {
                    exoPlayer.playWhenReady = true
                    exoPlayer.play()
                }
            }

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { index -> videos[index]?.id ?: "video-$index" },
                beyondViewportPageCount = 0
            ) { page ->
                val video = videos[page]
                if (video == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    VideoPage(
                        video = video,
                        isActive = pagerState.currentPage == page,
                        exoPlayer = exoPlayer,
                        playerErrorMessage = playerErrorMessage,
                        isPaused = isPausedByUser,
                        onTogglePause = {
                            if (pagerState.currentPage == page) {
                                isPausedByUser = !isPausedByUser
                                if (isPausedByUser) {
                                    exoPlayer.playWhenReady = false
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.playWhenReady = true
                                    exoPlayer.play()
                                }
                            }
                        },
                        onCommentClick = {
                            commentTargetVideoId = video.mongoId.ifBlank { video.id }
                            showCommentSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showCommentSheet && !commentTargetVideoId.isNullOrBlank()) {
        CommentsBottomSheet(
            onModel = "Video",
            itemId = commentTargetVideoId!!,
            showSheet = true,
            onDismiss = { showCommentSheet = false },
            currentUserAvatar = null
        )
    }
}

@Composable
private fun VideoPage(
    video: VideoDto,
    isActive: Boolean,
    exoPlayer: ExoPlayer,
    playerErrorMessage: String?,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onCommentClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ShortVideoPlayer(
            isActive = isActive,
            exoPlayer = exoPlayer,
            onTogglePause = onTogglePause,
            modifier = Modifier.fillMaxSize()
        )

        if (isActive && isPaused) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Paused",
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isActive && !playerErrorMessage.isNullOrBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                color = Color.Black.copy(alpha = 0.65f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = playerErrorMessage,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 18.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "For You",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = video.title?.ifBlank { "Untitled video" } ?: "Untitled video",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = video.description?.ifBlank { "No description available." } ?: "No description available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${video.uploadedBy?.take(10) ?: "encye"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }

            Column(
                modifier = Modifier.width(72.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                VideoStatAction(
                    icon = if (video.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = formatVideoCount(video.likeCount),
                    tint = if (video.isLiked) Color(0xFFFF5252) else Color.White
                )
                VideoStatAction(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = formatVideoCount(video.commentCount),
                    tint = Color.White,
                    onClick = onCommentClick
                )
                VideoStatAction(
                    icon = Icons.Outlined.Share,
                    label = formatVideoCount(video.shareCount),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ShortVideoPlayer(
    isActive: Boolean,
    exoPlayer: ExoPlayer,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isActive) {
        Box(
            modifier = modifier.background(Color.Black),
            contentAlignment = Alignment.Center
        ) { }
        return
    }

    LaunchedEffect(isActive, exoPlayer) {
        exoPlayer.playWhenReady = isActive
        if (isActive) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            onTogglePause()
        },
        factory = { viewContext ->
            PlayerView(viewContext).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        update = { playerView ->
            playerView.player = if (isActive) exoPlayer else null
        }
    )
}

@Composable
private fun VideoStatAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
    }
}

private fun formatVideoCount(value: Int): String {
    return when {
        value >= 1_000_000 -> "${"%.1f".format(value / 1_000_000f)}M"
        value >= 1_000 -> "${"%.1f".format(value / 1_000f)}K"
        else -> value.toString()
    }
}
