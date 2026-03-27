package com.selegic.encye.training

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.selegic.encye.data.remote.dto.ImageDto
import com.selegic.encye.data.remote.dto.TrainingDto
import com.selegic.encye.data.remote.dto.TrainingModuleDto
import com.selegic.encye.data.remote.dto.UserDto
import com.selegic.encye.ui.theme.EncyeTheme
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private val DetailHtmlTagRegex = Regex("<[^>]*>")
private val DetailHtmlWhitespaceRegex = Regex("\\s+")

private enum class TrainingDetailTab(val label: String) {
    DESCRIPTION("Description"),
    MODULES("Modules"),
    REVIEWS("Reviews")
}

private data class TrainingDetailUiModel(
    val title: String,
    val trainerName: String,
    val category: String,
    val learnerCountLabel: String,
    val durationLabel: String,
    val moduleCountLabel: String,
    val description: AnnotatedString,
    val descriptionPlainText: String,
    val progressPercent: Int,
    val completedModules: Int,
    val selectedTab: TrainingDetailTab
)

private data class ReviewSummaryUi(
    val ratingLabel: String,
    val totalReviewsLabel: String,
    val distribution: List<Pair<Int, Float>>,
    val items: List<ReviewItemUi>
)

private data class ReviewItemUi(
    val author: String,
    val dateLabel: String,
    val badge: String,
    val rating: Int,
    val reviewText: String
)

@Composable
fun TrainingDetailScreen(
    trainingId: String,
    onBack: () -> Unit,
    onOpenModule: (String, TrainingModuleDto) -> Unit
) {
    val viewModel: TrainingDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(trainingId) {
        launch {
            viewModel.loadTraining(trainingId)
        }
        launch {
            viewModel.fetchEnrollments(trainingId)
        }
    }

    TrainingDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onEnrollClick = { viewModel.enrollInTraining(trainingId) },
        onRetry = {
            scope.launch {
                viewModel.loadTraining(trainingId, forceRefresh = true)
            }
        },
        onOpenModule = onOpenModule
    )
}
@Composable
fun TrainingDetailScreen(
    uiState: TrainingDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onEnrollClick: () -> Unit = {},
    onOpenModule: (String, TrainingModuleDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F8FC),
        bottomBar = {
            if (uiState.training != null && !uiState.isLoading) {
                TrainingEnrollBar(
                    onClick = onEnrollClick,
                    isLoading = uiState.isEnrollmentLoading,
                    isEnrolled = uiState.isEnrolled
                )
            }
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
                    onBack = onBack,
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
    onBack: () -> Unit,
    onOpenModule: (String, TrainingModuleDto) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiModel = remember(training) { training.toDetailUiModel() }
    val reviewSummary = remember(training.id) { buildReviewSummary() }
    var selectedTab by rememberSaveable(training.id) { mutableStateOf(uiModel.selectedTab) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FC)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TrainingDetailTopRow(onBack = onBack) }
        item { TrainingHero(imageUrl = training.coverImage?.url, title = training.title) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = uiModel.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF101828)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = uiModel.trainerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF7A8294),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = "|", color = Color(0xFFD0D5DD), fontWeight = FontWeight.Bold)
                    Text(
                        text = uiModel.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF3B6EF6),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailStatCard(Modifier.weight(1f), Icons.Outlined.Groups, uiModel.learnerCountLabel, "Learners")
                DetailStatCard(Modifier.weight(1f), Icons.Outlined.AccessTime, uiModel.durationLabel, "Duration")
                DetailStatCard(Modifier.weight(1f), Icons.Outlined.MenuBook, uiModel.moduleCountLabel, "Modules")
            }
        }
        item {
            TrainingTabBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }

        when (selectedTab) {
            TrainingDetailTab.DESCRIPTION -> {
                item { TrainerCard(name = uiModel.trainerName) }
                item {
                    DescriptionCard(
                        summary = uiModel.description,
                        fallbackText = uiModel.descriptionPlainText
                    )
                }
            }

            TrainingDetailTab.MODULES -> {
                item { ModuleProgressCard(progressPercent = uiModel.progressPercent) }
                itemsIndexed(training.modules, key = { _, module -> module.id }) { index, module ->
                    ModuleRowCard(
                        module = module,
                        index = index,
                        completedModules = uiModel.completedModules,
                        onClick = { onOpenModule(training.title, module) }
                    )
                }
                if (training.modules.isEmpty()) {
                    item { EmptyModulesState() }
                }
            }

            TrainingDetailTab.REVIEWS -> {
                item { ReviewSummaryCard(summary = reviewSummary) }
                itemsIndexed(reviewSummary.items, key = { index, _ -> index }) { _, item ->
                    ReviewCard(review = item)
                }
            }
        }
    }
}

