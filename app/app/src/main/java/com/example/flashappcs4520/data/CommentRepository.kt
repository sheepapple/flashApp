package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.Comment
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 1. What: Reads/writes comments for an article in the Supabase `comments` table.
 * 2. Who:  Called by the UI layer when a card's comment section opens / a comment is sent.
 * 3. When: fetch on open; add on send.
 */
class CommentRepository {

    /** Loads all comments for one article, oldest first. Works with the anon key + a read RLS policy. */
    suspend fun fetchComments(articleId: Long): List<Comment> =
        withContext(Dispatchers.IO) {
            SupabaseProvider.client
                .from("comments")
                .select(Columns.list("id", "created_at", "articleId", "userID", "text")) {
                    filter { eq("articleId", articleId) }
                    order("created_at", Order.ASCENDING)
                }
                .decodeList<Comment>()
        }

    /**
     * No-op for now. Saving comments needs user authentication, which isn't set up yet.
     * Before this can be implemented:
     *   1. Add Supabase Auth (login) so there is a signed-in user.
     *   2. Set the `userID` column default to `auth.uid()` and add an INSERT RLS policy.
     *   3. Then insert { articleId, text } here (the DB stamps userID from the session).
     */
    suspend fun addComment(articleId: Long, text: String) {
        // intentionally empty — see KDoc above
    }
}
