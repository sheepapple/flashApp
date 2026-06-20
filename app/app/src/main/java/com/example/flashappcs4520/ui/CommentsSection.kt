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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flashappcs4520.R
import com.example.flashappcs4520.common.Comment
import com.example.flashappcs4520.common.Profile
import com.example.flashappcs4520.ui.theme.FlashAppCS4520Theme
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

/**
 * Builds the [CommentUi] reply tree from a flat list of DB comments plus a profile lookup.
 *
 * Pure (no I/O): the caller fetches the comments and the profiles; this assembles them — nesting
 * each comment under its `parentId`, resolving the author's username/avatar, and formatting the
 * timestamp. Comments arrive oldest-first, and groupBy preserves that, so siblings stay in order.
 *
 * A missing profile falls back to "Unknown"; a reply whose parent isn't in the list is dropped
 * (won't happen in normal use, since we fetch all of an article's comments together).
 */
fun buildCommentUiTree(
    comments: List<Comment>,
    profiles: Map<String, Profile>,
): List<CommentUi> {
    val byParent = comments.groupBy { it.parentId }
    fun toUi(comment: Comment): CommentUi {
        val profile = comment.userId?.let { profiles[it] }
        return CommentUi(
            id = comment.id.orEmpty(),
            username = profile?.username?.takeIf { it.isNotBlank() } ?: "Unknown",
            avatarUrl = profile?.avatarUrl,
            timestamp = formatRelative(comment.createdAt),
            text = comment.text,
            replies = byParent[comment.id].orEmpty().map(::toUi),
        )
    }
    return byParent[null].orEmpty().map(::toUi)
}

private val COMMENT_TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx")
private val COMMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d")

/** A Postgres timestamptz → a short relative label: "just now", "5m", "2h", "3d", or "Jun 19". */
private fun formatRelative(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return ""
    val then = parseTimestamp(timestamp) ?: return timestamp.take(10)
    val minutes = ChronoUnit.MINUTES.between(then, OffsetDateTime.now())
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m"
        minutes < 1_440 -> "${minutes / 60}h"
        minutes < 10_080 -> "${minutes / 1_440}d"
        else -> then.format(COMMENT_DATE_FORMAT)
    }
}

/**
 * Parses the timestamptz PostgREST returns — ISO 8601 with a 'T' and possibly fractional seconds,
 * e.g. "2026-06-19T09:00:00.123456+00:00" — falling back to the space-separated form just in case.
 */
private fun parseTimestamp(ts: String): OffsetDateTime? =
    runCatching { OffsetDateTime.parse(ts) }
        .recoverCatching { OffsetDateTime.parse(ts, COMMENT_TS_FORMAT) }
        .getOrNull()

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
                CommentItem(comment = comment, depth = 0, onReply = onReply)
            }
        }
    }
}

/**
 * The comments "layer": a tall bottom sheet hosting the scrolling thread with the input pinned
 * to its bottom. Rises over a scrim; drag-down or scrim-tap dismisses it without touching the
 * feed behind. Tapping Reply focuses the input and raises the keyboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsSheet(
    comments: List<CommentUi>,
    onDismiss: () -> Unit,
    onSendComment: (text: String, parentId: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var replyingTo by remember { mutableStateOf<CommentUi?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .imePadding(),
        ) {
            Text(
                text = "Comments",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // The thread scrolls in the space between the header and the pinned input bar.
            CommentsSection(
                comments = comments,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onReply = { replyingTo = it },
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
            CommentInputBar(
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null },
                onSend = { text ->
                    onSendComment(text, replyingTo?.id)
                    replyingTo = null
                },
            )
        }
    }
}

/**
 * The pinned input row at the bottom of the comments sheet. Shows a "Replying to {username}"
 * label (with cancel) when replying, and pulls focus so the keyboard opens when a reply starts.
 */
@Composable
private fun CommentInputBar(
    replyingTo: CommentUi?,
    onCancelReply: () -> Unit,
    onSend: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    LaunchedEffect(replyingTo) {
        if (replyingTo != null) focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (replyingTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Replying to ${replyingTo.username}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Cancel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable { onCancelReply() },
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text(if (replyingTo == null) "Add a comment…" else "Write a reply…") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                shape = RoundedCornerShape(percent = 50),
            )
            IconButton(
                onClick = {
                    if (draft.isNotBlank()) {
                        onSend(draft.trim())
                        draft = ""
                        focusManager.clearFocus()   // dismiss the keyboard after posting
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

/**
 * One comment plus its (collapsible) replies. Recursive: each reply is itself a [CommentItem],
 * so the parentID hierarchy renders to any depth. Replies start hidden behind a "show replies"
 * toggle.
 */
/** How many levels deep replies keep indenting before they stop and continue flush. */
private const val MAX_INDENT_DEPTH = 5

/** Horizontal indent added per nesting level (on top of the thin thread line). */
private val INDENT_STEP = 12.dp

@Composable
private fun CommentItem(
    comment: CommentUi,
    depth: Int,
    onReply: (CommentUi) -> Unit,
) {
    var repliesShown by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // The comment itself: avatar + content.
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

                // Show / hide replies toggle.
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
                }
            }
        }

        // Nested replies render below, indented one step (with a thread line) per level — until
        // we hit MAX_INDENT_DEPTH, after which deeper replies stop indenting and continue flush
        // (like Reddit), so very deep threads don't march off the right edge.
        if (comment.replies.isNotEmpty() && repliesShown) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                if (depth < MAX_INDENT_DEPTH) {
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.outlineVariant),
                    )
                    Spacer(Modifier.width(INDENT_STEP))
                }
                Column {
                    comment.replies.forEach { reply ->
                        CommentItem(comment = reply, depth = depth + 1, onReply = onReply)
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

@Preview(showBackground = true, name = "Empty")
@Composable
private fun CommentsSectionEmptyPreview() {
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CommentsSection(comments = emptyList(), modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true, name = "Mapped from DB rows")
@Composable
private fun CommentsSectionMappedPreview() {
    // Flat rows as they'd come back from the DB (note the parentId links), plus a profile lookup.
    val rows = listOf(
        Comment(id = "1", createdAt = "2026-06-19 09:00:00+00", articleId = 1, userId = "u-alice", text = "Top-level comment."),
        Comment(id = "2", createdAt = "2026-06-19 10:30:00+00", articleId = 1, userId = "u-bob", text = "A reply to the first comment.", parentId = "1"),
        Comment(id = "3", createdAt = "2026-06-19 11:00:00+00", articleId = 1, userId = "u-alice", text = "And a reply to that reply.", parentId = "2"),
        Comment(id = "4", createdAt = "2026-06-19 08:00:00+00", articleId = 1, userId = "u-ghost", text = "Author with no profile row."),
    )
    val profiles = mapOf(
        "u-alice" to Profile(id = "u-alice", username = "alice"),
        "u-bob" to Profile(id = "u-bob", username = "bob"),
    )
    FlashAppCS4520Theme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            CommentsSection(
                comments = buildCommentUiTree(rows, profiles),
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
