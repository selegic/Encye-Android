package com.selegic.encye.article

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.selegic.encye.R
import com.selegic.encye.data.remote.dto.AutoCategoryDto
import com.selegic.encye.data.remote.dto.ArticleDto
import com.selegic.encye.data.remote.dto.CategoryDto
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.UserDto
import com.selegic.encye.ui.theme.EncyeTheme

private val HtmlTagRegex = Regex("<[^>]*>")
private val HtmlWhitespaceRegex = Regex("\\s+")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    onNavigateToArticleDetail: (ArticleDto) -> Unit,
    onCreateArticle: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val viewModel: ArticleViewModel = hiltViewModel()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val filteredArticlesState by viewModel.filteredArticlesState.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    ArticleScreenContent(
        allArticles = articles.itemSnapshotList.items,
        filteredArticlesState = filteredArticlesState,
        selectedCategory = selectedCategory,
        onCategorySelected = { category ->
            selectedCategory = category
            viewModel.selectCategory(category)
        },
        onLoadMoreFilteredArticles = viewModel::loadMoreFilteredArticles,
        onNavigateToArticleDetail = onNavigateToArticleDetail,
        onCreateArticle = onCreateArticle,
        sharedTransitionScope = sharedTransitionScope,
        animatedContentScope = animatedContentScope,
        pagingLoadState = articles.loadState
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleScreenContent(
    allArticles: List<ArticleDto>,
    filteredArticlesState: FilteredArticlesUiState,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onLoadMoreFilteredArticles: () -> Unit,
    onNavigateToArticleDetail: (ArticleDto) -> Unit,
    onCreateArticle: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
    pagingLoadState: CombinedLoadStates? = null
) {
    val articleTags = allArticles
        .mapNotNull { article -> article.autoCategory?.primary?.name?.trim()?.takeIf { it.isNotEmpty() } }
        .distinct()
    val categories = listOf("All") + articleTags
    val isAllSelected = selectedCategory == "All"
    val visibleArticles = remember(allArticles, filteredArticlesState.articles, isAllSelected) {
        if (isAllSelected) allArticles else filteredArticlesState.articles
    }

    // Scroll-direction tracking: positive = scrolling down, negative = scrolling up
    var scrollDelta by remember { mutableFloatStateOf(0f) }
    var isHeaderVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                scrollDelta += delta
                // Threshold to avoid flickering on tiny scrolls
                if (scrollDelta < -30f) {
                    isHeaderVisible = false
                    scrollDelta = 0f
                } else if (scrollDelta > 30f) {
                    isHeaderVisible = true
                    scrollDelta = 0f
                }
                return Offset.Zero
            }
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Discover",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${visibleArticles.size} stories ready",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SmallFloatingActionButton(
                            onClick = onCreateArticle,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_article)
                            )
                        }
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
//                AnimatedVisibility(
//                    visible = isHeaderVisible,
//                    enter = expandVertically() + fadeIn(),
//                    exit = shrinkVertically() + fadeOut()
//                ) {
//                    Column {
//                        Spacer(modifier = Modifier.height(10.dp))
//                        Surface(
//                            modifier = Modifier.fillMaxWidth(),
//                            shape = RoundedCornerShape(14.dp),
//                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
//                        ) {
//                            Row(
//                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
//                                Spacer(modifier = Modifier.width(8.dp))
//                                Text(
//                                    text = "Search news, topics, or authors",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
//                                )
//                            }
//                        }
//                    }
//                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) },
                            label = {
                                Text(
                                    category,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
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

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top stories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${visibleArticles.size} results",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(visibleArticles, key = { article -> article.id }) { articleDto ->
                ArticleCard(article = articleDto, onCLick = {
                    onNavigateToArticleDetail(articleDto)
                }, sharedTransitionScope = sharedTransitionScope, animatedContentScope = animatedContentScope)
            }

            if (isAllSelected) {
                pagingLoadState?.apply {
                    when {
                        refresh is LoadState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillParentMaxSize()) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }

                        append is LoadState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            }
                        }

                        refresh is LoadState.Error -> {
                            val e = refresh as LoadState.Error
                            item {
                                Text(text = "Error: ${e.error.localizedMessage}")
                            }
                        }

                        append is LoadState.Error -> {
                            val e = append as LoadState.Error
                            item {
                                Text(text = "Error: ${e.error.localizedMessage}")
                            }
                        }
                    }
                }
            } else {
                if (filteredArticlesState.isLoading && visibleArticles.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize()) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

                filteredArticlesState.errorMessage?.let { message ->
                    item {
                        Text(
                            text = "Error: $message",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                if (!filteredArticlesState.isLoading && visibleArticles.isEmpty() && filteredArticlesState.errorMessage == null) {
                    item {
                        Text(
                            text = "No loaded articles match yet. Load more to continue searching.",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (filteredArticlesState.hasMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = onLoadMoreFilteredArticles,
                                enabled = !filteredArticlesState.isLoading
                            ) {
                                if (filteredArticlesState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Load more articles")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleCard(
    article: ArticleDto,
    onCLick: (String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null
) {
    val authorName = listOfNotNull(article.createdBy?.firstName, article.createdBy?.lastName)
        .joinToString(" ")
        .ifBlank { "Encye Desk" }
    val plainDescription = article.description.toPlainText()
    val categoryName = article.autoCategory?.primary?.name?.uppercase()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onCLick(article.id) },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 128.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                AsyncImage(
                    model = article.image?.url,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .articleSharedElement(
                            key = "image-${article.id}",
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.28f)
                                ),
                                startY = 32f
                            )
                        )
                )

                if (categoryName != null) {
                    Surface(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.BottomStart),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = categoryName,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 128.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = formatArticleDate(article.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    modifier = Modifier.articleSharedElement(
                        key = "title-${article.id}",
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope
                    ),
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

//                Text(
//                    text = if (plainDescription.isBlank()) AnnotatedString.fromHtml(article.description) else AnnotatedString(plainDescription),
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    lineHeight = 18.sp
//                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = article.createdBy?.profilePicture,
                        contentDescription = null,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = authorName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = categoryName ?: "Featured",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.articleSharedElement(
    key: String,
    sharedTransitionScope: SharedTransitionScope?,
    animatedContentScope: AnimatedContentScope?
): Modifier {
    if (sharedTransitionScope == null || animatedContentScope == null) {
        return this
    }

    with(sharedTransitionScope) {
        return this@articleSharedElement.sharedElement(
            sharedTransitionScope.rememberSharedContentState(key = key),
            animatedContentScope
        )
    }
}

private fun String.toPlainText(): String {
    return replace(HtmlTagRegex, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(HtmlWhitespaceRegex, " ")
        .trim()
}

private fun formatArticleDate(raw: String): String {
    val cleaned = raw.substringBefore('T').substringBefore(' ')
    return cleaned.ifBlank { raw }.take(16)
}

@Preview
@Composable
fun ArticleScreenPreview() {
    EncyeTheme {
        ArticleScreenContent(
            allArticles = previewArticles,
            filteredArticlesState = FilteredArticlesUiState(),
            selectedCategory = "All",
            onCategorySelected = {},
            onLoadMoreFilteredArticles = {},
            onNavigateToArticleDetail = {},
            onCreateArticle = {}
        )
    }
}

private val previewArticles = listOf(
    ArticleDto(
        _id = "preview-1",
        id = "preview-1",
        title = "How community-led climate projects are scaling across cities",
        description = "<p>Local teams are combining policy, finance, and field data to turn pilots into durable public infrastructure.</p>",
        image = ImageDto(
            id = "image-1",
            url = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee"
        ),
        autoCategory = AutoCategoryDto(
            primary = CategoryDto(name = "Environment", slug = "environment")
        ),
        createdBy = UserDto(
            id = "user-1",
            firstName = "Asha",
            lastName = "Menon",
            profilePicture = "https://images.unsplash.com/photo-1494790108377-be9c29b29330"
        ),
        createdAt = "2026-03-11T09:15:00Z"
    ),
    ArticleDto(
        _id = "preview-2",
        id = "preview-2",
        title = "Why applied AI teams are moving evaluation earlier in the product cycle",
        description = "<p>Teams are treating evaluation as a design constraint, not a late-stage safety net.</p>",
        image = ImageDto(
            id = "image-2",
            url = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3"
        ),
        autoCategory = AutoCategoryDto(
            primary = CategoryDto(name = "Technology", slug = "technology")
        ),
        createdBy = UserDto(
            id = "user-2",
            firstName = "Rohan",
            lastName = "Das",
            profilePicture = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e"
        ),
        createdAt = "2026-03-10T18:40:00Z"
    )
)
