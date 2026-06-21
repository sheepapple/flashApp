package com.example.flashappcs4520.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("articleID") val articleId: Long? = null,
    @SerialName("commentID") val commentId: String? = null,
    @SerialName("userID") val userId: String? = null,
    val text: String = "",
    @SerialName("is_read") val isRead: Boolean = false,
    val type: String = "",
)
