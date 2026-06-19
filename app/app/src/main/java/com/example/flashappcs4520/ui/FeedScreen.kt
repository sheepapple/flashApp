package com.example.flashappcs4520.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.flashappcs4520.R
import com.example.flashappcs4520.common.Article
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme

/** Stateful entry point: pulls state from the ViewModel and hands it to FeedContent. */
@Composable
fun FeedScreen(
    articleViewModel: ArticleViewModel,
    avatarUrl: String? = null,
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val state by articleViewModel.state.collectAsStateWithLifecycle()
    FeedContent(
        state = state,
        avatarUrl = avatarUrl,
        onProfileClick = onProfileClick,
        onBackClick = onLogoutClick,
        onLikeToggle = { article -> article.id?.let { articleViewModel.toggleLike(it) } },
        onSaveToggle = { article -> article.id?.let { articleViewModel.toggleSave(it) } },
        onSendComment = { article, text, parentId ->
            article.id?.let { articleViewModel.addComment(it, text, parentId) }
        },
        onCommentOpen = { article -> article.id?.let { articleViewModel.loadComments(it) } },
    )
}

/** Stateless feed UI — easy to preview with mock state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    state: ArticleFeedState,
    avatarUrl: String? = null,
    onArticleClick: (Article) -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLikeToggle: (Article) -> Unit = {},
    onSaveToggle: (Article) -> Unit = {},
    onSendComment: (Article, String, String?) -> Unit = { _, _, _ -> },
    onCommentOpen: (Article) -> Unit = {},
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.ic_flash_logo),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Flash",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            // take profile url as change now
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
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
                .padding(innerPadding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.error != null -> {
                    Message(
                        text = state.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.articles.isEmpty() -> {
                    Message(
                        text = "No articles yet.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.articles,
                            key = { article -> article.webUrl }
                        ) { article ->
                            ArticleCard(
                                article = article,
                                liked = article.id != null && article.id in state.likedByMe,
                                likeCount = article.id?.let { state.likeCounts[it] } ?: 0,
                                onLikeToggle = { onLikeToggle(article) },
                                saved = article.id != null && article.id in state.savedByMe,
                                onSaveToggle = { onSaveToggle(article) },
                                comments = article.id?.let { state.comments[it] } ?: emptyList(),
                                onComment = { onCommentOpen(article) },
                                onSendComment = { text, parentId -> onSendComment(article, text, parentId) },
                                onClick = { onArticleClick(article) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 32.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
}

@Preview(showBackground = true)
@Composable
fun FeedContentPreview() {
    val mockState = ArticleFeedState(
        articles = listOf(
            Article(
                webTitle = "On-device AI model runs without the cloud",
                summary = "A research team unveiled a compact model that runs entirely on-device.",
                imageUrl = "https://media.guim.co.uk/fdc906e59f18b1f42ac9263c76f7c5da318bf8ee/0_0_5000_4000/500.jpg",
                webUrl = "https://www.theverge.com/2026/jun/12/on-device-ai",
                publishedAt = "2026-06-12 16:41:25+00",
            ),
            Article(
                webTitle = "Markets rally as inflation cools",
                summary = null,
                imageUrl = "https://media.guim.co.uk/fdc906e59f18b1f42ac9263c76f7c5da318bf8ee/0_0_5000_4000/500.jpg",
                webUrl = "https://www.theguardian.com/business/2026/jun/11/markets",
                publishedAt = "2026-06-11 09:15:00+00",
            ),
        )
    )
    FlashAppCS4520Theme {
        FeedContent(state = mockState)
    }
}