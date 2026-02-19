package com.selegic.encye.article

import com.selegic.encye.data.remote.dto.ArticleDto

data class ArticleUiState(
    val articles: List<ArticleDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
