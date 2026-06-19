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

    /** Only the columns we set; id/created_at use their DB defaults. */
    @Serializable
    private data class CommentInsert(
        @SerialName("articleID") val articleId: Long,
        @SerialName("userID") val userId: String,
        val text: String,
    )

    /** Inserts one comment as the current user. */
    suspend fun addComment(articleID: Long, text: String) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("comments")
                .insert(CommentInsert(articleID, uid, text))
        }
}
