package com.example.flashappcs4520.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

/**
 * The user's saved (bookmarked) articles. A separate list from the feed — it uses its own
 * keyed [ArticleViewModel] instance so loading saves never clobbers the feed's list — but
 * reuses [ArticleList], so cards behave exactly like the feed (tap to expand, like, comment…).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    onBackClick: () -> Unit,
    // A distinct instance — keyed so it doesn't share the activity-scoped feed ViewModel, and
    // built with autoLoadFeed = false so its init doesn't fetch the feed over the saved list.
    viewModel: ArticleViewModel = viewModel(
        key = "saved",
        factory = viewModelFactory { initializer { ArticleViewModel(autoLoadFeed = false) } },
    ),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadSavedArticles() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                title = {
                    Text(text = "Saved", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.secondaryFixed,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Message(
                        text = state.error ?: "Something went wrong.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                state.articles.isEmpty() -> {
                    Message(
                        text = "No saved articles yet.",
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    ArticleList(
                        state = state,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        onLikeToggle = { article -> article.id?.let { viewModel.toggleLike(it) } },
                        onSaveToggle = { article -> article.id?.let { viewModel.toggleSave(it) } },
                        onSendComment = { article, text, parentId ->
                            article.id?.let { viewModel.addComment(it, text, parentId) }
                        },
                        onCommentOpen = { article -> article.id?.let { viewModel.loadComments(it) } },
                        onExpand = { article -> article.id?.let { viewModel.blacklistArticle(it) } },
                        onShare = { article -> article.id?.let { viewModel.blacklistArticle(it) } },
                    )
                }
            }
        }
    }
}

/** Centered placeholder message (mirrors the feed's empty/error text style). */
@Composable
private fun Message(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 32.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
}