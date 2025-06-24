package com.example.spooder.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Channel(
    val id: String? = null,
    val name: String,
    val created_at: String? = null,
    val imageUrl: String? = null
)


