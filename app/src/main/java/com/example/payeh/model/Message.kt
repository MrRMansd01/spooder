package com.example.payeh.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val channel_id: String,
    val content: String,
    val sender_id: String?,
    val created_at: String?,
    val imageUrl: String? = null,
    val senderName: String? = null,
    val senderAvatarUrl: String? = null
)
