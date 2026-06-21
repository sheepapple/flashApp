package com.example.flashappcs4520.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flashappcs4520.common.Article

/**
 * The scrolling list of [ArticleCard]s, shared by the feed and the Saved screen.
 *
 * The article whose id equals [ArticleFeedState.focusedArticleId] (set by a deep link, e.g. a
 * tapped notification) starts expanded, and the list scrolls to the top to reveal it — the
 * ViewModel always prepends the focused article at index 0.
 */
@Composable
fun ArticleList(
    state: ArticleFeedState,
    modifier: Modifier = Modifier,
    onArticleClick: (Article) -> Unit = {},
    onLikeToggle: (Article) -> Unit = {},
    onSaveToggle: (Article) -> Unit = {},
    onSendComment: (Article, String, String?) -> Unit = { _, _, _ -> },
    onCommentOpen: (Article) -> Unit = {},
    onExpand: (Article) -> Unit = {},
    onShare: (Article) -> Unit = {},
    onLoadMore: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    LaunchedEffect(state.focusedArticleId) {
        if (state.focusedArticleId != null) listState.animateScrollToItem(0)
    }

    // Page in more once the last item scrolls into view. We read totalItemsCount from layoutInfo
    // (always current) rather than closing over state.articles.size (would go stale). The
    // ViewModel guards re-entry and the end-of-list case, so a few extra calls here are harmless.
    val reachedBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()
            last != null && last.index >= info.totalItemsCount - 1
        }
    }
    LaunchedEffect(reachedBottom) {
        if (reachedBottom && state.articles.isNotEmpty()) onLoadMore()
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = state.articles,
            key = { article -> article.webUrl },
        ) { article ->
            val isFocused = article.id != null && article.id == state.focusedArticleId
            // The reply deep link's target comment, only for the focused article.
            val highlightedCommentId = if (isFocused) state.focusedCommentId else null
            ArticleCard(
                article = article,
                liked = article.id != null && article.id in state.likedByMe,
                likeCount = article.id?.let { state.likeCounts[it] } ?: 0,
                onLikeToggle = { onLikeToggle(article) },
                saved = article.id != null && article.id in state.savedByMe,
                onSaveToggle = { onSaveToggle(article) },
                comments = article.id?.let { state.comments[it] } ?: emptyList(),
                onComment = { onCommentOpen(article) },
                commentCount = article.id?.let { state.commentCounts[it] } ?: 0,
                onSendComment = { text, parentId -> onSendComment(article, text, parentId) },
                onClick = { onArticleClick(article) },
                onExpand = { onExpand(article) },
                onShare = { onShare(article) },
                initiallyExpanded = isFocused,
                initiallyShowComments = highlightedCommentId != null,
                highlightedCommentId = highlightedCommentId,
                highlighted = isFocused,
            )
        }

        if (state.isLoadingMore) {
            item(key = "loading-more") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}