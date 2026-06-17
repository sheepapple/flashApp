package com.example.flashappcs4520.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single user row from public.users.
 * Loaded by data/UserRepository; consumed by ProfileViewModel to populate ProfileScreen.
 */
@Serializable
data class User(
    val id: String? = null,        // Links to auth.uid()
    val username: String = "",     // Displays as the profile name
    @SerialName("topicofinterest")
    val interests: List<String>? = null, // Drives topic chips on profile screen
)