package com.selegic.encye.article

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.ArticleDto
import com.selegic.encye.ui.component.CommentsBottomSheet

private val HashtagSanitizeRegex = Regex("[^A-Za-z0-9]+")

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    articleDto: ArticleDto,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val articleViewModel: ArticleViewModel = hiltViewModel()
    val article by articleViewModel.article.collectAsState()
    val resolvedArticleId = articleId.ifBlank { articleDto.id }
    val commentTargetId = article?._id?.ifBlank { null }
        ?: articleDto._id.ifBlank { null }
        ?: resolvedArticleId
    var showCommentSheet by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val showActionBar by remember {
        derivedStateOf { !scrollState.isScrollInProgress }
    }

    LaunchedEffect(articleDto) {
        articleViewModel.setArticle(articleDto)
    }

    LaunchedEffect(resolvedArticleId) {
        if (resolvedArticleId.isNotBlank()) {
            articleViewModel.fetchArticleById(resolvedArticleId)
        }
    }

    val isTransitionRunning = animatedContentScope.transition.isRunning

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(
                visible = showActionBar,
                enter = slideInVertically { it / 2 } + fadeIn(),
                exit = slideOutVertically { it / 2 } + fadeOut()
            ) {
                ArticleActionBar(
                    article = article ?: articleDto,
                    onCommentClick = {
                        if (commentTargetId.isNotBlank()) {
                            showCommentSheet = true
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .clipToBounds()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            article?.let {
                with(sharedTransitionScope) {
                    AsyncImage(
                        model = it.image?.url,
                        contentDescription = it.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.6f)
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "image-${it.id}"),
                                animatedContentScope
                            )
                    )
                    Column(Modifier.padding(16.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ArticleMetaSection(article = it)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                            modifier = Modifier
                                .sharedElement(
                                    sharedTransitionScope.rememberSharedContentState(
                                        key = "title-${it.id}"
                                    ), animatedVisibilityScope = animatedContentScope
                                )
                        )
                        ArticleHashtagRow(hashtags = buildArticleHashtags(it))
                        Spacer(modifier = Modifier.height(8.dp))
                        ArticleBody(
                            html = it.description,
                            showWebView = !isTransitionRunning,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }

        if (showCommentSheet && commentTargetId.isNotBlank()) {
            CommentsBottomSheet(
                onModel = "Article",
                itemId = commentTargetId,
                showSheet = true,
                onDismiss = { showCommentSheet = false },
                currentUserAvatar = null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ArticleActionBar(
    article: ArticleDto,
    onCommentClick: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth(),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
            shadowElevation = 10.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            ButtonGroup(
                overflowIndicator = { menuState ->
                    ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                },
                modifier = Modifier
                    .padding(10.dp)
            ) {
                clickableItem(
                    onClick = { },
                    label = "",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark"
                        )
                    }
                )
//
//                clickableItem(
//                    onClick = onCommentClick,
//                    label = "",
//                    icon = {
//                        Icon(
//                            imageVector = Icons.Outlined.ChatBubbleOutline,
//                            contentDescription = "Comment"
//                        )
//                    }
//                )
//
//                clickableItem(
//                    onClick = {
//                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                            type = "text/plain"
//                            putExtra(
//                                Intent.EXTRA_TEXT,
//                                buildString {
//                                    append(article.title)
//                                    if (article.id.isNotBlank()) {
//                                        append("\n\n")
//                                        append(article.id)
//                                    }
//                                }
//                            )
//                        }
//                        context.startActivity(Intent.createChooser(shareIntent, "Share article"))
//                    },
//                    label = "",
//                    icon = {
//                        Icon(
//                            imageVector = Icons.Outlined.Share,
//                            contentDescription = "Share"
//                        )
//                    }
//                )
            }
        }
    }
}

@Composable
private fun ArticleMetaSection(article: ArticleDto) {
    val authorName = listOfNotNull(article.createdBy?.firstName, article.createdBy?.lastName)
        .joinToString(" ")
        .ifBlank { "Encye Desk" }
    val categoryName = article.autoCategory?.primary?.name

    Column {
        if (!categoryName.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = categoryName.uppercase(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row {
            AsyncImage(
                model = article.createdBy?.profilePicture,
                contentDescription = authorName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatArticleDate(article.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArticleHashtagRow(hashtags: List<String>) {
    if (hashtags.isEmpty()) {
        return
    }

    Spacer(modifier = Modifier.height(14.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        hashtags.forEach { hashtag ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ) {
                Text(
                    text = hashtag,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ArticleBody(
    html: String,
    showWebView: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clipToBounds()) {
        if (showWebView) {
            HtmlWebView(
                html = html,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(Modifier.height(1.dp))
        }
    }
}

@Composable
fun HtmlWebView(html: String, modifier: Modifier = Modifier) {
    val wrappedHtml = remember(html) {
        """
        <!DOCTYPE html>
        <html>
        <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            body {
                margin: 0;
                padding: 0;
                word-wrap: break-word;
                overflow-wrap: break-word;
                white-space: pre-wrap;
                background-color: transparent;
                font-size: 14px;
                font-family: sans-serif;
                color: #666666;
            }
            img, table {
                max-width: 100%;
                height: auto;
            }
            ul {
                padding-left: 20px; 
                margin-left: 0;
            }
        </style>
        </head>
        <body>
        $html
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier.clipToBounds(),
        factory = { context ->
            WebView(context).apply {
                isVerticalScrollBarEnabled = false
                setBackgroundColor(0x00000000) // Transparent
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, wrappedHtml, "text/html", "UTF-8", null)
        }
    )
}

private fun buildArticleHashtags(article: ArticleDto): List<String> {
    val rawTags = if (article.tags.isNotEmpty()) {
        article.tags
    } else {
        listOfNotNull(article.autoCategory?.primary?.name)
    }

    return rawTags.mapNotNull { rawTag ->
        rawTag.trim()
            .removePrefix("#")
            .replace(HashtagSanitizeRegex, "")
            .takeIf { it.isNotBlank() }
            ?.let { "#$it" }
    }.distinct()
}

private fun formatArticleDate(raw: String): String {
    val cleaned = raw.substringBefore('T').substringBefore(' ')
    return cleaned.ifBlank { raw }.take(16)
}
