package com.example.flashappcs4520.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: A single "save" (bookmark) of an article by a user.
 * 2. Who:  Loaded/written by SaveRepository against the Supabase `saves` table.
 * 3. When: Fetched with the feed (which posts I've saved); created/deleted when the
 *          bookmark button is toggled (needs an authenticated user).
 *
 * Schema (public.saves) — identical to `likes`:
 *   id         uuid        (default gen_random_uuid())
 *   created_at timestamptz (default now())
 *   articleID  -> public.articles.id   (integer; modelled as Long to match Article.id)
 *   userID     uuid -> auth.users.id
 *
 * Unlike likes, saves are private: there is no total count and no public-read policy,
 * so only the owning user ever reads their own rows.
 */
@Serializable
data class Saved(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("articleID") val articleId: Long? = null,
    @SerialName("userID") val userId: String? = null,
)