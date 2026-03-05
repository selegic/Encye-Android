package com.selegic.encye.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.selegic.encye.article.HtmlWebView
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.TrainingDto
import com.selegic.encye.data.remote.dto.TrainingModuleDto
import com.selegic.encye.data.remote.dto.UserDto
import com.selegic.encye.ui.theme.EncyeTheme

private val DetailHtmlTagRegex = Regex("<[^>]*>")
private val DetailHtmlWhitespaceRegex = Regex("\\s+")

@Composable
fun TrainingDetailScreen(
    trainingId: String,
    onBack: () -> Unit,
    onOpenModule: (String, TrainingModuleDto) -> Unit
) {
    val viewModel: TrainingDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(trainingId) {
        viewModel.loadTraining(trainingId)
    }

    TrainingDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = { viewModel.loadTraining(trainingId, forceRefresh = true) },
        onOpenModule = onOpenModule
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDetailScreen(
    uiState: TrainingDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenModule: (String, TrainingModuleDto) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Training Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
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

            uiState.training != null -> {
                TrainingDetailContent(
                    training = uiState.training,
                    onOpenModule = onOpenModule,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                TrainingDetailError(
                    message = uiState.errorMessage ?: "Unable to load training",
                    onRetry = onRetry,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TrainingDetailContent(
    training: TrainingDto,
    onOpenModule: (String, TrainingModuleDto) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
            ) {
                AsyncImage(
                    model = training.coverImage?.url,
                    contentDescription = training.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xCC101828)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = "${training.modules.size} modules",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = training.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Text(
                        text = buildTrainerName(training.createdBy),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.86f)
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                CollapsibleTrainingSummary(
                    summary = training.summary.toAnnotatedHtml(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TrainingStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Modules",
                    value = training.modules.size.toString(),
                    icon = Icons.Outlined.MenuBook
                )
                TrainingStatCard(
                    modifier = Modifier.weight(1f),
                    label = "Similar",
                    value = training.similar.size.toString(),
                    icon = Icons.Outlined.Quiz
                )
            }
        }

        item {
            Text(
                text = "Modules",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(training.modules, key = { _, module -> module.id }) { index, module ->
            TrainingModuleCard(
                module = module,
                index = index,
                onClick = { onOpenModule(training.title, module) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TrainingStatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrainingModuleCard(
    module: TrainingModuleDto,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${module.moduleNumber ?: index + 1}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = module.content.toPlainText().takeIf { it.isNotBlank() } ?: "No module description available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                TrainingModuleMeta(Icons.Outlined.PermMedia, "${module.images.size} images")
                TrainingModuleMeta(Icons.Outlined.PlayCircleOutline, "${module.videos.size} videos")
                TrainingModuleMeta(Icons.Outlined.Quiz, if (module.quiz != null) "Quiz" else "No quiz")
            }
        }
    }
}

@Composable
private fun TrainingModuleMeta(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.height(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CollapsibleTrainingSummary(
    summary: AnnotatedString,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 6
) {
    var isExpanded by remember(summary) { mutableStateOf(false) }
    var isOverflowing by remember(summary) { mutableStateOf(false) }

    val canToggle = isOverflowing || isExpanded

    Column(
        modifier = modifier.then(
            if (canToggle) {
                Modifier.clickable { isExpanded = !isExpanded }
            } else {
                Modifier
            }
        )
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    isOverflowing = textLayoutResult.hasVisualOverflow
                }
            }
        )

        if (canToggle) {
            Text(
                text = if (isExpanded) "Show less" else "Read more",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TrainingDetailError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onRetry) {
                Text("Try again")
            }
        }
    }
}

private fun buildTrainerName(createdBy: UserDto?): String {
    return listOfNotNull(createdBy?.firstName, createdBy?.lastName)
        .joinToString(" ")
        .ifBlank { "Encye Training" }
}

@Composable
fun TrainingHtmlContent(
    html: String,
    modifier: Modifier = Modifier
) {
    if (LocalInspectionMode.current) {
        Text(
            text = html.toPlainText(),
            modifier = modifier,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        HtmlWebView(
            html = html,
            modifier = modifier
        )
    }
}

private fun String?.toPlainText(): String {
    return this.orEmpty()
        .replace(DetailHtmlTagRegex, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(DetailHtmlWhitespaceRegex, " ")
        .trim()
}

private fun String?.toAnnotatedHtml(): AnnotatedString {
    val normalized = this.orEmpty()
        .replace("<meta[^>]*>".toRegex(RegexOption.IGNORE_CASE), "")
        .replace("&nbsp;", " ")
    return if (normalized.isBlank()) {
        AnnotatedString("")
    } else {
        AnnotatedString.fromHtml(normalized)
    }
}

@Preview(showBackground = true)
@Composable
private fun TrainingDetailScreenPreview() {
    EncyeTheme {
        TrainingDetailScreen(
            uiState = TrainingDetailUiState(
                isLoading = false,
                training = TrainingDto(
                    id = "training-1",
                    title = "From Busy to Impactful",
                    summary = "<p>Learn a modern blueprint for time mastery across deep work, focus, and sustainable execution.</p>",
                    coverImage = ImageDto(id = "image-1"),
                    createdBy = UserDto(id = "user-1", firstName = "Tuhin", lastName = "Bal"),
                    modules = listOf(
                        TrainingModuleDto(
                            id = "module-1",
                            name = "The ROI of Strategic Time Management",
                            moduleNumber = 1,
                            content = "<p>Time is a strategic asset. This module reframes productivity around value creation.</p>"
                        ),
                        TrainingModuleDto(
                            id = "module-2",
                            name = "Focus Systems That Scale",
                            moduleNumber = 2,
                            content = "<p>Build repeatable systems for protecting attention and reducing decision fatigue.</p>"
                        )
                    )
                )
            ),
            onBack = { },
            onRetry = { },
            onOpenModule = { _, _ -> }
        )
    }
}
