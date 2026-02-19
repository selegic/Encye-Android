package com.selegic.encye.article

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.ArticleDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    onNavigateToArticleDetail: (ArticleDto) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val viewModel: ArticleViewModel = hiltViewModel()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    var selectedCategory by remember { mutableStateOf("For You") }
    val categories = listOf("For You", "Technology", "Global", "Economy", "Science", "Health")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Discover",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                // Expressive Search Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Search news, topics, or authors...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Category Tabs
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = null
                        )
                    }
                }
            }

            items(articles.itemCount) { index ->
                articles[index]?.let { articleDto ->
                    ArticleCard(article = articleDto, onCLick = {
                        onNavigateToArticleDetail(articleDto)
                    }, sharedTransitionScope = sharedTransitionScope, animatedContentScope = animatedContentScope)
                }
            }

            articles.apply {
                when {
                    loadState.refresh is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }

                    loadState.append is LoadState.Loading -> {
                        item {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }

                    loadState.refresh is LoadState.Error -> {
                        val e = articles.loadState.refresh as LoadState.Error
                        item {
                            Text(text = "Error: ${e.error.localizedMessage}")
                        }
                    }

                    loadState.append is LoadState.Error -> {
                        val e = articles.loadState.append as LoadState.Error
                        item {
                            Text(text = "Error: ${e.error.localizedMessage}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(article: ArticleDto, onCLick: (String) -> Unit = {}, sharedTransitionScope: SharedTransitionScope, animatedContentScope: AnimatedContentScope) {
    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCLick(article.id) }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Expressive Shape Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)

            ) {
                AsyncImage(
                    model = article.image?.url,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = "image-${article.id}"),
                            animatedContentScope
                        )
                        .clip(RoundedCornerShape(32.dp))
                )
                // Category Badge
                article.autoCategory?.primary?.let {
                    Surface(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = it.name.uppercase(),
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                ,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headline
            Text(
                modifier = Modifier.sharedElement(
                    sharedTransitionScope.rememberSharedContentState(key = "title-${article.id}"),
                    animatedContentScope
                ),
                text = article.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(8.dp))


            AnnotatedString.fromHtml(article.description)
            // Description
            Text(
                text = AnnotatedString.fromHtml(article.description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Author Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = article.createdBy?.profilePicture,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${article.createdBy?.firstName} ${article.createdBy?.lastName}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = article.createdAt, // This needs to be formatted
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                IconButton(onClick = { }) {
                    Icon(
                        Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ArticleScreenPreview() {
    SharedTransitionLayout( ) {
        ArticleScreen(
            animatedContentScope = LocalNavAnimatedContentScope.current,
            sharedTransitionScope = this@SharedTransitionLayout,
            onNavigateToArticleDetail = { },
        )
    }
}
