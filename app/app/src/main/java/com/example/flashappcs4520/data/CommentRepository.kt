package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.Comment
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: Reads/writes comments for an article in the Supabase `comments` table.
 * 2. Who:  Called by the UI layer when a card's comment section opens / a comment is sent.
 * 3. When: fetch on open; add on send.
 */
class CommentRepository {

    /** Loads all comments for one article, oldest first. Works with the anon key + a read RLS policy. */
    suspend fun fetchComments(articleID: Long): List<Comment> =
        withContext(Dispatchers.IO) {
            SupabaseProvider.client
                .from("comments")
                .select(Columns.list("id", "created_at", "articleID", "userID", "text", "parentID")) {
                    filter { eq("articleID", articleID) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Comment>()
        }

    /** All comments the current user has written, newest first — for the "My Comments" page. */
    suspend fun fetchMyComments(): List<Comment> =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()
            SupabaseProvider.client
                .from("comments")
                .select(Columns.list("id", "created_at", "articleID", "userID", "text", "parentID")) {
                    filter { eq("userID", uid) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Comment>()
        }

    /** Total comment count per article (replies included), for the feed badge. One lightweight
     *  query (just the articleID column), grouped client-side. */
    suspend fun fetchCommentCounts(articleIDs: List<Long>): Map<Long, Int> =
        withContext(Dispatchers.IO) {
            if (articleIDs.isEmpty()) return@withContext emptyMap()
            SupabaseProvider.client
                .from("comments")
                .select(Columns.list("articleID")) {
                    filter { isIn("articleID", articleIDs) }
                }
                .decodeList<Comment>()
                .mapNotNull { it.articleId }
                .groupingBy { it }
                .eachCount()
        }

    /** Only the columns we set; id/created_at use their DB defaults. parentID is null for a
     *  top-level comment, or the id of the comment being replied to. */
    @Serializable
    private data class CommentInsert(
        @SerialName("articleID") val articleId: Long,
        @SerialName("userID") val userId: String,
        val text: String,
        @SerialName("parentID") val parentId: String? = null,
    )


    // inserts a comment or a reply; returns the inserted row so callers know its new id
    suspend fun addComment(articleID: Long, text: String, parentId: String? = null) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")

            val inserted = SupabaseProvider.client
                .from("comments")
                .insert(CommentInsert(articleID, uid, text, parentId)) {
                    select()
                }
                .decodeSingle<Comment>()

            // if this is a reply, notify the original commenter and point them at this new reply
            if (parentId != null) {
                val parentComment = SupabaseProvider.client
                    .from("comments")
                    .select(Columns.list("userID")) {
                        filter { eq("id", parentId) }
                    }
                    .decodeList<Comment>()
                    .firstOrNull()

                val parentAuthorId = parentComment?.userId
                if (parentAuthorId != null && parentAuthorId != uid) {
                    NotificationRepository().insertNotification(
                        targetUserId = parentAuthorId,
                        articleId = articleID,
                        commentId = inserted.id,
                        text = "Someone replied to your comment",
                        type = "reply",
                    )
                }
            }
        }
}
