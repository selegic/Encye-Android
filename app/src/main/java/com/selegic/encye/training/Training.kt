package com.selegic.encye.training

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DesignServices
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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
import kotlin.math.abs
import kotlin.math.min

private val HtmlTagRegex = Regex("<[^>]*>")
private val HtmlWhitespaceRegex = Regex("\\s+")

private enum class TrainingCourseStatus(
    val label: String,
    val progressLabel: String
) {
    NOT_STARTED("Not-Started", "Completed : 0%"),
    IN_PROGRESS("In-Progress", "Completed : 25%"),
    COMPLETED("Completed", "Completed : 100%")
}

private data class TrainingCategoryUi(
    val label: String,
    val icon: ImageVector
)

private data class TrainingCourseUi(
    val training: TrainingDto,
    val category: TrainingCategoryUi,
    val instructorName: String,
    val lessonCount: Int,
    val durationLabel: String,
    val status: TrainingCourseStatus,
    val progress: Float
)

@Composable
fun TrainingScreen(
    onNavigateToTrainingDetail: (String) -> Unit = { }
) {
    val viewModel: TrainingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrainingScreen(
        uiState = uiState,
        onRetry = viewModel::loadTrainings,
        onNavigateToTrainingDetail = onNavigateToTrainingDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    uiState: TrainingUiState,
    onRetry: () -> Unit,
    onNavigateToTrainingDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Training", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh trainings"
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

            uiState.errorMessage != null && uiState.trainings.isEmpty() -> {
                TrainingErrorState(
                    message = uiState.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                TrainingContent(
                    trainings = uiState.trainings,
                    errorMessage = uiState.errorMessage,
                    onRetry = onRetry,
                    onNavigateToTrainingDetail = onNavigateToTrainingDetail,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TrainingContent(
    trainings: List<TrainingDto>,
    errorMessage: String?,
    onRetry: () -> Unit,
    onNavigateToTrainingDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCourses = remember(trainings) { trainings.map(::toTrainingCourseUi) }
    val activeCourses = remember(allCourses) {
        allCourses.filter { it.status == TrainingCourseStatus.IN_PROGRESS }
    }
    val completedCourses = remember(allCourses) {
        allCourses.filter { it.status == TrainingCourseStatus.COMPLETED }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8FC)),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            TrainingHeader()
        }

        item {
            TrainingCategoryRow()
        }

        if (errorMessage != null && trainings.isNotEmpty()) {
            item {
                InlineTrainingError(
                    message = errorMessage,
                    onRetry = onRetry
                )
            }
        }

        if (allCourses.isEmpty()) {
            item {
                EmptyTrainingState(onRetry = onRetry)
            }
        } else {
            item {
                TrainingSection(
                    title = "All Training",
                    courses = allCourses,
                    onCourseClick = { onNavigateToTrainingDetail(it.training.id) }
                )
            }

            item {
                TrainingSection(
                    title = "Active Training",
                    courses = activeCourses.ifEmpty { allCourses.take(2) },
                    onCourseClick = { onNavigateToTrainingDetail(it.training.id) }
                )
            }

            item {
                TrainingSection(
                    title = "Completed Training",
                    courses = completedCourses.ifEmpty { allCourses.takeLast(2) },
                    onCourseClick = { onNavigateToTrainingDetail(it.training.id) }
                )
            }
        }
    }
}

@Composable
private fun TrainingHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Training",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TrainingCategoryRow() {
    val categories = remember {
        listOf(
            TrainingCategoryUi("Design", Icons.Outlined.DesignServices),
            TrainingCategoryUi("Programming", Icons.Outlined.Code),
            TrainingCategoryUi("Medical", Icons.Outlined.MedicalServices),
            TrainingCategoryUi("More", Icons.Outlined.MoreHoriz)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        categories.forEach { category ->
            TrainingCategoryItem(category = category)
        }
    }
}

@Composable
private fun TrainingCategoryItem(
    category: TrainingCategoryUi
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = Color(0xFFE8ECF4)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.label,
                    tint = Color(0xFF4A5568),
                    modifier = Modifier.size(34.dp)
                )
            }
        }

        Text(
            text = category.label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF222B45)
        )
    }
}

@Composable
private fun TrainingSection(
    title: String,
    courses: List<TrainingCourseUi>,
    onCourseClick: (TrainingCourseUi) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View all",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.SemiBold
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(courses, key = { it.training.id + title }) { course ->
                TrainingCourseCard(
                    course = course,
                    onClick = { onCourseClick(course) }
                )
            }
        }
    }
}

@Composable
private fun TrainingCourseCard(
    course: TrainingCourseUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TrainingCourseImage(
                imageUrl = course.training.coverImage?.url,
                title = course.training.title
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = course.training.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${course.instructorName}  |  ${course.category.label}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            TrainingCardMetaRow(
                lessonCount = course.lessonCount,
                durationLabel = course.durationLabel
            )

            TrainingProgressBlock(course = course)
        }
    }
}

@Composable
private fun TrainingCourseImage(
    imageUrl: String?,
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF18203F),
                        Color(0xFF2A1F5A),
                        Color(0xFF0B7CFF)
                    )
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
                    .background(Color(0x550A1120))
            )
        } else {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.94f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun TrainingCardMetaRow(
    lessonCount: Int,
    durationLabel: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrainingMetaItem(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            text = lessonCount.toString()
        )
        TrainingMetaItem(
            icon = Icons.Outlined.Schedule,
            text = durationLabel
        )
    }
}

