package com.selegic.encye.article

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.ArticleDto

private const val TAG = "ArticleDetail"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ArticleDetailScreen(
    articleId: String,
    articleDto: ArticleDto,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val viewModel: ArticleViewModel = hiltViewModel()
    val article = viewModel.article.collectAsState()

    LaunchedEffect(articleDto.id) {
//        Log.d(TAG, "ArticleDetailScreen: fetching article... $articleId")
        viewModel.setArticle(articleDto)
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        article.value?.let {
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
                    Spacer(modifier = Modifier.height(8.dp))
                    HtmlWebView(
                        html = it.description
                    )
                }
            }
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
        </style>
        </head>
        <body>
        $html
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
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
