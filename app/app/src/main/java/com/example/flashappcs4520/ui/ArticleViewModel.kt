package com.example.flashappcs4520.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashappcs4520.common.Article
import com.example.flashappcs4520.data.ArticleRepository
import com.example.flashappcs4520.data.AuthRepository
import com.example.flashappcs4520.data.BlacklistRepository
import com.example.flashappcs4520.data.CommentRepository
import com.example.flashappcs4520.data.LikeRepository
import com.example.flashappcs4520.data.SaveRepository
import com.example.flashappcs4520.data.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 1. What: Holds the state of the article feed screen (list, loading, error).
 * 2. Who:  Observed by the feed Composable; calls ArticleRepository to load data.
 * 3. When: Loads once on creation (init); can be re-triggered via loadArticles().
 */
data class ArticleFeedState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    val likeCounts: Map<Long, Int> = emptyMap(),
    val likedByMe: Set<Long> = emptySet(),
    val savedByMe: Set<Long> = emptySet(),
    val comments: Map<Long, List<CommentUi>> = emptyMap(),
    val commentCounts: Map<Long, Int> = emptyMap(),
    val error: String? = null,
)

class ArticleViewModel(
    private val repository: ArticleRepository = ArticleRepository(),
    private val likeRepository: LikeRepository = LikeRepository(),
    private val saveRepository: SaveRepository = SaveRepository(),
    private val commentRepository: CommentRepository = CommentRepository(),
    private val userRepository: UserRepository = UserRepository(),
    private val authRepository: AuthRepository = AuthRepository(),
    private val blacklistRepository: BlacklistRepository = BlacklistRepository(),
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
                // fetch articles, blacklist, and user interests in parallel
                val articlesDeferred = async { repository.fetchArticles(count = 50) }
                val blacklistDeferred = async { blacklistRepository.fetchBlacklistedIds() }
                val interestsDeferred = async { userRepository.fetchCurrentUser()?.interests ?: emptyList() }

                val articles = articlesDeferred.await()
                val blacklisted = blacklistDeferred.await()
                val interests = interestsDeferred.await()

                // tier 1: matches interests, not blacklisted
                // tier 2: no interest match, not blacklisted
                // tier 3: matches interests, blacklisted
                // tier 4: no interest match, blacklisted
                val tier1 = articles.filter { it.id !in blacklisted && it.section in interests }
                val tier2 = articles.filter { it.id !in blacklisted && it.section !in interests }
                val tier3 = articles.filter { it.id in blacklisted && it.section in interests }
                val tier4 = articles.filter { it.id in blacklisted && it.section !in interests }

                val sorted = tier1 + tier2 + tier3 + tier4

                _state.update { it.copy(isLoading = false, articles = sorted) }
                loadLikes(sorted)
                loadSaves()
                loadCommentCounts(sorted)
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to load articles")
                }
            }
        }
    }

    /** Adds an article to the blacklist. Called on like, save, comment, share, or expand. */
    fun blacklistArticle(articleId: Long) {
        viewModelScope.launch {
            try {
                blacklistRepository.addToBlacklist(articleId)
            } catch (e: Exception) {
                Log.e("Blacklist", "failed to add $articleId: ${e.message}", e)
            }
        }
    }

    /** Loads like counts + my-likes for the given articles. Best-effort: a failure here
     *  leaves the feed usable with zeroed-out like state rather than erroring the screen. */
    private suspend fun loadLikes(articles: List<Article>) {
        val myId = authRepository.currentUser()?.id
        try {
            val likes = likeRepository.fetchLikesFor(articles.mapNotNull { it.id })
            val counts = likes.mapNotNull { it.articleId }
                .groupingBy { it }
                .eachCount()
            val mine = likes
                .filter { it.userId != null && it.userId == myId }
                .mapNotNull { it.articleId }
                .toSet()
            _state.update { it.copy(likeCounts = counts, likedByMe = mine) }
        } catch (_: Exception) {
            // leave like state empty; the feed itself already rendered.
        }
    }

    /** Optimistically flips a like, then persists. Reverts the UI if the write fails. */
    fun toggleLike(articleId: Long) {
        val wasLiked = _state.value.likedByMe.contains(articleId)
        applyLike(articleId, liked = !wasLiked)
        viewModelScope.launch {
            try {
                if (wasLiked) likeRepository.unlikeArticle(articleId)
                else {
                    likeRepository.likeArticle(articleId)
                    blacklistArticle(articleId)
                }
            } catch (e: Exception) {
                applyLike(articleId, liked = wasLiked)   // revert
            }
        }
    }

    /** Sets the liked/unliked state for one article and adjusts its count accordingly. */
    private fun applyLike(articleId: Long, liked: Boolean) {
        _state.update { s ->
            if (s.likedByMe.contains(articleId) == liked) return@update s
            val counts = s.likeCounts.toMutableMap()
            val current = counts[articleId] ?: 0
            counts[articleId] = (if (liked) current + 1 else current - 1).coerceAtLeast(0)
            val mine = s.likedByMe.toMutableSet().apply { if (liked) add(articleId) else remove(articleId) }
            s.copy(likeCounts = counts, likedByMe = mine)
        }
    }

    /** Loads which articles the current user has saved. Best-effort, like loadLikes. */
    private suspend fun loadSaves() {
        try {
            val saved = saveRepository.fetchMySavedArticleIds().toSet()
            _state.update { it.copy(savedByMe = saved) }
        } catch (_: Exception) {
            // leave save state empty; the feed itself already rendered.
        }
    }

    /** Optimistically flips a save, then persists. Reverts the UI if the write fails. */
    fun toggleSave(articleId: Long) {
        val wasSaved = _state.value.savedByMe.contains(articleId)
        applySave(articleId, saved = !wasSaved)
        viewModelScope.launch {
            try {
                if (wasSaved) saveRepository.unsaveArticle(articleId)
                else {
                    saveRepository.saveArticle(articleId)
                    blacklistArticle(articleId)
                }
            } catch (e: Exception) {
                applySave(articleId, saved = wasSaved)   // revert
            }
        }
    }

    /** Sets the saved/unsaved state for one article. */
    private fun applySave(articleId: Long, saved: Boolean) {
        _state.update { s ->
            if (s.savedByMe.contains(articleId) == saved) return@update s
            val mine = s.savedByMe.toMutableSet().apply { if (saved) add(articleId) else remove(articleId) }
            s.copy(savedByMe = mine)
        }
    }

    /** Fetches an article's comments + author profiles, maps them to a UI tree, and stores it. */
    fun loadComments(articleId: Long) {
        viewModelScope.launch { refreshComments(articleId) }
    }

    /** Posts a comment (or a reply, if [parentId] is set), then refreshes the thread. */
    fun addComment(articleId: Long, text: String, parentId: String? = null) {
        viewModelScope.launch {
            try {
                commentRepository.addComment(articleId, text, parentId)
                blacklistArticle(articleId)
                refreshComments(articleId)
            } catch (e: Exception) {
                Log.e("Comments", "Failed to post comment on article $articleId", e)
            }
        }
    }

    /** Loads an article's comments + profiles, maps to the UI tree, and updates state.
     *  Also refreshes the badge count from the fetched rows so posting updates it immediately. */
    private suspend fun refreshComments(articleId: Long) {
        try {
            val rows = commentRepository.fetchComments(articleId)
            val profiles = userRepository.fetchProfiles(rows.mapNotNull { it.userId })
            _state.update {
                it.copy(
                    comments = it.comments + (articleId to buildCommentUiTree(rows, profiles)),
                    commentCounts = it.commentCounts + (articleId to rows.size),
                )
            }
        } catch (e: Exception) {
            Log.e("Comments", "Failed to load comments for article $articleId", e)
        }
    }

    /** Loads total comment counts for the feed's articles (for the badge), in one query. */
    private suspend fun loadCommentCounts(articles: List<Article>) {
        try {
            val counts = commentRepository.fetchCommentCounts(articles.mapNotNull { it.id })
            _state.update { it.copy(commentCounts = counts) }
        } catch (_: Exception) {
            // leave counts empty; the feed already rendered.
        }
    }
}