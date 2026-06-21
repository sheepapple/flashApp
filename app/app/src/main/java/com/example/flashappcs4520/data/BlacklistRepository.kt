package com.example.flashappcs4520.data

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: Reads/writes the blacklist for the current user in the Supabase `blacklist` table.
 * 2. Who:  Called by ArticleViewModel when user interacts with an article (like, comment, share, expand).
 * 3. When: Fetch on feed load; insert when interaction occurs.
 */
class BlacklistRepository {

    @Serializable
    private data class BlacklistInsert(
        @SerialName("userID") val userId: String,
        @SerialName("articleID") val articleId: Long,
    )

    @Serializable
    private data class BlacklistRow(
        @SerialName("articleID") val articleId: Long,
    )

    // returns all blacklisted article IDs for the current user
    suspend fun fetchBlacklistedIds(): Set<Long> = withContext(Dispatchers.IO) {
        val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id
            ?: return@withContext emptySet()
        SupabaseProvider.client
            .from("blacklist")
            .select(Columns.list("articleID")) {
                filter { eq("userID", uid) }
            }
            .decodeList<BlacklistRow>()
            .map { it.articleId }
            .toSet()
    }

    // adds an article to the current user's blacklist
    suspend fun addToBlacklist(articleId: Long) = withContext(Dispatchers.IO) {
        val uid = SupabaseProvider.client.auth.currentUserOrNull()?.id ?: return@withContext
        Log.d("BLACKLIST_TEST", "inserting articleId=$articleId uid=$uid")
        SupabaseProvider.client
            .from("blacklist")
            .upsert(BlacklistInsert(uid, articleId))
        Log.d("BLACKLIST_TEST", "insert done")
    }
}