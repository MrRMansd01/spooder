package com.example.spooder.model

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Task(
    val id: String,
    val title: String,
    val date: String,
    @SerialName("time_start")
    val time_start: String? = null,
    @SerialName("time_end")
    val time_end: String? = null,
    val description: String? = null,
    val color: String,
    @SerialName("is_completed")
    val is_completed: Boolean = false,
    @SerialName("user_id")
    val user_id: String? = null,
    @SerialName("created_at")
    val created_at: String? = null,
    @SerialName("updated_at")
    val updated_at: String? = null,
    val priority: String? = null
)