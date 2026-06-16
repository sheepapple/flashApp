package com.example.flashappcs4520.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Fetch user information once when the user navigates to their profile
 *  Loaded by data/UserRepository
 *  Consumed by ui/ProfileViewModel
 */
@Serializable
data class User(
    val id: String? = null,
    val username: String = "",
    @SerialName("topicofinterest")
    val interests: List<String>? = null,
)