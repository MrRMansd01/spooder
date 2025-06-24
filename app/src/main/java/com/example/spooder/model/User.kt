package com.example.spooder.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String? = null,
    @SerialName("total_minutes")
    val total_minutes: Int = 0,
    @SerialName("completed_tasks")
    val completed_tasks: Int = 0,
    @SerialName("pending_tasks")
    val pending_tasks: Int = 0,
    @SerialName("created_at")
    val created_at: String,
    @SerialName("updated_at")
    val updated_at: String,
    @SerialName("avatar_url")
    val avatar_url: String? = null,
    @SerialName("last_sign_in")
    val last_sign_in: String? = null
) 