package com.selegic.encye.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selegic.encye.data.remote.dto.CategoryTreeDto
import com.selegic.encye.data.repository.ArticleRepository
import com.selegic.encye.data.repository.CategoryRepository
import com.selegic.encye.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ArticleComposerUiState(
    val title: String = "",
    val subtitle: String = "",
    val availableCategories: List<CategoryOption> = emptyList(),
    val selectedCategoryIds: List<String> = emptyList(),
    val isLoadingCategories: Boolean = false,
    val section: String = "",
    val tags: String = "",
    val htmlDescription: String = "",
    val coverImageFile: File? = null,
    val coverImageName: String? = null,
    val isPublished: Boolean = false,
    val isSubmitting: Boolean = false,
    val isUploadingInlineImage: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val insertedInlineImageUrl: String? = null
)

@HiltViewModel
class ArticleComposerViewModel @Inject constructor(
    private val articleRepository: ArticleRepository,
    private val categoryRepository: CategoryRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArticleComposerUiState())
    val uiState: StateFlow<ArticleComposerUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun updateTitle(value: String) = _uiState.update { it.copy(title = value, errorMessage = null) }
    fun updateSubtitle(value: String) = _uiState.update { it.copy(subtitle = value, errorMessage = null) }
    fun updateSection(value: String) = _uiState.update { it.copy(section = value, errorMessage = null) }
    fun updateTags(value: String) = _uiState.update { it.copy(tags = value, errorMessage = null) }
    fun updateHtmlDescription(value: String) = _uiState.update {
        it.copy(htmlDescription = normalizeEditorHtml(value), errorMessage = null)
    }

    fun updatePublished(value: Boolean) = _uiState.update { it.copy(isPublished = value) }

    fun toggleCategorySelection(categoryId: String) = _uiState.update { state ->
        val updatedIds = if (categoryId in state.selectedCategoryIds) {
            state.selectedCategoryIds - categoryId
        } else {
            state.selectedCategoryIds + categoryId
        }
        state.copy(selectedCategoryIds = updatedIds, errorMessage = null)
    }

    fun setCoverImage(file: File?, displayName: String?) = _uiState.update {
        it.copy(coverImageFile = file, coverImageName = displayName)
    }

    fun clearInsertedInlineImageSignal() = _uiState.update { it.copy(insertedInlineImageUrl = null) }
    fun clearSuccessMessage() = _uiState.update { it.copy(successMessage = null) }
    fun clearErrorMessage() = _uiState.update { it.copy(errorMessage = null) }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCategories = true, errorMessage = null) }

            runCatching {
                categoryRepository.getAllCategories()
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        availableCategories = response.data.orEmpty().flattenCategoryTree(),
                        errorMessage = if (response.success) null else response.msg
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingCategories = false,
                        errorMessage = error.message ?: "Unable to load categories"
                    )
                }
            }
        }
    }

    fun uploadInlineImage(file: File) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingInlineImage = true,
                    errorMessage = null,
                    insertedInlineImageUrl = null
                )
            }

            runCatching {
                imageRepository.uploadImage(file, category = "article")
            }.onSuccess { response ->
                val imageUrl = response.data?.url
                _uiState.update {
                    it.copy(
                        isUploadingInlineImage = false,
                        insertedInlineImageUrl = imageUrl,
                        errorMessage = if (response.success && !imageUrl.isNullOrBlank()) null else response.msg
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isUploadingInlineImage = false,
                        errorMessage = error.message ?: "Unable to upload image"
                    )
                }
            }
        }
    }

    fun submit(onSuccess: (() -> Unit)? = null) {
        val currentState = _uiState.value
        val normalizedHtml = normalizeEditorHtml(currentState.htmlDescription)

        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Title is required") }
            return
        }

        if (normalizedHtml.htmlToPlainText().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Article body is required") }
            return
        }

        if (currentState.selectedCategoryIds.size > 1) {
            _uiState.update {
                it.copy(errorMessage = "The article API currently supports only one category. Please keep one selected.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    successMessage = null,
                    htmlDescription = normalizedHtml
                )
            }

            runCatching {
                articleRepository.createArticle(
                    title = currentState.title.trim(),
                    description = normalizedHtml,
                    image = currentState.coverImageFile,
                    subtitle = currentState.subtitle.trim().ifBlank { null },
                    category = currentState.selectedCategoryIds.firstOrNull(),
                    section = currentState.section.trim().ifBlank { null },
                    tags = currentState.tags.toTagList(),
                    isPublished = currentState.isPublished
                )
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = if (response.success) response.msg.ifBlank { "Article created" } else null,
                        errorMessage = if (response.success) null else response.msg
                    )
                }

                if (response.success) {
                    onSuccess?.invoke()
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = error.message ?: "Unable to create article"
                    )
                }
            }
        }
    }
}

data class CategoryOption(
    val id: String,
    val name: String,
    val depth: Int
)

private val MultipleBreakRegex = Regex("(?i)(<p>(\\s|&nbsp;|<br\\s*/?>)*</p>\\s*)+$")
private val ScriptStyleRegex = Regex("(?is)<(script|style)[^>]*>.*?</\\1>")
private val HtmlTagRegex = Regex("<[^>]*>")
private val HtmlWhitespaceRegex = Regex("\\s+")

fun normalizeEditorHtml(rawHtml: String): String {
    return rawHtml
        .replace(ScriptStyleRegex, "")
        .replace(Regex("(?i)</?(html|head|body)[^>]*>"), "")
        .replace(Regex("(?i)<p><br\\s*/?></p>"), "")
        .replace(MultipleBreakRegex, "")
        .trim()
}

private fun String.toTagList(): List<String>? {
    return split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .takeIf { it.isNotEmpty() }
}

fun String.htmlToPlainText(): String {
    return replace(HtmlTagRegex, " ")
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace(HtmlWhitespaceRegex, " ")
        .trim()
}

private fun List<CategoryTreeDto>.flattenCategoryTree(depth: Int = 0): List<CategoryOption> {
    return flatMap { category ->
        listOf(
            CategoryOption(
                id = category.id,
                name = category.name,
                depth = depth
            )
        ) + category.subcategories.flattenCategoryTree(depth + 1)
    }
}