@Composable
private fun TrainingMetaItem(
    icon: ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF64748B),
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
        )
    }
}

@Composable
private fun TrainingProgressBlock(
    course: TrainingCourseUi
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = course.status.progressLabel,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8)
        )
        LinearProgressIndicator(
            progress = { course.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = statusColor(course.status),
            trackColor = Color(0xFFE2E8F0)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TrainingStatusChip(status = course.status)
        }
    }
}

@Composable
private fun TrainingStatusChip(
    status: TrainingCourseStatus
) {
    Surface(
        shape = CircleShape,
        color = statusColor(status).copy(alpha = 0.14f)
    ) {
        Text(
            text = status.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = statusColor(status),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun InlineTrainingError(
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Retry",
                modifier = Modifier.clickable(onClick = onRetry),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TrainingErrorState(
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
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Unable to load training",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(onClick = onRetry) {
                    Text("Try again")
                }
            }
        }
    }
}

@Composable
private fun EmptyTrainingState(
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No training available yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Refresh to load newly published learning paths.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Refresh")
            }
        }
    }
}

private fun toTrainingCourseUi(training: TrainingDto): TrainingCourseUi {
    val category = resolveCategory(training)
    val status = resolveStatus(training.id)
    val lessonCount = training.modules.size.coerceAtLeast(1)

    return TrainingCourseUi(
        training = training,
        category = category,
        instructorName = buildInstructorName(training.createdBy),
        lessonCount = lessonCount,
        durationLabel = "${lessonCount * 15}",
        status = status,
        progress = when (status) {
            TrainingCourseStatus.NOT_STARTED -> 0f
            TrainingCourseStatus.IN_PROGRESS -> 0.25f
            TrainingCourseStatus.COMPLETED -> 1f
        }
    )
}

private fun resolveCategory(training: TrainingDto): TrainingCategoryUi {
    val text = "${training.title} ${training.summary.toPlainText()}".lowercase()
    return when {
        listOf("design", "ui", "ux", "figma", "brand").any(text::contains) ->
            TrainingCategoryUi("Design", Icons.Outlined.DesignServices)
        listOf("medical", "health", "clinic", "doctor", "nursing").any(text::contains) ->
            TrainingCategoryUi("Medical", Icons.Outlined.MedicalServices)
        else -> TrainingCategoryUi("Programming", Icons.Outlined.Code)
    }
}

private fun resolveStatus(id: String): TrainingCourseStatus {
    return when (abs(id.hashCode()) % 3) {
        0 -> TrainingCourseStatus.NOT_STARTED
        1 -> TrainingCourseStatus.IN_PROGRESS
        else -> TrainingCourseStatus.COMPLETED
    }
}

private fun statusColor(status: TrainingCourseStatus): Color {
    return when (status) {
        TrainingCourseStatus.NOT_STARTED -> Color(0xFFF59E0B)
        TrainingCourseStatus.IN_PROGRESS -> Color(0xFF38BDF8)
        TrainingCourseStatus.COMPLETED -> Color(0xFF22C55E)
    }
}

private fun buildInstructorName(createdBy: UserDto?): String {
    return listOfNotNull(createdBy?.firstName, createdBy?.lastName)
        .joinToString(" ")
        .ifBlank { "Encye" }
}

private fun String?.toPlainText(): String {
    return this.orEmpty()
        .replace(HtmlTagRegex, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(HtmlWhitespaceRegex, " ")
        .trim()
}

@Preview(showBackground = true)
@Composable
private fun TrainingScreenPreview() {
    EncyeTheme {
        TrainingScreen(
            uiState = TrainingUiState(
                isLoading = false,
                trainings = listOf(
                    TrainingDto(
                        id = "1",
                        title = "UI Design for Beginners",
                        summary = "Learn fundamentals of interface design, layout, and visual hierarchy.",
                        coverImage = ImageDto(id = "img-1", url = null),
                        modules = listOf(
                            TrainingModuleDto(id = "module-1", name = "Design Basics"),
                            TrainingModuleDto(id = "module-2", name = "Color and Typography")
                        ),
                        createdBy = UserDto(id = "user-1", firstName = "Arindam", lastName = "Roy")
                    ),
                    TrainingDto(
                        id = "2",
                        title = "Vue JS Scratch Course",
                        summary = "Master components, routing, and state management with Vue.",
                        coverImage = ImageDto(id = "img-2", url = null),
                        modules = listOf(
                            TrainingModuleDto(id = "module-1", name = "Vue Intro"),
                            TrainingModuleDto(id = "module-2", name = "Reactive State")
                        ),
                        createdBy = UserDto(id = "user-2", firstName = "Arindam", lastName = "Roy")
                    ),
                    TrainingDto(
                        id = "3",
                        title = "Design Fundamentals",
                        summary = "A practical design systems primer for product teams.",
                        coverImage = ImageDto(id = "img-3", url = null),
                        modules = listOf(
                            TrainingModuleDto(id = "module-1", name = "Foundation"),
                            TrainingModuleDto(id = "module-2", name = "Patterns"),
                            TrainingModuleDto(id = "module-3", name = "Accessibility")
                        ),
                        createdBy = UserDto(id = "user-3", firstName = "Arindam", lastName = "Roy")
                    )
                )
            ),
            onRetry = { },
            onNavigateToTrainingDetail = { }
        )
    }
}
