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

    /**
     * Fetches a page of articles, newest first. [offset] skips that many rows so the feed can
     * page in more as the user scrolls. The secondary sort on `id` keeps ordering stable across
     * pages (ties on `published_at` won't shuffle and cause gaps/duplicates between pages).
     */
    suspend fun fetchArticles(count: Long = 10, offset: Long = 0): List<Article> =
        withContext(Dispatchers.IO) {
            SupabaseProvider.client
                .from("articles")
                .select(Columns.list("id", "web_title", "summary", "image_url", "web_url", "published_at", "section")) {
                    order("published_at", Order.DESCENDING)
                    order("id", Order.DESCENDING)
                    range(offset, offset + count - 1)
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