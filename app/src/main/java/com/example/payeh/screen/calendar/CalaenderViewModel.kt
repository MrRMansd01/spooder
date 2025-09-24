package com.example.payeh.screen.calendar

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.payeh.SupabaseService
import com.example.payeh.model.Task
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val supabaseService = SupabaseService.getInstance(application)

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private fun formatDateForDb(date: String): String {
        try {
            val calendar = Calendar.getInstance()
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            dbFormat.timeZone = TimeZone.getTimeZone("UTC")

            if (date.matches(Regex("\\d{1,2}"))) {


                val result = dbFormat.format(calendar.time)
                Log.d("TaskViewModel", "Formatted day $date to $result")
                return result
            }

            if (date.contains("-")) {
                return date
            }

            val today = dbFormat.format(calendar.time)
            Log.w("TaskViewModel", "Invalid date format: $date, using today: $today")
            return today

        } catch (e: Exception) {
            Log.e("TaskViewModel", "Error formatting date: $date", e)
            val calendar = Calendar.getInstance()
            val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            return dbFormat.format(calendar.time)
        }
    }

    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    private fun getColorScore(color: String): String {
        // If the color is already a number, return it
        if (color.toIntOrNull() != null) {
            return color
        }
        // Otherwise convert from color name to score
        return when (color.lowercase()) {
            "red" -> "3"
            "yellow" -> "2"
            "green" -> "1"
            else -> "1"
        }
    }

    fun fetchTasksByDateAndUser(date: String, userId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val formattedDate = formatDateForDb(date)
                Log.d("TaskViewModel", "Original date: $date, Formatted date: $formattedDate")
                
                if (userId == null) {
                    Log.w("TaskViewModel", "fetchTasksByDateAndUser called with null userId")
                    fetchTasksByDate(formattedDate)
                    return@launch
                }

                val result = supabaseService.getTasksByDateAndUser(formattedDate, userId)
                result.fold(
                    onSuccess = { tasks ->
                        Log.d("TaskViewModel", "Raw tasks received: ${tasks.size}")
                        tasks.forEach { task ->
                            Log.d("TaskViewModel", "Task date: ${task.date}, id: ${task.id}")
                        }
                        
                        _tasks.value = tasks.map { task ->
                            task.copy(color = getColorScore(task.color))
                        }.sortedByDescending { it.created_at }
                        
                        Log.d("TaskViewModel", "Successfully processed ${tasks.size} tasks")
                    },
                    onFailure = { exception ->
                        Log.e("TaskViewModel", "Error fetching tasks: ${exception.message}")
                        _error.value = exception.message
                        _tasks.value = emptyList()
                    }
                )
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Exception in fetchTasksByDateAndUser: ${e.message}")
                _error.value = e.message
                _tasks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchTasksByDate(date: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val formattedDate = formatDateForDb(date)
                Log.d("TaskViewModel", "Fetching all tasks for date: $formattedDate")
                
                val result = supabaseService.getTasksByDate(formattedDate)
                if (result.isSuccess) {
                    val tasksList = result.getOrNull() ?: emptyList()
                    val tasksWithScores = tasksList.map { task ->
                        task.copy(color = getColorScore(task.color))
                    }
                    _tasks.value = tasksWithScores
                    _error.value = null
                    Log.d("TaskViewModel", "Fetched ${tasksWithScores.size} tasks for date $formattedDate")
                } else {
                    _error.value = "Failed to fetch tasks"
                    _tasks.value = emptyList()
                }
            } catch (e: Exception) {
                _error.value = e.message
                Log.e("TaskViewModel", "Error fetching tasks: ${e.message}")
                _tasks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            try {
                val currentTime = getCurrentTimestamp()
                val formattedDate = formatDateForDb(task.date)
                Log.d("TaskViewModel", "Adding task with date: Original=${task.date}, Formatted=$formattedDate")
                
                val taskWithMetadata = task.copy(
                    date = formattedDate,
                    created_at = currentTime,
                    updated_at = currentTime
                )
                
                Log.d("TaskViewModel", "Task metadata: created_at=$currentTime, updated_at=$currentTime")
                
                val result = supabaseService.addTask(taskWithMetadata)
                if (result.isSuccess) {
                    Log.d("TaskViewModel", "Task added successfully, refreshing tasks for date: $formattedDate")
                    fetchTasksByDateAndUser(formattedDate, task.user_id)
                } else {
                    Log.e("TaskViewModel", "Failed to add task")
                    _error.value = "Failed to add task"
                }
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error adding task: ${e.message}")
                _error.value = e.message
            }
        }
    }

    fun toggleTaskCompletion(taskId: String, newCompletionState: Boolean) {
        viewModelScope.launch {
            try {
                val result = supabaseService.toggleTaskCompletion(taskId, newCompletionState)
                if (result.isSuccess) {
                    // Update the task in the current list
                    _tasks.value = _tasks.value?.map {
                        if (it.id == taskId) {
                            it.copy(
                                is_completed = newCompletionState,
                                updated_at = getCurrentTimestamp()
                            )
                        } else it
                    }
                } else {
                    _error.value = "Failed to update task completion status"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                val result = supabaseService.deleteTask(task.id)
                if (result.isSuccess) {
                    // Remove the task from the current list
                    _tasks.value = _tasks.value?.filter { it.id != task.id }
                } else {
                    _error.value = "Failed to delete task"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    init {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val today = dateFormat.format(calendar.time)
        val userId = supabaseService.getCurrentUserId()
        fetchTasksByDateAndUser(today, userId.toString())
    }
}