@Composable
private fun TrainingDetailTopRow(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF101828)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DetailActionButton(Icons.Outlined.BookmarkBorder, "Bookmark")
            DetailActionButton(Icons.Outlined.Share, "Share")
        }
    }
}

@Composable
private fun DetailActionButton(
    imageVector: ImageVector,
    contentDescription: String
) {
    Surface(
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                tint = Color(0xFF344054),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TrainingHero(
    imageUrl: String?,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.55f)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF39316C), Color(0xFF2B275E), Color(0xFF1B1C4A))
                )
            )
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x55212A64))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0x55FFFFFF), Color(0x221A1E56), Color.Transparent)
                        )
                    )
            )
        }

        Surface(
            modifier = Modifier.align(Alignment.Center),
            shape = RoundedCornerShape(18.dp),
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DetailStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF101828),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF101828),
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF667085),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TrainingTabBar(
    selectedTab: TrainingDetailTab,
    onTabSelected: (TrainingDetailTab) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TrainingDetailTab.entries.forEach { tab ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (tab == selectedTab) Color(0xFF111827) else Color(0xFF475467)
                )
                Box(
                    modifier = Modifier
                        .width(76.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(if (tab == selectedTab) Color(0xFF2E6BFF) else Color(0xFFD9DDE7))
                )
            }
        }
    }
}

@Composable
private fun TrainerCard(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = Color(0xFFDCE7FF)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initialsFor(name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2E6BFF)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )
            Text(
                text = "UI/UX Designer",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )
        }
    }
}

@Composable
private fun DescriptionCard(
    summary: AnnotatedString,
    fallbackText: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Training Description",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF101828)
        )
        Text(
            text = summary.takeIf { it.text.isNotBlank() }
                ?: AnnotatedString(fallbackText.ifBlank { "No training description available yet." }),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF101828)
        )
    }
}

@Composable
private fun ModuleProgressCard(progressPercent: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Modules of this training",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828)
            )
            Text(
                text = "Overall Progress",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF475467),
                fontWeight = FontWeight.SemiBold
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFDCE5FF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent / 100f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E6BFF))
                )
            }
            Text(
                text = "$progressPercent%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E6BFF)
            )
        }
    }
}

@Composable
private fun ModuleRowCard(
    module: TrainingModuleDto,
    index: Int,
    completedModules: Int,
    onClick: () -> Unit
) {
    val moduleNumber = module.moduleNumber ?: (index + 1)
    val stateLabel = when {
        index < completedModules -> "Completed"
        index == completedModules -> "Start"
        else -> "Locked"
    }
    val stateColor = when (stateLabel) {
        "Completed" -> Color(0xFF4C74D8)
        "Start" -> Color(0xFF165DFF)
        else -> Color(0xFF98A2B3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 50.dp, height = 56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(Color(0xFF362B6B), Color(0xFF4C3C8E)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = moduleNumber.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF101828),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val moduleSummary = module.content.toPlainText()
                if (moduleSummary.isNotBlank()) {
                    Text(
                        text = moduleSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF667085),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(shape = CircleShape, color = stateColor) {
                Text(
                    text = stateLabel,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmptyModulesState() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Text(
            text = "Modules will appear here once this training is published.",
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF667085)
        )
    }
}

@Composable
private fun ReviewSummaryCard(summary: ReviewSummaryUi) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = summary.ratingLabel,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF101828)
                )
                Text(
                    text = "★★★★★",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFC107)
                )
                Surface(shape = CircleShape, color = Color(0xFF101828)) {
                    Text(
                        text = summary.totalReviewsLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                summary.distribution.forEach { (star, value) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$star",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF101828),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "★", color = Color(0xFFFFC107), fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCE5FF))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(value)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5A80E8))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewItemUi) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color(0xFFDCE7FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = initialsFor(review.author),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E6BFF)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${review.author}  ·  ${review.dateLabel}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101828)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF2F4F7))
                                    .border(1.dp, Color(0xFFE4E7EC), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = review.badge,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF475467),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(text = "★".repeat(review.rating), color = Color(0xFFFFC107))
                        }
                    }
                }
            }

            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF475467)
            )
        }
    }
}

