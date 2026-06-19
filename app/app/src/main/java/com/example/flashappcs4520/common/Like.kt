package com.example.flashappcs4520.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: A single "like" on an article, tied to the user who left it.
 * 2. Who:  Loaded/written by LikeRepository against the Supabase `likes` table.
 * 3. When: Fetched with the feed (for counts + which posts I've liked); created/
 *          deleted when the like button is toggled (needs an authenticated user).
 *
 * Schema (public.likes):
 *   id         uuid        (default gen_random_uuid())
 *   created_at timestamptz (default now())
 *   articleID  -> public.articles.id   (integer; modelled as Long to match Article.id)
 *   userID     uuid -> auth.users.id
 *
 * Note the capital "ID" in `articleID`/`userID` — this differs from `comments.articleId`.
 */
@Serializable
data class Like(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("articleID") val articleId: Long? = null,
    @SerialName("userID") val userId: String? = null,
)