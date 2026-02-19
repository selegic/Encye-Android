package com.selegic.encye.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.selegic.encye.R
import com.selegic.encye.data.remote.dto.PostDto

@Composable
fun PostItem( // Renamed from PostCard to avoid conflict
    modifier: Modifier = Modifier,
    post: PostDto,
    onLikeClick: (PostDto) -> Unit,
    onCommentClick: (PostDto) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            PostHeader(post)
            Spacer(modifier = Modifier.height(12.dp))
            PostContent(post)
            Spacer(modifier = Modifier.height(16.dp))
            PostActions(post, onLikeClick, onCommentClick)
        }
    }
}

@Composable
private fun PostHeader(post: PostDto) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(post.createdBy.profilePicture)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_launcher_background),
            contentDescription = "User Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "${post.createdBy.firstName} ${post.createdBy.lastName}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = post.createdAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PostContent(post: PostDto) {
    Column {
        Text(
            text = AnnotatedString.fromHtml(post.content),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        if (!post.image.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            // For simplicity, we'll just show the first image.
            // A real app would use a pager or a grid.
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.image.first().url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun PostActions(
    post: PostDto,
    onLikeClick: (PostDto) -> Unit,
    onCommentClick: (PostDto) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        TextButton(onClick = { onLikeClick(post) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ThumbUp,
                    contentDescription = "Like",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${post.likeCount} Likes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        TextButton(onClick = { onCommentClick(post) }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "${post.commentCount} Comments", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
