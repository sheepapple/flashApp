package com.example.flashappcs4520.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flashappcs4520.R
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme

/**
 * UI model for one comment in a thread. Decoupled from the DB `Comment` for now: it carries
 * exactly what the UI shows (author, avatar, when, text) plus its nested [replies]. When we wire
 * this up, we'll build this tree from the flat parentID list returned by the database.
 */
data class CommentUi(
    val id: String,
    val username: String,
    val avatarUrl: String? = null,
    val timestamp: String,
    val text: String,
    val replies: List<CommentUi> = emptyList(),
)

/** The full comments thread for an article: a list of top-level comments, each expandable. */
@Composable
fun CommentsSection(
    comments: List<CommentUi>,
    modifier: Modifier = Modifier,
    onReply: (CommentUi) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (comments.isEmpty()) {
            Text(
                text = "No comments yet",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
            )
        } else {
            comments.forEach { comment ->
                CommentItem(comment = comment, onReply = onReply)
            }
        }
    }
}

/**
 * One comment plus its (collapsible) replies. Recursive: each reply is itself a [CommentItem],
 * so the parentID hierarchy renders to any depth. Replies start hidden behind a "show replies"
 * toggle.
 */
@Composable
private fun CommentItem(
    comment: CommentUi,
    onReply: (CommentUi) -> Unit,
) {
    var repliesShown by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        CommentAvatar(username = comment.username, avatarUrl = comment.avatarUrl)
        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Header: username · timestamp
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = " · ${comment.timestamp}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Body
            Text(
                text = comment.text,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp),
            )

            // Actions row — only Reply for now; add like/share/award here later.
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CommentAction(
                    iconRes = R.drawable.ic_reply,
                    label = "Reply",
                    onClick = { onReply(comment) },
                )
            }

            // Show / hide replies toggle + the nested thread.
            if (comment.replies.isNotEmpty()) {
                val count = comment.replies.size
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { repliesShown = !repliesShown }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (repliesShown) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = if (repliesShown) "Hide replies"
                        else "Show $count ${if (count == 1) "reply" else "replies"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (repliesShown) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min).padding(top = 2.dp)) {
                        // Vertical thread line spanning the nested replies.
                        Box(
                            modifier = Modifier
                                .width(1.5.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outlineVariant),
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            comment.replies.forEach { reply ->
                                CommentItem(comment = reply, onReply = onReply)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** A single tappable action (icon + label) in a comment's action row. */
@Composable
private fun CommentAction(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Round avatar: the user's photo if present, otherwise a circle with their first initial. */
@Composable
private fun CommentAvatar(
    username: String,
    avatarUrl: String?,
    size: Dp = 32.dp,
) {
    if (avatarUrl != null) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size).clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

// ---- Previews ------------------------------------------------------------------------------

private val previewComments = listOf(
    CommentUi(
        id = "1",
        username = "alice",
        timestamp = "2h",
        text = "Great write-up — point 3 especially matches what I've seen in production.",
        replies = listOf(
            CommentUi(
                id = "2",
                username = "bob",
                timestamp = "1h",
                text = "Agreed, though I think it's a bit more nuanced than that.",
                replies = listOf(
                    CommentUi(
                        id = "3",
                        username = "alice",
                        timestamp = "45m",
                        text = "Fair — which part would you push back on?",
                    ),
                ),
            ),
            CommentUi(id = "4", username = "carol", timestamp = "30m", text = "Saving this for later."),
        ),
    ),
    CommentUi(id = "5", username = "dave", timestamp = "5h", text = "First!"),
)

@Preview(showBackground = true, name = "Comments thread")
@Composable
private fun CommentsSectionPreview() {
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CommentsSection(comments = previewComments, modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun CommentsSectionEmptyPreview() {
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CommentsSection(comments = emptyList(), modifier = Modifier.padding(16.dp))
        }
    }
}
