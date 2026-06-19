package com.example.flashappcs4520.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: A single comment on an article, tied to the user who left it.
 * 2. Who:  Loaded/saved by CommentRepository against the Supabase `comments` table.
 * 3. When: Fetched when a card's comment section opens; created on send (needs auth).
 *
 * Schema (public.comments):
 *   id         uuid
 *   created_at timestamptz
 *   articleID  -> public.articles.id   (int8; matches likes/saves naming)
 *   userID     uuid -> auth.users.id
 *   text       text
 *   parentID   uuid (nullable, for threaded replies)
 */
@Serializable
data class Comment(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("articleID") val articleId: Long? = null,
    @SerialName("userID") val userId: String? = null,
    val text: String = "",
    @SerialName("parentID") val parentId: String? = null,
)