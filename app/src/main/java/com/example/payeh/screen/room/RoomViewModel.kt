package com.example.payeh.screen.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.payeh.SupabaseService
import com.example.payeh.model.Channel
import com.example.payeh.model.PomodoroSession
import com.example.payeh.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


@Suppress("UNCHECKED_CAST")
class RoomViewModel(application: Application) : AndroidViewModel(application) {
    private val supabaseService = SupabaseService.getInstance(application)
    
    private val _mostTimeUsers = MutableLiveData<List<UserData>>()
    val mostTimeUsers: LiveData<List<UserData>> = _mostTimeUsers

    private val _otherUsers = MutableLiveData<List<UserData>>()
    val otherUsers: LiveData<List<UserData>> = _otherUsers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentSession = MutableLiveData<PomodoroSession?>()
    val currentSession: LiveData<PomodoroSession?> = _currentSession

    private val _todayStats = MutableLiveData<TodayStats?>()
    val todayStats: LiveData<TodayStats> = _todayStats as LiveData<TodayStats>

    private val _userState = MutableStateFlow<Channel?>(null)
    val userState: StateFlow<Channel?> = _userState.asStateFlow()

    private val _weeklyStats = MutableLiveData<UserData>()

    init {
        fetchUserData()
        fetchWeeklyStats()
    }

    fun fetchUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get all users from Supabase
                val usersResult = supabaseService.getAllUsers()
                if (usersResult.isFailure) {
                    throw usersResult.exceptionOrNull() ?: Exception("Failed to fetch users")
                }
                val users = usersResult.getOrNull() ?: emptyList()

                // Get current month's date range in yyyy-MM-dd format
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = dateFormat.format(startOfMonth.time)
                val endDate = dateFormat.format(endOfMonth.time)

                // Get this month's tasks for all users
                val tasksResult = supabaseService.getTasksByDateRange(startDate, endDate)
                if (tasksResult.isFailure) {
                    throw tasksResult.exceptionOrNull() ?: Exception("Failed to fetch tasks")
                }
                val tasks = tasksResult.getOrNull() ?: emptyList()

                // Process users and their tasks
                val userDataList = users.map { user ->
                    val userTasks = tasks.filter { it.user_id == user.id }
                    
                    val completedTasks = userTasks.count { it.is_completed }
                    val pendingTasks = userTasks.count { !it.is_completed }
                    
                    // Calculate total time from completed tasks
                    val totalMinutes = userTasks.filter { it.is_completed }.sumOf { task ->
                        calculateTaskDuration(task)
                    }
                    
                    // Calculate score based on task colors
                    val score = userTasks.filter { it.is_completed }.sumOf { 
                        when (it.color.lowercase()) {
                            "3" -> 3.0
                            "2" -> 2.0
                            "1" -> 1.0
                            else -> 1.0
                        }
                    }.toFloat()
                    
                    UserData(
                        name = user.username.toString(),
                        hours = formatTotalTime(totalMinutes),
                        completedTasks = completedTasks,
                        pendingTasks = pendingTasks,
                        score = score,
                        avatarUrl = user.avatar_url
                    )
                }

                // Sort users by total time for Time section
                val timeBasedUsers = userDataList.sortedByDescending { 
                    parseHoursToMinutes(it.hours)
                }
                _mostTimeUsers.value = timeBasedUsers

                // Sort users by score for Score section
                val scoreBasedUsers = userDataList.sortedByDescending { it.score }
                _otherUsers.value = scoreBasedUsers

            } catch (e: Exception) {
                _error.value = "Error loading data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun retryFetch() {
        fetchUserData()
    }


    private fun parseHoursToMinutes(timeStr: String): Int {
        val parts = timeStr.split(" ")
        var totalMinutes = 0
        
        for (part in parts) {
            when {
                part.endsWith("h") -> {
                    totalMinutes += part.removeSuffix("h").toIntOrNull()?.times(60) ?: 0
                }
                part.endsWith("m") -> {
                    totalMinutes += part.removeSuffix("m").toIntOrNull() ?: 0
                }
            }
        }
        
        return totalMinutes
    }

    private fun calculateTaskDuration(task: Task): Int {
        return try {
            if (task.time_start.isNullOrBlank() || task.time_end.isNullOrBlank()) {
                return 0
            }
            val startStr = task.time_start.trim()
            val endStr = task.time_end.trim()
            var duration = 0

            // Try 24-hour format first
            val timeFormat24 =SimpleDateFormat("HH:mm", Locale.getDefault())
            val start24 = runCatching { timeFormat24.parse(startStr) }.getOrNull()
            val end24 = runCatching { timeFormat24.parse(endStr) }.getOrNull()
            if (start24 != null && end24 != null) {
                val diffMillis = end24.time - start24.time
                duration += (diffMillis / (60 * 1000)).toInt().let { if (it < 0) it + 24 * 60 else it }
            }

            // Try 12-hour format (h:mm a)
            val timeFormat12 = SimpleDateFormat("h:mm a", Locale.getDefault())
            val start12 = runCatching { timeFormat12.parse(startStr) }.getOrNull()
            val end12 = runCatching { timeFormat12.parse(endStr) }.getOrNull()
            if (start12 != null && end12 != null) {
                val diffMillis = end12.time - start12.time
                duration += (diffMillis / (60 * 1000)).toInt().let { if (it < 0) it + 24 * 60 else it }
            }

            duration
        } catch (_: Exception) {
            0
        }
    }


    private fun formatTotalTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins}m"
        }
    }




    fun fetchWeeklyStats() {
        viewModelScope.launch {
            try {
                val userId = supabaseService.getCurrentUserId()
                if (userId.toString().isEmpty()) {
                    return@launch
                }
                val tasksResult = supabaseService.getTasksForLastWeek(userId.toString())
                if (tasksResult.isFailure) {
                    throw tasksResult.exceptionOrNull() ?: Exception("Failed to fetch weekly tasks")
                }
                val tasks = tasksResult.getOrNull() ?: emptyList()

                // Calculate weekly stats
                val completedTasks = tasks.count { it.is_completed }
                val pendingTasks = tasks.count { !it.is_completed }

                // Calculate total time from completed tasks
                val totalMinutes = tasks.filter { it.is_completed }.sumOf { task ->
                    calculateTaskDuration(task)
                }

                // Calculate score
                val score = tasks.filter { it.is_completed }.sumOf { 
                    when (it.color.lowercase()) {
                        "3" -> 3.0
                        "2" -> 2.0
                        "1" -> 1.0
                        else -> 1.0
                    }
                }.toFloat()

                val weeklyUserData = UserData(
                    name = "Current User",
                    hours = formatTotalTime(totalMinutes),
                    completedTasks = completedTasks,
                    pendingTasks = pendingTasks,
                    score = score,
                    avatarUrl = null
                )

                _weeklyStats.value = weeklyUserData

            } catch (e: Exception) {
                _error.value = "Error loading weekly stats: ${e.message}"
            }
        }
    }
}

data class UserData(
    val name: String,
    val hours: String,
    val completedTasks: Int,
    val pendingTasks: Int,
    val score: Float,
    val avatarUrl: String?
)

data class TodayStats(
    val total_focus_minutes: Int,
    val completed_pomodoros: Int
)