@Composable
private fun TrainingEnrollBar(
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isEnrolled: Boolean = false
) {
    Surface(color = Color(0xFFF7F8FC), shadowElevation = 10.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            Button(
                onClick = onClick,
                enabled = !isLoading && !isEnrolled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isEnrolled) Color(0xFF344054) else Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isEnrolled) "Enrolled" else "Enroll now",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
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
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Unable to load training",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) { Text("Try again") }
            }
        }
    }
}

private fun TrainingDto.toDetailUiModel(): TrainingDetailUiModel {
    val moduleCount = modules.size
    val completedModules = if (moduleCount > 1) 1 else 0
    val progressPercent = if (moduleCount == 0) 0 else ((completedModules / moduleCount.toFloat()) * 100).roundToInt()
    val durationHours = max(1f, moduleCount * 1.2f - 0.1f)

    return TrainingDetailUiModel(
        title = title,
        trainerName = buildTrainerName(createdBy),
        category = resolveTrainingCategory(this),
        learnerCountLabel = "50+",
        durationLabel = if (durationHours % 1f == 0f) "${durationHours.toInt()} Hours" else "${durationHours} Hours",
        moduleCountLabel = moduleCount.toString().ifBlank { "0" },
        description = summary.toAnnotatedHtml(),
        descriptionPlainText = summary.toPlainText(),
        progressPercent = progressPercent,
        completedModules = completedModules,
        selectedTab = when {
            moduleCount > 0 -> TrainingDetailTab.MODULES
            summary.toPlainText().isNotBlank() -> TrainingDetailTab.DESCRIPTION
            else -> TrainingDetailTab.REVIEWS
        }
    )
}

private fun resolveTrainingCategory(training: TrainingDto): String {
    val text = "${training.title} ${training.summary.toPlainText()}".lowercase()
    return when {
        listOf("design", "ui", "ux", "figma", "brand").any(text::contains) -> "Design"
        listOf("medical", "health", "clinic", "doctor", "nursing").any(text::contains) -> "Medical"
        else -> "Programming"
    }
}

private fun buildReviewSummary(): ReviewSummaryUi {
    return ReviewSummaryUi(
        ratingLabel = "4.5",
        totalReviewsLabel = "653 reviews",
        distribution = listOf(5 to 0.96f, 4 to 0.84f, 3 to 0.68f, 2 to 0.45f, 1 to 0.22f),
        items = listOf(
            ReviewItemUi(
                author = "PetParent7",
                dateLabel = "22 Jul",
                badge = "Verified",
                rating = 5,
                reviewText = "Clear lessons, strong pacing, and useful examples. The modules feel practical rather than academic, which made the training easy to follow."
            ),
            ReviewItemUi(
                author = "Asha D",
                dateLabel = "18 Jul",
                badge = "Student",
                rating = 5,
                reviewText = "The course structure is simple and the module breakdown is excellent. I especially liked the way each lesson focused on one concept at a time."
            )
        )
    )
}

private fun buildTrainerName(createdBy: UserDto?): String {
    return listOfNotNull(createdBy?.firstName, createdBy?.lastName)
        .joinToString(" ")
        .ifBlank { "Encye Training" }
}

private fun initialsFor(name: String): String {
    return name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "EN" }
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
    return if (normalized.isBlank()) AnnotatedString("") else AnnotatedString.fromHtml(normalized)
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
                    title = "UI Design for Beginners",
                    summary = "<p>The training program is designed to help learners build a strong foundation in modern front-end design and practical interface thinking.</p><p>Participants will explore design systems, typography, component structure, spacing, and responsive workflows through concise modules.</p>",
                    coverImage = ImageDto(id = "image-1"),
                    createdBy = UserDto(id = "user-1", firstName = "Arindam", lastName = "Roy"),
                    modules = listOf(
                        TrainingModuleDto(
                            id = "module-1",
                            name = "Understanding The Threat Landscape",
                            moduleNumber = 1,
                            content = "<p>Start with the current risks, terminology, and patterns that define the design environment.</p>"
                        ),
                        TrainingModuleDto(
                            id = "module-2",
                            name = "Building a Strong Defense: Passwords, MFA, and Devices",
                            moduleNumber = 2,
                            content = "<p>Build repeatable workflows and practical habits that strengthen your daily process.</p>"
                        ),
                        TrainingModuleDto(
                            id = "module-3",
                            name = "Incident Response: When in Doubt, Report It!",
                            moduleNumber = 3,
                            content = "<p>Learn how to identify issues early and respond with confidence.</p>"
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
