package com.example.flashappcs4520.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flashappcs4520.common.Notification
import java.time.Duration
import java.time.Instant
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color

/**
 * display all notification of current user
 * notification comes in 4 types:
 * 1.breaking — new article matching user's interests
 * 2.reply — someone replied to your comment
 * 3.like — your comment got likes [Todo-determined by either we allow likes on comments]
 * 4.digest — daily morning summary of top stories
 *
  */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationViewModel = viewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    // Mark all read only on exit of page, so during visit still is unread
                    IconButton(onClick = {
                        viewModel.markAllRead()
                        onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                title = {
                    Text(
                        text = "Notifications",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            notifications.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No notifications yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> {
                // split notification into: Today or Earlier(more than 24hr)
                val today = notifications.filter { isToday(it.createdAt) }
                val earlier = notifications.filter { !isToday(it.createdAt) }

                LazyColumn(modifier = Modifier.padding(innerPadding)) {
                    if (today.isNotEmpty()) {
                        item { SectionHeader("Today") }
                        items(today) { notif ->
                            NotificationRow(notif)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                    if (earlier.isNotEmpty()) {
                        item { SectionHeader("Earlier") }
                        items(earlier) { notif ->
                            NotificationRow(notif)
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun NotificationRow(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // red dot visible only for unread
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (!notification.isRead)  Color.Red else Color.Transparent,
                    shape = CircleShape,
                )
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = iconForType(notification.type),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = notification.text,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = relativeTime(notification.createdAt),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// maps type with icon
private fun iconForType(type: String): ImageVector = when (type) {
    "breaking" -> Icons.Default.Bolt
    "reply"    -> Icons.Default.ChatBubble
    "like"     -> Icons.Default.Favorite
    "digest"   -> Icons.Default.Article
    else       -> Icons.Default.Bolt
}

// helper, true if within 24hr
private fun isToday(createdAt: String?): Boolean {
    if (createdAt == null) return false
    return try {
        Duration.between(Instant.parse(createdAt), Instant.now()).toHours() < 24
    } catch (e: Exception) { false }
}

// convert timestamp
private fun relativeTime(createdAt: String?): String {
    if (createdAt == null) return ""
    return try {
        val elapsed = Duration.between(Instant.parse(createdAt), Instant.now())
        when {
            elapsed.toMinutes() < 1  -> "just now"
            elapsed.toMinutes() < 60 -> "${elapsed.toMinutes()}m ago"
            elapsed.toHours() < 24   -> "${elapsed.toHours()}h ago"
            else                     -> "${elapsed.toDays()}d ago"
        }
    } catch (e: Exception) { "" }
}