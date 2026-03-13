package com.selegic.encye.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.selegic.encye.ui.theme.EncyeTheme

@Composable
fun PostCreate(
    modifier: Modifier = Modifier,
    userDisplayName: String = "You",
    userAvatarUrl: String? = null,
    attachmentCount: Int = 0,
    isSubmitting: Boolean = false,
    onPostClick: () -> Unit = {},
    onPhotoClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (!userAvatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = userAvatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = userDisplayName.firstOrNull()?.uppercase() ?: "Y",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (attachmentCount > 0) {
                            "$attachmentCount photo${if (attachmentCount == 1) "" else "s"} ready to share"
                        } else {
                            "Start a quick update for your feed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            Surface(
                onClick = onPostClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
//                    Text(
//                        text = "What's moving right now?",
//                        style = MaterialTheme.typography.titleSmall,
//                        color = MaterialTheme.colorScheme.onSurface
//                    )
                    Text(
                        text = "Share a thought, question, milestone, or image update.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PostActionItem(
                    icon = Icons.Default.Edit,
                    text = "Write post",
                    iconTint = Color(0xFF2477F4),
                    onClick = onPostClick,
                    modifier = Modifier.weight(1f)
                )
                PostActionItem(
                    icon = Icons.Default.AddPhotoAlternate,
                    text = "Add photos",
                    iconTint = Color(0xFF45BD62),
                    onClick = onPhotoClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PostActionItem(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PostCreatePreview() {
    EncyeTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                .padding(16.dp)
        ) {
            PostCreate(
                userDisplayName = "Avery Jones",
                attachmentCount = 2,
                onPostClick = {},
                onPhotoClick = {}
            )
        }
    }
}
