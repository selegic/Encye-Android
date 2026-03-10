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

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    val articles: Flow<PagingData<ArticleDto>> = articleRepository.getArticles().cachedIn(viewModelScope)

    private val _article = MutableStateFlow<ArticleDto?>(null)
    val article: StateFlow<ArticleDto?> = _article

    private val _actionState = MutableStateFlow(ArticleActionUiState())
    val actionState: StateFlow<ArticleActionUiState> = _actionState.asStateFlow()

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
}
