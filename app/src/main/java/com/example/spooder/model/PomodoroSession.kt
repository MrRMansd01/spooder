package com.example.spooder.model

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.LocalDate

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class PomodoroSession(
    val id: String? = null,
    val user_id: String? = null,
    val session_date: String = LocalDate.now().toString(),
    val start_time: String = LocalDateTime.now().toString(),
    val end_time: String? = null,
    val session_type: String, // "focus" or "break"
    val duration_minutes: Int,
    val completed: Boolean = false,
    val focus_duration_setting: Int,
    val break_duration_setting: Int,
    val created_at: String? = null,
    val updated_at: String? = null
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class DailyPomodoroStats(
    val user_id: String,
    val session_date: String,
    val total_focus_minutes: Int,
    val completed_pomodoros: Int,
    val incomplete_pomodoros: Int
) 