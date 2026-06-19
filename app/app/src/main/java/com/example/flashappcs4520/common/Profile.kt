package com.example.flashappcs4520.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 1. What: The public-facing slice of a user — username + avatar only — read from the
 *          `public_profiles` view (the columns everyone is allowed to see).
 * 2. Who:  Used to resolve a comment's `userID` into a display name and picture.
 * 3. When: Fetched alongside an article's comments, then merged in by the comment mapper.
 */
@Serializable
data class Profile(
    val id: String,
    val username: String = "",
    @SerialName("profilepicture_url") val avatarUrl: String? = null,
)