package com.selegic.encye.ui.component

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.selegic.encye.data.FakeData
import com.selegic.encye.data.remote.dto.PostDto
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun TextFirstFeedScreen(posts: List<PostDto>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), // Slight off-white/gray background
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Distinct separation between articles
    ) {
        items(posts) { post ->
            TextFocusPostCard(post = post,)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextFocusPostCard(post: PostDto, onCommentClick: () -> Unit = {}) {
    val authorName = "${post.createdBy.firstName} ${post.createdBy.lastName}"
    val categoryName = post.autoCategory?.primary?.name ?: "Article"
    // Clean up HTML tags and extra whitespace
    val plainTextContent = Html.fromHtml(post.content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
    val formattedDate = formatDate(post.createdAt)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // Flat edges like Twitter/LinkedIn
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
        ) {
            // 1. Publisher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.createdBy.profilePicture,
                    contentDescription = "Author Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(44.dp) // Slightly larger avatar to establish authority
                        .clip(CircleShape)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = authorName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    // Context string: "Technology • Feb 16"
                    Text(
                        text = "$categoryName • $formattedDate",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. The Main Focus: The Writing
            Text(
                text = plainTextContent,
                fontSize = 16.sp,
                lineHeight = 24.sp, // Generous line height for readability
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                maxLines = 6, // Show much more text before truncating
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(onClick = onCommentClick)
            )

            // Optional "Read more" if text is long
            if (plainTextContent.length > 250) {
                Text(
                    text = "Read more",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable(onClick = onCommentClick)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Topic Tags (Highlights what the writing is about)
            if (post.autoTags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    post.autoTags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "#${tag.replaceFirstChar { it.uppercase() }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 4. Secondary Focus: The Image Attachment
            val imageUrl = post.image?.firstOrNull()?.url
            if (!imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f) // Constrained landscape aspect ratio so it doesn't take over the screen
                        .clip(RoundedCornerShape(12.dp)) // Rounded corners make it look like an embedded attachment
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // 5. Action Footer (Subtle)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextActionButton(
                    icon = Icons.Outlined.FavoriteBorder,
                    label = if (post.likeCount > 0) post.likeCount.toString() else "Like"
                )
                TextActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = if (post.commentCount > 0) post.commentCount.toString() else "Comment",
                    onClick = onCommentClick
                )
                TextActionButton(
                    icon = Icons.Outlined.Share,
                    label = "Share"
                )
            }
        }
    }
}

@Composable
fun TextActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper to format the ISO date from your API to "Feb 16" or similar
fun formatDate(isoDate: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(isoDate) ?: return ""
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        formatter.format(date)
    } catch (e: Exception) {
        ""
    }
}

@Preview
@Composable
fun PostItemPreview() {
    TextFocusPostCard(FakeData.getPosts()[0],)
}