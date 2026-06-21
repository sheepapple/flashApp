package com.example.flashappcs4520.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    hasUnread: Boolean = false,
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
) {
    val state by articleViewModel.state.collectAsStateWithLifecycle()
    FeedContent(
        state = state,
        avatarUrl = avatarUrl,
        hasUnread = hasUnread,
        onProfileClick = onProfileClick,
        onBackClick = onLogoutClick,
        onLikeToggle = { article -> article.id?.let { articleViewModel.toggleLike(it) } },
        onSaveToggle = { article -> article.id?.let { articleViewModel.toggleSave(it) } },
        onSendComment = { article, text, parentId ->
            article.id?.let { articleViewModel.addComment(it, text, parentId) }
        },
        onCommentOpen = { article -> article.id?.let { articleViewModel.loadComments(it) } },
        onExpand = { article -> article.id?.let { articleViewModel.blacklistArticle(it) } },
        onShare = { article -> article.id?.let { articleViewModel.blacklistArticle(it) } },
    )
}

/** Stateless feed UI — easy to preview with mock state. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    state: ArticleFeedState,
    avatarUrl: String? = null,
    hasUnread: Boolean = false,
    onArticleClick: (Article) -> Unit = {},
    onBackClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLikeToggle: (Article) -> Unit = {},
    onSaveToggle: (Article) -> Unit = {},
    onSendComment: (Article, String, String?) -> Unit = { _, _, _ -> },
    onCommentOpen: (Article) -> Unit = {},
    onExpand: (Article) -> Unit = {},
    onShare: (Article) -> Unit = {},
) {
    // Confirm before logging the user out (returns to the home/login screen).
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out?") },
            text = { Text("You will be returned to the home screen") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onBackClick()
                }) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { showLogoutDialog = true },
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
                    // Profile avatar + unread dot. NOT an IconButton: IconButton clips its
                    // content to a circle, which would cut off a corner badge. We clip only the
                    // avatar itself and overlay the unread dot in the (unclipped) corner so users
                    // know to head to Profile to view Notifications.
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable(onClick = onProfileClick),
                            contentAlignment = Alignment.Center,
                        ) {
                            // take profile url as change now
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = if (hasUnread) "Profile, unread notifications" else "Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = if (hasUnread) "Profile, unread notifications" else "Profile",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                        if (hasUnread) {
                            Badge(modifier = Modifier.align(Alignment.TopEnd).size(10.dp))
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
                    ArticleList(
                        state = state,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        onArticleClick = onArticleClick,
                        onLikeToggle = onLikeToggle,
                        onSaveToggle = onSaveToggle,
                        onSendComment = onSendComment,
                        onCommentOpen = onCommentOpen,
                        onExpand = onExpand,
                        onShare = onShare,
                    )
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