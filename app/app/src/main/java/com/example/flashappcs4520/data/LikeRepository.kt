package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.Like
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: Reads/writes likes for articles in the Supabase `likes` table.
 * 2. Who:  Called by ArticleViewModel — for counts + which posts I've liked — and
 *          when the user toggles a like.
 * 3. When: fetch with the feed; insert on like; delete on unlike.
 *
 * Like a comment, writing requires a signed-in user. Auth is wired (see AuthRepository),
 * so we read `auth.uid()` directly and stamp it onto each row.
 *
 * DB prerequisites for writes to succeed (one-time, in Supabase):
 *   - RLS SELECT policy (so counts load), INSERT policy `userID = auth.uid()`,
 *     DELETE policy `userID = auth.uid()`.
 *   - A UNIQUE(articleID, userID) constraint so a user can't like the same article twice.
 */
class LikeRepository {

    /** Insert payload: only the columns we set; id + created_at use their DB defaults. */
    @Serializable
    private data class LikeInsert(
        @SerialName("articleID") val articleId: Long,
        @SerialName("userID") val userId: String,
    )

    /**
     * All like rows for the given articles, in one query. The caller aggregates these
     * into per-article counts and the set of articles the current user has liked.
     */
    suspend fun fetchLikesFor(articleIds: List<Long>): List<Like> =
        withContext(Dispatchers.IO) {
            if (articleIds.isEmpty()) return@withContext emptyList()
            SupabaseProvider.client
                .from("likes")
                .select(Columns.list("id", "created_at", "articleID", "userID")) {
                    filter { isIn("articleID", articleIds) }
                }
                .decodeList<Like>()
        }

    /** Adds the current user's like for an article. */
    suspend fun likeArticle(articleId: Long) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("likes")
                .insert(LikeInsert(articleId, uid))
        }

    /** Removes the current user's like for an article. */
    suspend fun unlikeArticle(articleId: Long) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("likes")
                .delete {
                    filter {
                        eq("articleID", articleId)
                        eq("userID", uid)
                    }
                }
        }

    /** Article ids the current user has liked — for a future "liked posts" view. */
    suspend fun fetchMyLikedArticleIds(): List<Long> =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()
            SupabaseProvider.client
                .from("likes")
                .select(Columns.list("articleID")) {
                    filter { eq("userID", uid) }
                }
                .decodeList<Like>()
                .mapNotNull { it.articleId }
        }
}
