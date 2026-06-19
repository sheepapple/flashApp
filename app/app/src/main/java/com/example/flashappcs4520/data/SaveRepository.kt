package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.Saved
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: Reads/writes the current user's saved (bookmarked) articles in `saves`.
 * 2. Who:  Called by ArticleViewModel for "which posts have I saved" + toggling a save.
 * 3. When: fetch with the feed; insert on save; delete on unsave.
 */
class SaveRepository {

    /** Insert payload: id + created_at use their DB defaults. */
    @Serializable
    private data class SaveInsert(
        @SerialName("articleID") val articleId: Long,
        @SerialName("userID") val userId: String,
    )

    /** Article ids the current user has saved — for the feed and a "saved posts" view. */
    suspend fun fetchMySavedArticleIds(): List<Long> =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: return@withContext emptyList()
            SupabaseProvider.client
                .from("saves")
                .select(Columns.list("articleID")) {
                    filter { eq("userID", uid) }
                }
                .decodeList<Saved>()
                .mapNotNull { it.articleId }
        }

    /** Saves an article for the current user. */
    suspend fun saveArticle(articleId: Long) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("saves")
                .insert(SaveInsert(articleId, uid))
        }

    /** Removes the current user's save for an article. */
    suspend fun unsaveArticle(articleId: Long) =
        withContext(Dispatchers.IO) {
            val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("No authenticated user")
            SupabaseProvider.client
                .from("saves")
                .delete {
                    filter {
                        eq("articleID", articleId)
                        eq("userID", uid)
                    }
                }
        }
}