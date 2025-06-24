package com.example.spooder.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable


@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChannelMember(
    val channel_id: String,
    val user_id: String
)