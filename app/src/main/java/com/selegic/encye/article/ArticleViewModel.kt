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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    val articles: Flow<PagingData<ArticleDto>> = articleRepository.getArticles().cachedIn(viewModelScope)

    private val _article = MutableStateFlow<ArticleDto?>(null)
    val article: StateFlow<ArticleDto?> = _article

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
}
