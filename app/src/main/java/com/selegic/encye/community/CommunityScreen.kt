package com.selegic.encye.community

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.CommunityDto
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.ui.theme.EncyeTheme
import kotlin.math.absoluteValue

@Composable
fun CommunityRoute(
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CommunityScreen(
        uiState = uiState,
        onRetry = viewModel::fetchCommunities,
        onToggleMembership = viewModel::toggleCommunityMembership
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    uiState: CommunityUiState,
    onRetry: () -> Unit,
    onToggleMembership: (CommunityDto) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Communities") }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.communities.isEmpty() -> {
                CommunityFeedbackState(
                    modifier = Modifier.padding(paddingValues),
                    title = "Couldn’t load communities",
                    message = uiState.errorMessage,
                    actionLabel = "Try again",
                    onAction = onRetry
                )
            }

            uiState.communities.isEmpty() -> {
                CommunityFeedbackState(
                    modifier = Modifier.padding(paddingValues),
                    title = "No communities yet",
                    message = "Communities will appear here once the API returns data.",
                    actionLabel = "Refresh",
                    onAction = onRetry
                )
            }

            else -> {
                val pagerState = rememberPagerState(pageCount = { uiState.communities.size })

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Community directory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Browse official community spaces across the network.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalPager(
                        state = pagerState,
                        pageSpacing = 16.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) { page ->
                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val fraction = 1f - (pageOffset.absoluteValue.coerceIn(0f, 1f) * 0.06f)

                        CommunityCard(
                            community = uiState.communities[page],
                            currentUserId = uiState.currentUserId,
                            isUpdatingMembership = uiState.communities[page].mongoId in uiState.updatingCommunityIds,
                            onToggleMembership = onToggleMembership,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(RoundedCornerShape(28.dp))
                                .border(
                                    width = if (fraction > 0.99f) 1.25.dp else 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(28.dp)
                                )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(uiState.communities.size) { index ->
                            val selected = index == pagerState.currentPage
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (selected) 24.dp else 8.dp, 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CommunityCard(
    community: CommunityDto,
    currentUserId: String?,
    isUpdatingMembership: Boolean,
    onToggleMembership: (CommunityDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val coverImageUrl = community.coverImage?.url
    val profileImageUrl = community.profileImage?.url
    val communityHandle = community.id.ifBlank {
        community.name.lowercase().replace(" ", "")
    }
    val description = community.about?.takeIf { it.isNotBlank() }
        ?: "No description available for this community yet."
    val isMember = !currentUserId.isNullOrBlank() && community.member.contains(currentUserId)
    val actionLabel = when {
        community.isAdmin || community.isModerator -> "Manage"
        isMember -> "Leave"
        else -> "Join"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 6.dp
    ) {
        Column() {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
            ) {
                if (!coverImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = coverImageUrl,
                        contentDescription = community.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .offset(y = (-22).dp)
                        .size(56.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!profileImageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = profileImageUrl,
                                contentDescription = community.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = community.name.firstOrNull()?.uppercase() ?: "C",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-12).dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = community.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        FilledTonalButton(
                            onClick = { onToggleMembership(community) },
                            enabled = !isUpdatingMembership && !community.isAdmin && !community.isModerator,
                            shape = ButtonDefaults.largePressedShape,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 10.dp,
                                vertical = 0.dp
                            )
                        ) {
                            if (isUpdatingMembership) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                if (!isMember && !community.isAdmin && !community.isModerator) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = actionLabel,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    CommunityMetaRow(community = community)

                    community.category
                        .filter { it.isNotBlank() }
                        .take(3)
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("  •  ")
                        ?.let { categories ->
                            Text(
                                text = categories,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun CommunityMetaRow(
    community: CommunityDto
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactMetaPill(value = community.member.size.toString(), label = "members")
        CompactMetaPill(value = community.moderator.size.toString(), label = "moderators")
        if (community.isAdmin || community.isModerator) {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = if (community.isAdmin) "Admin" else "Moderator",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun CompactMetaPill(
    value: String,
    label: String,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommunityFeedbackState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityScreenPreview() {
    EncyeTheme {
            CommunityScreen(
                uiState = CommunityUiState(
                    isLoading = false,
                    communities = previewCommunities
                ),
            onRetry = {},
            onToggleMembership = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityScreenLoadingPreview() {
    EncyeTheme {
        CommunityScreen(
            uiState = CommunityUiState(isLoading = true),
            onRetry = {},
            onToggleMembership = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityScreenErrorPreview() {
    EncyeTheme {
        CommunityScreen(
            uiState = CommunityUiState(
                isLoading = false,
                errorMessage = "Network request failed."
            ),
            onRetry = {},
            onToggleMembership = {}
        )
    }
}

private val previewCommunities = listOf(
    CommunityDto(
        mongoId = "1",
        name = "Android Architecture",
        id = "android_architecture",
        moderator = listOf("mod-1", "mod-2", "mod-3"),
        member = List(1240) { "member-$it" },
        profileImage = ImageDto(
            id = "img-1",
            url = "https://images.unsplash.com/photo-1512941937669-90a1b58e7e9c?auto=format&fit=crop&w=400&q=80"
        ),
        coverImage = ImageDto(
            id = "cover-1",
            url = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80"
        ),
        category = listOf("Android", "Architecture", "Jetpack"),
        about = "Patterns, app structure, state management, and practical discussions around building maintainable Android apps."
    ),
    CommunityDto(
        mongoId = "2",
        name = "Product Design Review",
        id = "product_design_review",
        moderator = listOf("mod-1", "mod-2"),
        member = List(860) { "member-$it" },
        category = listOf("Design", "UX", "Research"),
        about = "A moderated space for interface critiques, design systems, and product usability reviews."
    )
)
