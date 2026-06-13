package com.example.flashappcs4520.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonElement
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * 1. What: Represents a single Article's data retrieved from the Guardian API.
 * 2. Who:  Consumed by ArticleRepository when mapping API results into News cards.
 * 3. When: Created during deserialization of results in the ArticleRepository.
 */
@Serializable
data class Article (
    @SerialName("web_title")
    val webTitle: String,

    val summary: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("web_url")
    val webUrl: String,

    @SerialName("published_at")
    val publishedAt: String,
    ) {


    fun formattedDate(): String =
        try {
            OffsetDateTime.parse(publishedAt, TIMESTAMP_FORMAT)   // "2026-06-12 16:38:45+00"
                .format(DISPLAY_FORMAT)                           // -> "Jun 12, 2026"
        } catch (e: Exception) {
            publishedAt.take(10)   // fallback: show just the date part, e.g. "2026-06-12"
        }

    companion object {
        // Postgres timestamptz text form: space separator, short "+00" offset.
        private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssx")
        private val DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy")
    }
}
