package com.example.flashappcs4520.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flashappcs4520.R
import com.example.flashappcs4520.common.Article
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
    commentCount: Int = 0,
    onShare: () -> Unit = {},
    comments: List<CommentUi> = emptyList(),
    onSendComment: (text: String, parentId: String?) -> Unit = { _, _ -> },
    initiallyExpanded: Boolean = false,
    initiallyShowComments: Boolean = false,
    onExpand: () -> Unit = {},
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    var showShareSheet by remember { mutableStateOf(false) }
    // Whether the comments bottom sheet (scrolling thread + pinned input) is open.
    var showCommentsSheet by remember { mutableStateOf(initiallyShowComments) }
    // Confirmation dialog before leaving the app for the publisher's site.
    var showOpenDialog by remember { mutableStateOf(false) }

    Card(
        onClick = {
            if (!expanded) onExpand()
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
                // Full-width pill linking out to the publisher's site (Guardian).
                Button(
                    onClick = { showOpenDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50),
                ) {
                    Text(text = "Read the full article")
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 1.dp,
                    modifier = Modifier.padding(top = 16.dp),
                )
                ActionRow(
                    liked = liked,
                    likeCount = likeCount,
                    onLikeToggle = onLikeToggle,
                    onComment = {
                        showCommentsSheet = true
                        onComment()
                    },
                    commentCount = commentCount,
                    onShare = {
                        showShareSheet = true
                        onShare()
                    },
                    saved = saved,
                    onSaveToggle = onSaveToggle,
                )
            }
        }
    }

    if (showShareSheet) {
        ShareSheet(
            article = article,
            onDismiss = { showShareSheet = false },
        )
    }

    if (showCommentsSheet) {
        CommentsSheet(
            comments = comments,
            onDismiss = { showCommentsSheet = false },
            onSendComment = onSendComment,
        )
    }

    if (showOpenDialog) {
        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text("Leave Flash?") },
            text = { Text("This will open the full article on the publisher's site in your browser.") },
            confirmButton = {
                TextButton(onClick = {
                    showOpenDialog = false
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.webUrl))
                    runCatching { context.startActivity(intent) }
                }) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ActionRow(
    liked: Boolean,
    likeCount: Int,
    onLikeToggle: () -> Unit,
    onComment: () -> Unit,
    commentCount: Int,
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
        // Comment button + total count.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onComment) {
                Icon(painterResource(R.drawable.ic_comment), contentDescription = "Comment", tint = iconTint)
            }
            if (commentCount > 0) {
                Text(
                    text = "$commentCount",
                    fontSize = 14.sp,
                    color = iconTint,
                )
            }
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