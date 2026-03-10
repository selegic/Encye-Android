package com.selegic.encye.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.ArticleDto
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.UserProfileArticleDto
import com.selegic.encye.data.remote.dto.UserProfileDataDto
import com.selegic.encye.data.remote.dto.UserProfilePostDto
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val HtmlTagRegex = Regex("<[^>]*>")
private val HtmlWhitespaceRegex = Regex("\\s+")

@Composable
fun UserProfileRoute(
    userId: String? = null,
    onBack: () -> Unit,
    onOpenArticle: (ArticleDto) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(userId) {
        viewModel.loadIfNeeded(userId)
    }

    UserProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = { viewModel.refresh(userId) },
        onOpenArticle = onOpenArticle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    uiState: UserProfileUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenArticle: (ArticleDto) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            UserProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is UserProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }

            is UserProfileUiState.Success -> {
                UserProfileContent(
                    modifier = Modifier.padding(paddingValues),
                    profile = uiState.profile,
                    onRetry = onRetry,
                    onOpenArticle = onOpenArticle
                )
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    modifier: Modifier = Modifier,
    profile: UserProfileDataDto,
    onRetry: () -> Unit,
    onOpenArticle: (ArticleDto) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Articles", "Posts")
    val fullName = listOfNotNull(
        profile.userDetails.firstName,
        profile.userDetails.lastName
    ).joinToString(" ").ifBlank { "Encye User" }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = profile.userDetails.profilePicture,
                                contentDescription = fullName,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = fullName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                profile.userDetails.email?.let { email ->
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = if (profile.isFollowing) "Following" else "Not Following",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ProfileStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Followers",
                                value = profile.userDetails.followersCount ?: 0,
                                icon = Icons.Outlined.PeopleAlt
                            )
                            ProfileStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Following",
                                value = profile.userDetails.followingCount ?: 0,
                                icon = Icons.Outlined.PeopleAlt
                            )
                            ProfileStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Posts",
                                value = profile.posts.size,
                                icon = Icons.Outlined.GridView
                            )
                            ProfileStatCard(
                                modifier = Modifier.weight(1f),
                                label = "Articles",
                                value = profile.articles.size,
                                icon = Icons.Outlined.Article
                            )
                        }
                    }
                }
            }
        }

        item {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            items(profile.articles, key = { it.articleId }) { article ->
                ArticleProfileCard(
                    article = article,
                    onClick = { onOpenArticle(article.toArticleDto(profile.userDetails)) }
                )
            }
        } else {
            items(profile.posts, key = { it.id }) { post ->
                PostProfileCard(
                    post = post,
                    authorName = fullName,
                    authorAvatar = profile.userDetails.profilePicture
                )
            }
        }

        if ((selectedTab == 0 && profile.articles.isEmpty()) || (selectedTab == 1 && profile.posts.isEmpty())) {
            item {
                EmptyProfileSection(
                    title = if (selectedTab == 0) "No articles yet" else "No posts yet",
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ArticleProfileCard(
    article: UserProfileArticleDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AsyncImage(
                model = article.image.toImageUrl(),
                contentDescription = article.title,
                modifier = Modifier
                    .size(width = 92.dp, height = 110.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.description.toPlainText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.createdAt.toProfileDate(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PostProfileCard(
    post: UserProfilePostDto,
    authorName: String,
    authorAvatar: String?
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = authorAvatar,
                    contentDescription = authorName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = post.createdAt.toProfileDate(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = AnnotatedString.fromHtml(post.content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileMetaPill(text = "${post.likeCount} likes")
                ProfileMetaPill(text = "${post.commentCount} comments")
                if (post.autoTags.isNotEmpty()) {
                    ProfileMetaPill(text = "#${post.autoTags.first()}")
                }
            }
        }
    }
}

@Composable
private fun ProfileMetaPill(text: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun EmptyProfileSection(
    title: String,
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Pull the latest profile data again if this should contain content.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Refresh",
                modifier = Modifier.clickable(onClick = onRetry),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun String.toPlainText(): String {
    return replace(HtmlTagRegex, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(HtmlWhitespaceRegex, " ")
        .trim()
}

private fun String.toProfileDate(): String {
    return substringBefore("T").ifBlank { this }.take(16)
}

private fun UserProfileArticleDto.toArticleDto(author: com.selegic.encye.data.remote.dto.UserDto): ArticleDto {
    return ArticleDto(
        _id = articleId,
        id = id,
        title = title,
        description = description,
        image = image.toImageDto(),
        createdBy = author,
        createdAt = createdAt
    )
}

private fun kotlinx.serialization.json.JsonElement?.toImageDto(): ImageDto? {
    val obj = this?.jsonObject ?: return null
    val id = obj["_id"]?.jsonPrimitive?.contentOrNull ?: return null
    return ImageDto(
        id = id,
        externalId = obj["id"]?.jsonPrimitive?.contentOrNull,
        idWithExt = obj["idWithExt"]?.jsonPrimitive?.contentOrNull,
        url = obj["url"]?.jsonPrimitive?.contentOrNull,
        name = obj["name"]?.jsonPrimitive?.contentOrNull,
        filePath = obj["filePath"]?.jsonPrimitive?.contentOrNull,
        type = obj["type"]?.jsonPrimitive?.contentOrNull,
        category = obj["category"]?.jsonPrimitive?.contentOrNull,
        organization = obj["organization"]?.jsonPrimitive?.contentOrNull,
        createdBy = obj["createdBy"]?.jsonPrimitive?.contentOrNull,
        createdAt = obj["createdAt"]?.jsonPrimitive?.contentOrNull,
        updatedAt = obj["updatedAt"]?.jsonPrimitive?.contentOrNull,
        order = obj["order"]?.jsonPrimitive?.contentOrNull?.toIntOrNull(),
        version = obj["__v"]?.jsonPrimitive?.contentOrNull?.toIntOrNull()
    )
}

private fun kotlinx.serialization.json.JsonElement?.toImageUrl(): String? {
    return this
        ?.jsonObject
        ?.get("url")
        ?.jsonPrimitive
        ?.contentOrNull
}
