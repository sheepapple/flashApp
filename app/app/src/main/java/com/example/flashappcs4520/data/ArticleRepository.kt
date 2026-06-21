package com.example.flashappcs4520.data

import com.example.flashappcs4520.common.Article
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 1. What: Reads Article rows from the Supabase `articles` table.
 * 2. Who:  Called by the UI layer (ViewModel / screen) to populate the feed.
 * 3. When: On screen load / refresh.
 *
 * Note: we select only the columns the Article model needs. `summary` exists in
 * the table but rows may be null until the Gemini summarization step is built.
 */
class ArticleRepository {

    suspend fun fetchArticles(count: Long = 10): List<Article> =
        withContext(Dispatchers.IO) {
            SupabaseProvider.client
                .from("articles")
                .select(Columns.list("id", "web_title", "summary", "image_url", "web_url", "published_at", "section")) {
                    order("published_at", Order.DESCENDING)
                    limit(count)
                }
                .decodeList<Article>()
        }

    /** Fetches specific articles by id — backs deep links (notification → article) and the
     *  saved-articles list. Returns whatever ids exist; an empty input short-circuits to []. */
    suspend fun fetchArticlesByIds(ids: List<Long>): List<Article> =
        withContext(Dispatchers.IO) {
            if (ids.isEmpty()) return@withContext emptyList()
            SupabaseProvider.client
                .from("articles")
                .select(Columns.list("id", "web_title", "summary", "image_url", "web_url", "published_at", "section")) {
                    filter { isIn("id", ids) }
                }
                .decodeList<Article>()
        }
}