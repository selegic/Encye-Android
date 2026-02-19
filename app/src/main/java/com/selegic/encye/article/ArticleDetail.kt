package com.selegic.encye.article

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.hilt.navigation.compose.hiltViewModel
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.ArticleDto
import io.github.malikshairali.nativehtml.RenderHtml

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

    LazyColumn(modifier = Modifier) {
        item {
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
                    }
                    HtmlWebView(
                        html= it.description,
                        modifier = Modifier.height(400.dp)
                    )
                }
            }
        }
    }

}

@Composable
fun HtmlWebView(html: String, modifier: Modifier = Modifier) {
//    AndroidView(
//        modifier = modifier.fillMaxSize(),
//        factory = { context ->
//            WebView(context).apply {
//                ViewCompat.setNestedScrollingEnabled(this,true)
//                webViewClient = WebViewClient()
//                settings.javaScriptEnabled = true // Enable JavaScript if needed
//                loadData(htmlContent, "text/html", "utf-8") // Load the HTML string directly
//            }
//        },
//        update = { webView ->
//            webView.loadData(htmlContent, "text/html", "utf-8")
//        }
//    )
    Text(text = remember(html) { htmlToAnnotatedString(html) })
}