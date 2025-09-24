package com.example.payeh.screen.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.payeh.SupabaseService
import com.example.payeh.model.Channel
import com.example.payeh.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val supabaseService: SupabaseService
) : ViewModel() {

    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        getChannels()
        fetchAllUsers() // اضافه کردن این خط
    }

    fun getChannels() {
        viewModelScope.launch {
            _isLoading.value = true
            Log.d("HomeViewModel", "Starting to fetch channels...")

            supabaseService.getChannels()
                .onSuccess { channelList ->
                    Log.d("HomeViewModel", "Successfully fetched ${channelList.size} channels")
                    channelList.forEach { channel ->
                        Log.d("HomeViewModel", "Channel: ${channel.name} - ID: ${channel.id}")
                    }
                    _channels.value = channelList
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "Error fetching channels: ${e.message}", e)
                    _channels.value = emptyList()
                }
            _isLoading.value = false
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "Starting to fetch all users...")

            supabaseService.getAllUsers()
                .onSuccess { userList ->
                    val currentUserId = supabaseService.getCurrentUserId()?.toString()
                    val filteredUsers = userList.filter { it.id.toString() != currentUserId }
                    Log.d("HomeViewModel", "Successfully fetched ${filteredUsers.size} users (excluding current user)")
                    _allUsers.value = filteredUsers
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "Error fetching all users: ${e.message}", e)
                    _allUsers.value = emptyList()
                }
        }
    }

    fun addChannel(name: String, imageUri: Uri?, selectedUserIds: List<String>) {
        if (name.isBlank()) {
            Log.e("HomeViewModel", "Channel name cannot be blank")
            return
        }

        if (selectedUserIds.isEmpty()) {
            Log.e("HomeViewModel", "No users selected")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("HomeViewModel", "Creating channel: $name with ${selectedUserIds.size} members")

                val imageUrl = imageUri?.let {
                    Log.d("HomeViewModel", "Uploading image...")
                    supabaseService.uploadImage(it, "channel_images").getOrNull()
                }

                supabaseService.createChannel(name, imageUrl, selectedUserIds)
                    .onSuccess {
                        Log.d("HomeViewModel", "Channel created successfully")
                        getChannels() // Refresh the channel list
                    }
                    .onFailure { e ->
                        Log.e("HomeViewModel", "Error creating channel: ${e.message}", e)
                    }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception in addChannel: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getChannelImage(channelId: String, onImageFetched: (Uri?) -> Unit) {
        viewModelScope.launch {
            supabaseService.getChannelById(channelId).onSuccess { channel ->
                channel.imageUrl?.let {
                    onImageFetched(Uri.parse(it))
                } ?: onImageFetched(null)
            }.onFailure {
                onImageFetched(null)
            }
        }
    }
}