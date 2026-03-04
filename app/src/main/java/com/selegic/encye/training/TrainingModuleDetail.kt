package com.selegic.encye.training

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.selegic.encye.article.HtmlWebView
import com.selegic.encye.data.remote.dto.TrainingModuleDto
import com.selegic.encye.ui.theme.EncyeTheme
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val ModuleDetailHtmlTagRegex = Regex("<[^>]*>")
private val ModuleDetailHtmlWhitespaceRegex = Regex("\\s+")

private data class ModuleQuizUi(
    val id: String?,
    val questions: List<ModuleQuizQuestionUi>
)

private data class ModuleQuizOptionUi(
    val text: String,
    val isCorrect: Boolean = false
)

private data class ModuleQuizQuestionUi(
    val question: String,
    val options: List<ModuleQuizOptionUi>,
    val correctIndex: Int?
)

private data class ModuleQuizEvaluationUi(
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val scoredQuestions: Int,
    val correctAnswers: Int,
    val results: List<ModuleQuizQuestionResultUi>
) {
    val incorrectAnswers: Int = scoredQuestions - correctAnswers
}

private data class ModuleQuizQuestionResultUi(
    val selectedIndex: Int?,
    val correctIndex: Int?,
    val isCorrect: Boolean?
)

private enum class ModuleDetailMode {
    CONTENT,
    QUIZ
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingModuleDetailScreen(
    trainingTitle: String,
    module: TrainingModuleDto,
    onBack: () -> Unit
) {
    val quiz = remember(module.quiz) { module.quiz.toModuleQuizUi() }
    var mode by remember(module.id) { mutableStateOf(ModuleDetailMode.CONTENT) }
    val showingQuiz = mode == ModuleDetailMode.QUIZ && quiz != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (mode == ModuleDetailMode.QUIZ) "Module Quiz" else "Module Detail",
                            maxLines = 1
                        )
                        Text(
                            text = trainingTitle,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (quiz != null) {
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (mode == ModuleDetailMode.QUIZ) {
                            OutlinedButton(
                                onClick = { mode = ModuleDetailMode.CONTENT },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Back to module")
                            }
                            Button(
                                onClick = { },
                                modifier = Modifier.weight(1f),
                                enabled = false
                            ) {
                                Text("Quiz active")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { mode = ModuleDetailMode.CONTENT },
                                modifier = Modifier.weight(1f),
                                enabled = false
                            ) {
                                Text("Reading")
                            }
                            Button(
                                onClick = { mode = ModuleDetailMode.QUIZ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Take quiz")
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .then(
                    if (showingQuiz) {
                        Modifier
                    } else {
                        Modifier.verticalScroll(rememberScrollState())
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!showingQuiz) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = module.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Module ${module.moduleNumber ?: 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ModuleStatRow(Icons.Outlined.MenuBook, "Documents", module.documents.size.toString())
                            ModuleStatRow(Icons.Outlined.PermMedia, "Images", module.images.size.toString())
                            ModuleStatRow(Icons.Outlined.PlayCircleOutline, "Videos", module.videos.size.toString())
                            ModuleStatRow(Icons.Outlined.Quiz, "Quiz", if (module.quiz != null) "Available" else "None")
                        }
                    }
                }
            }

            if (showingQuiz) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ModuleQuizSection(
                        quiz = quiz,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            } else if (module.content.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No module content available.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ModuleContentSection(
                    html = module.content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ModuleContentSection(
    html: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (LocalInspectionMode.current) {
            Text(
                text = html.toPlainText(),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            HtmlWebView(
                html = html,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ModuleQuizSection(
    quiz: ModuleQuizUi,
    modifier: Modifier = Modifier
) {
    var selectedAnswers by remember(quiz.id) { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var evaluation by remember(quiz.id) { mutableStateOf<ModuleQuizEvaluationUi?>(null) }
    val answeredCount = selectedAnswers.size
    val totalQuestions = quiz.questions.size
    val showResults = evaluation != null
    val allQuestionsAnswered = answeredCount == totalQuestions && totalQuestions > 0

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Knowledge check",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalQuestions question${if (totalQuestions == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = if (showResults) {
                        evaluation?.summaryText().orEmpty()
                    } else {
                        "$answeredCount of $totalQuestions answered. Submit when every question has a selection."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        quiz.questions.forEachIndexed { index, question ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Question ${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (showResults && evaluation?.results?.getOrNull(index)?.isCorrect == true) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    question.options.forEachIndexed { optionIndex, option ->
                        val isSelected = selectedAnswers[index] == optionIndex
                        val isCorrect = question.correctIndex == optionIndex
                        val optionColor = when {
                            showResults && isCorrect -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            showResults && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
                            isSelected -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = optionColor,
                            onClick = {
                                if (!showResults) {
                                    selectedAnswers = selectedAnswers + (index to optionIndex)
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ('A' + optionIndex).toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (showResults && isCorrect) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else if (showResults && isSelected && !isCorrect) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (showResults && isCorrect) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else if (showResults && isSelected && !isCorrect) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else if (isSelected) {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                    if (showResults) {
                        val result = evaluation?.results?.getOrNull(index)
                        Text(
                            text = when {
                                result?.isCorrect == true -> "Correct"
                                result?.correctIndex != null -> {
                                    val answerLabel = ('A' + result.correctIndex).toString()
                                    "Incorrect. Correct answer: $answerLabel"
                                }
                                result?.selectedIndex != null -> "Answer submitted"
                                else -> "No answer submitted"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (result?.isCorrect) {
                                true -> MaterialTheme.colorScheme.primary
                                false -> MaterialTheme.colorScheme.error
                                null -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedAnswers = emptyMap()
                    evaluation = null
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null
                )
                Text("Reset")
            }
            Button(
                onClick = {
                    evaluation = quiz.evaluate(selectedAnswers)
                },
                enabled = allQuestionsAnswered && !showResults,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (showResults) "Submitted" else "Submit answers")
            }
        }
    }
}

@Composable
private fun ModuleStatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun String?.toPlainText(): String {
    return this.orEmpty()
        .replace(ModuleDetailHtmlTagRegex, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(ModuleDetailHtmlWhitespaceRegex, " ")
        .trim()
}

private fun JsonElement?.toModuleQuizUi(): ModuleQuizUi? {
    val objectValue = this as? JsonObject ?: return null
    val questions = objectValue["questions"]
        ?.jsonArrayOrNull()
        ?.mapNotNull { it.toModuleQuizQuestionUi() }
        .orEmpty()
        .ifEmpty {
            objectValue["quizQuestions"]
                ?.jsonArrayOrNull()
                ?.mapNotNull { it.toModuleQuizQuestionUi() }
                .orEmpty()
        }
        .ifEmpty {
            objectValue["items"]
                ?.jsonArrayOrNull()
                ?.mapNotNull { it.toModuleQuizQuestionUi() }
                .orEmpty()
        }
    if (questions.isEmpty()) return null
    return ModuleQuizUi(
        id = objectValue["_id"]?.jsonPrimitiveContentOrNull()
            ?: objectValue["id"]?.jsonPrimitiveContentOrNull(),
        questions = questions
    )
}

private fun JsonElement.toModuleQuizQuestionUi(): ModuleQuizQuestionUi? {
    val objectValue = this as? JsonObject ?: return null
    val options = objectValue["options"]
        ?.jsonArrayOrNull()
        ?.mapNotNull { it.toModuleQuizOptionUi() }
        .orEmpty()
    val correctIndexFromOptions = options.indexOfFirst { it.isCorrect }.takeIf { it >= 0 }
    val questionText = objectValue["question"]?.jsonPrimitiveContentOrNull()
        ?: objectValue["prompt"]?.jsonPrimitiveContentOrNull()
        ?: objectValue["title"]?.jsonPrimitiveContentOrNull()
        ?: return null
    if (options.isEmpty()) return null
    return ModuleQuizQuestionUi(
        question = questionText,
        options = options,
        correctIndex = objectValue.findCorrectIndex(options) ?: correctIndexFromOptions
    )
}

private fun JsonElement.toModuleQuizOptionUi(): ModuleQuizOptionUi? {
    return when (this) {
        is JsonPrimitive -> jsonPrimitiveContentOrNull()
            ?.takeIf { it.isNotBlank() }
            ?.let { ModuleQuizOptionUi(text = it) }
        is JsonObject -> {
            val text = this["text"]?.jsonPrimitiveContentOrNull()
                ?: this["option"]?.jsonPrimitiveContentOrNull()
                ?: this["label"]?.jsonPrimitiveContentOrNull()
                ?: this["answer"]?.jsonPrimitiveContentOrNull()
                ?: return null
            ModuleQuizOptionUi(
                text = text,
                isCorrect = this["isCorrect"]?.jsonPrimitiveBooleanOrNull()
                    ?: this["correct"]?.jsonPrimitiveBooleanOrNull()
                    ?: this["isAnswer"]?.jsonPrimitiveBooleanOrNull()
                    ?: false
            )
        }
        else -> null
    }
}

private fun JsonObject.findCorrectIndex(options: List<ModuleQuizOptionUi>): Int? {
    val indexedKeys = listOf(
        "correctIndex",
        "correctAnswerIndex",
        "answerIndex",
        "rightAnswerIndex"
    )
    indexedKeys.forEach { key ->
        this[key]?.jsonPrimitiveIntOrNull()?.let { index ->
            if (index in options.indices) return index
        }
    }

    val answerKeys = listOf(
        "correctAnswer",
        "answer",
        "rightAnswer",
        "correctOption",
        "correct_option"
    )
    answerKeys.forEach { key ->
        val rawAnswer = this[key]?.jsonPrimitiveContentOrNull()?.trim().orEmpty()
        if (rawAnswer.isBlank()) return@forEach

        rawAnswer.toIntOrNull()?.let { numericAnswer ->
            if (numericAnswer in options.indices) return numericAnswer
            val oneBasedIndex = numericAnswer - 1
            if (oneBasedIndex in options.indices) return oneBasedIndex
        }

        if (rawAnswer.length == 1) {
            val letterIndex = rawAnswer.uppercase()[0] - 'A'
            if (letterIndex in options.indices) return letterIndex
        }

        val matchedIndex = options.indexOfFirst { option ->
            option.text.equals(rawAnswer, ignoreCase = true)
        }
        if (matchedIndex >= 0) return matchedIndex
    }

    return null
}

private fun ModuleQuizUi.evaluate(selectedAnswers: Map<Int, Int>): ModuleQuizEvaluationUi {
    val results = questions.mapIndexed { index, question ->
        val selectedIndex = selectedAnswers[index]
        val correctIndex = question.correctIndex
        ModuleQuizQuestionResultUi(
            selectedIndex = selectedIndex,
            correctIndex = correctIndex,
            isCorrect = if (selectedIndex == null || correctIndex == null) {
                null
            } else {
                selectedIndex == correctIndex
            }
        )
    }

    return ModuleQuizEvaluationUi(
        totalQuestions = questions.size,
        answeredQuestions = results.count { it.selectedIndex != null },
        scoredQuestions = results.count { it.correctIndex != null },
        correctAnswers = results.count { it.isCorrect == true },
        results = results
    )
}

private fun ModuleQuizEvaluationUi.summaryText(): String {
    if (scoredQuestions == 0) {
        return "Answers submitted for $answeredQuestions of $totalQuestions questions."
    }
    return "Score: $correctAnswers / $scoredQuestions correct"
}

private fun JsonElement.jsonArrayOrNull(): JsonArray? = this as? JsonArray

private fun JsonElement.jsonPrimitiveContentOrNull(): String? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.content
}

private fun JsonElement.jsonPrimitiveIntOrNull(): Int? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.content.toIntOrNull()
}

private fun JsonElement.jsonPrimitiveBooleanOrNull(): Boolean? {
    val primitive = this as? JsonPrimitive ?: return null
    return primitive.content.lowercase().toBooleanStrictOrNull()
}

@Preview(showBackground = true)
@Composable
private fun TrainingModuleDetailPreview() {
    EncyeTheme {
        TrainingModuleDetailScreen(
            trainingTitle = "From Busy to Impactful",
            module = TrainingModuleDto(
                id = "module-1",
                name = "The ROI of Strategic Time Management",
                moduleNumber = 1,
                content = "<p>Time is a strategic asset. This module reframes productivity around value creation.</p>",
                quiz = buildJsonObject {
                    put("_id", "quiz-1")
                    put("questions", buildJsonArray {
                        add(
                            buildJsonObject {
                                put("question", "What does workplace ethics mainly mean?")
                                put("options", buildJsonArray {
                                    add(JsonPrimitive("Following orders without question"))
                                    add(JsonPrimitive("Doing your job honestly, respectfully, and fairly"))
                                    add(JsonPrimitive("Avoiding responsibility"))
                                    add(JsonPrimitive("Focusing only on results"))
                                })
                                put("correctIndex", 1)
                            }
                        )
                    })
                }
            ),
            onBack = { }
        )
    }
}
