package com.selegic.encye.article

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.selegic.encye.data.remote.dto.ArticleDto
import com.selegic.encye.data.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArticleActionUiState(
    val isDeleting: Boolean = false,
    val deleteSucceeded: Boolean = false,
    val errorMessage: String? = null
)

data class FilteredArticlesUiState(
    val selectedCategory: String = "All",
    val articles: List<ArticleDto> = emptyList(),
    val currentPage: Int = 0,
    val isLoading: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {
    companion object {
        private const val FILTER_PAGE_SIZE = 10
    }

    val articles: Flow<PagingData<ArticleDto>> = articleRepository.getArticles().cachedIn(viewModelScope)

    private val _article = MutableStateFlow<ArticleDto?>(null)
    val article: StateFlow<ArticleDto?> = _article

    private val _actionState = MutableStateFlow(ArticleActionUiState())
    val actionState: StateFlow<ArticleActionUiState> = _actionState.asStateFlow()

    private val _filteredArticlesState = MutableStateFlow(FilteredArticlesUiState())
    val filteredArticlesState: StateFlow<FilteredArticlesUiState> = _filteredArticlesState.asStateFlow()

    fun setArticle(articleDto: ArticleDto){
        _article.value = articleDto
    }

    fun fetchArticleById(id: String) {
        viewModelScope.launch {
            try {
                val response = articleRepository.getArticleById(id)
                if (response.success) {
                    _article.value = response.data
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteArticle(id: String) {
        if (id.isBlank()) return

        viewModelScope.launch {
            _actionState.update {
                it.copy(isDeleting = true, deleteSucceeded = false, errorMessage = null)
            }

            runCatching {
                articleRepository.deleteArticle(id)
            }.onSuccess { response ->
                _actionState.update {
                    it.copy(
                        isDeleting = false,
                        deleteSucceeded = response.success,
                        errorMessage = if (response.success) null else response.msg
                    )
                }
            }.onFailure { error ->
                _actionState.update {
                    it.copy(
                        isDeleting = false,
                        deleteSucceeded = false,
                        errorMessage = error.message ?: "Unable to delete article"
                    )
                }
            }
        }
    }

    fun clearActionMessage() {
        _actionState.update {
            it.copy(errorMessage = null, deleteSucceeded = false)
        }
    }

    fun selectCategory(category: String) {
        if (category == "All") {
            _filteredArticlesState.value = FilteredArticlesUiState(selectedCategory = category)
            return
        }

        if (_filteredArticlesState.value.selectedCategory == category &&
            (_filteredArticlesState.value.articles.isNotEmpty() || _filteredArticlesState.value.isLoading)
        ) {
            return
        }

        _filteredArticlesState.value = FilteredArticlesUiState(
            selectedCategory = category,
            errorMessage = null
        )
        loadMoreFilteredArticles(reset = true)
    }

    fun loadMoreFilteredArticles(reset: Boolean = false) {
        val state = _filteredArticlesState.value
        if (state.selectedCategory == "All" || (state.isLoading && !reset) || (!state.hasMore && !reset)) {
            return
        }

        val requestedCategory = state.selectedCategory
        val nextPage = if (reset) 1 else state.currentPage + 1
        viewModelScope.launch {
            _filteredArticlesState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            runCatching {
                articleRepository.getAllArticles(page = nextPage, limit = FILTER_PAGE_SIZE)
            }.onSuccess { response ->
                val responseArticles = response.data.orEmpty()
                val matchedArticles = responseArticles.filter { article ->
                    article.autoCategory?.primary?.name.equals(requestedCategory, ignoreCase = true)
                }
                val latestState = _filteredArticlesState.value
                if (latestState.selectedCategory != requestedCategory) {
                    return@onSuccess
                }

                val mergedArticles = if (reset) {
                    matchedArticles
                } else {
                    (latestState.articles + matchedArticles).distinctBy { it.id }
                }

                _filteredArticlesState.update {
                    it.copy(
                        articles = mergedArticles,
                        currentPage = response.currentPage ?: nextPage,
                        isLoading = false,
                        hasMore = response.hasMore ?: (responseArticles.size >= FILTER_PAGE_SIZE),
                        errorMessage = if (response.success) null else response.msg.ifBlank { "Unable to load articles" }
                    )
                }
            }.onFailure { error ->
                _filteredArticlesState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unable to load articles"
                    )
                }
            }
        }
    }
}
