package com.example.flashappcs4520.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashappcs4520.common.Article
import com.example.flashappcs4520.data.ArticleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 1. What: Holds the state of the article feed screen (list, loading, error).
 * 2. Who:  Observed by the feed Composable; calls ArticleRepository to load data.
 * 3. When: Loads once on creation (init); can be re-triggered via loadArticles().
 *
 * The feed runs off a single immutable state object so the UI just reads one
 * value and renders the matching state (spinner / list / error message).
 */
data class ArticleFeedState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val error: String? = null,
)

class ArticleViewModel(
    private val repository: ArticleRepository = ArticleRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleFeedState())
    val state: StateFlow<ArticleFeedState> = _state.asStateFlow()

    init {
        loadArticles()
    }

    fun loadArticles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val articles = repository.fetchArticles()
                _state.update { it.copy(isLoading = false, articles = articles) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load articles")
                }
            }
        }
    }
}