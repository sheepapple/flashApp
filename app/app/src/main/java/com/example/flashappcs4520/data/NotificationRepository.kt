package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.Notification
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class NotificationRepository {

    @Serializable
    private data class NotificationInsert(
        @SerialName("userID")
        val userId: String,
        @SerialName("articleID")
        val articleId: Long?,
        @SerialName("commentID")
        val commentId: String?,
        val text: String,
        val type: String,
    )

    // inserts a notification; commentId points a "reply" at the exact comment to scroll to
    suspend fun insertNotification(
        targetUserId: String,
        articleId: Long?,
        text: String,
        type: String,
        commentId: String? = null,
    ) =
        withContext(Dispatchers.IO) {
            client
                .from("notifications")
                .insert(NotificationInsert(targetUserId, articleId, commentId, text, type))
        }

    private val client get() = SupabaseProvider.client

    // loads all notifications for the current user, loads newest first
    suspend fun fetchNotifications(): List<Notification> = withContext(Dispatchers.IO) {
        val uid = client.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
        client
            .from("notifications")
            .select(Columns.list("id", "created_at", "articleID", "commentID", "userID", "text", "is_read", "type")) {
                filter { eq("userID", uid) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<Notification>()
    }

    // true if user has unread notification
    suspend fun hasUnread(): Boolean = withContext(Dispatchers.IO) {
        val uid = client.auth.currentUserOrNull()?.id ?: return@withContext false
        client
            .from("notifications")
            .select(Columns.list("id")) {
                filter {
                    eq("userID", uid)
                    eq("is_read", false)
                }
            }
            .decodeList<Notification>()
            .isNotEmpty()
    }

    // mark all notification as read
    suspend fun markAllRead() = withContext(Dispatchers.IO) {
        val uid = client.auth.currentUserOrNull()?.id ?: return@withContext
        client
            .from("notifications")
            .update({ set("is_read", true) }) {
                filter {
                    eq("userID", uid)
                    eq("is_read", false)
                }
            }
    }
}
