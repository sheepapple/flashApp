package com.example.flashappcs4520.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flashappcs4520.R
import com.example.flashappcs4520.common.Article
import com.example.flashappcs4520.common.Comment
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme
import java.net.URI

@Composable
fun ArticleCard(
    article: Article,
    liked: Boolean = false,
    likeCount: Int = 0,
    onLikeToggle: () -> Unit = {},
    saved: Boolean = false,
    onSaveToggle: () -> Unit = {},
    onClick: () -> Unit = {},
    onComment: () -> Unit = {},
    onShare: () -> Unit = {},
    comments: List<Comment> = emptyList(),
    onSendComment: (String) -> Unit = {},
    initiallyExpanded: Boolean = false,
    initiallyShowComments: Boolean = false,
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    var showComments by remember { mutableStateOf(initiallyShowComments) }
    var showShareSheet by remember { mutableStateOf(false) }

    Card(
        onClick = {
            expanded = !expanded
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),   // smooth grow/shrink on expand
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // Header: title + "source · date" meta line (padded).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Text(
                    text = article.webTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = metaLine(article),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Image: cropped to a fixed height when collapsed, full (uncropped) when expanded.
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.webTitle,
                contentScale = if (expanded) ContentScale.FillWidth else ContentScale.Crop,
                modifier = if (expanded) {
                    Modifier.fillMaxWidth()
                } else {
                    Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                },
            )

            // Summary: truncated when collapsed, full when expanded.
            Text(
                text = article.summary ?: "No summary available.",
                fontSize = 16.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )

            // Action buttons appear only when expanded.
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                ActionRow(
                    liked = liked,
                    likeCount = likeCount,
                    onLikeToggle = onLikeToggle,
                    onComment = {
                        showComments = !showComments
                        onComment()
                    },
                    onShare = {
                        showShareSheet = true
                        onShare()
                    },
                    saved = saved,
                    onSaveToggle = onSaveToggle,
                )

                // Comments section appears when the comment button is toggled on.
                if (showComments) {
                    CommentsSection(
                        comments = comments,
                        onSendComment = onSendComment,
                    )
                }
            }
        }
    }

    if (showShareSheet) {
        ShareSheet(
            article = article,
            onDismiss = { showShareSheet = false },
        )
    }
}

@Composable
private fun ActionRow(
    liked: Boolean,
    likeCount: Int,
    onLikeToggle: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    saved: Boolean,
    onSaveToggle: () -> Unit,
) {
    // Like and save are both persisted; their state is owned by the ViewModel.
    val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Like button + total count.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onLikeToggle) {
                Icon(
                    painter = painterResource(if (liked) R.drawable.ic_liked else R.drawable.ic_like),
                    contentDescription = "Like",
                    // ic_liked is already red; leave it untinted so its own color shows.
                    tint = if (liked) Color.Unspecified else iconTint,
                )
            }
            if (likeCount > 0) {
                Text(
                    text = "$likeCount",
                    fontSize = 14.sp,
                    color = iconTint,
                )
            }
        }
        IconButton(onClick = onComment) {
            Icon(painterResource(R.drawable.ic_comment), contentDescription = "Comment", tint = iconTint)
        }
        IconButton(onClick = onShare) {
            Icon(painterResource(R.drawable.ic_share), contentDescription = "Share", tint = iconTint)
        }
        IconButton(onClick = onSaveToggle) {
            Icon(
                painter = painterResource(if (saved) R.drawable.ic_bookmarked else R.drawable.ic_bookmark),
                contentDescription = "Save",
                // ic_bookmarked is already blue; leave it untinted so its own color shows.
                tint = if (saved) Color.Unspecified else iconTint,
            )
        }
    }
}

@Composable
private fun CommentsSection(
    comments: List<Comment>,
    onSendComment: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

        if (comments.isEmpty()) {
            Text(
                text = "No comments yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
        } else {
            comments.forEach { comment ->
                Text(
                    text = comment.text,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }

        // Message box + send button (no-op for now; wired to onSendComment).
        var draft by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text("Add a comment…") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(percent = 50),   // capsule
            )
            IconButton(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSendComment(draft.trim())
                        draft = ""
                    }
                },
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_reply),
                        contentDescription = "Send comment",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/** Builds a "source · date" line, deriving the source from the article URL's host. */
private fun metaLine(article: Article): String {
    val source = runCatching { URI(article.webUrl).host?.removePrefix("www.") }
        .getOrNull() ?: "News"
    return "$source · ${article.formattedDate()} · ${article.section}"
}

@Preview(showBackground = true, name = "Collapsed")
@Composable
fun ArticleCardPreview() {
    val mockArticle = Article(
        webTitle = "World Cup football and T20 cricket galore, plus F1 in Barcelona – follow with us ",
        summary = "A research team unveiled a compact model that runs entirely on-device, no cloud connection required.",
        imageUrl = "https://media.guim.co.uk/fdc906e59f18b1f42ac9263c76f7c5da318bf8ee/0_0_5000_4000/500.jpg",
        webUrl = "https://www.theguardian.com/sport/2026/jun/12/world-cup-football-and-t20-cricket-galore-plus-f1-in-barcelona-follow-with-us",
        publishedAt = "2026-06-12 16:41:25+00"
    )
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleCard(article = mockArticle)
        }
    }
}

@Preview(showBackground = true, name = "Expanded + Comments")
@Composable
fun ArticleCardExpandedPreview() {
    val mockArticle = Article(
        webTitle = "On-device AI model runs without the cloud",
        summary = "A research team unveiled a compact model that runs entirely on-device, no cloud connection required. The breakthrough could reshape how mobile apps handle private data.",
        imageUrl = "https://media.guim.co.uk/fdc906e59f18b1f42ac9263c76f7c5da318bf8ee/0_0_5000_4000/500.jpg",
        webUrl = "https://www.theverge.com/2026/jun/12/on-device-ai",
        publishedAt = "2026-06-12 16:41:25+00"
    )
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ArticleCard(
                article = mockArticle,
                initiallyExpanded = true,
                initiallyShowComments = true,
            )
        }
    }
}