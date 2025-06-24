package com.example.spooder.screen.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spooder.SupabaseService
import com.example.spooder.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseService: SupabaseService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun sendMessage(
        channelId: UUID,
        content: String,
        senderId: String?,
        senderName: String,
        senderAvatarUrl: String?,
        imageUrl: String? = null
    ) {
        viewModelScope.launch {
            try {
                val message = Message(
                    id = UUID.randomUUID().toString(),
                    channel_id = channelId.toString(),
                    content = content,
                    sender_id = senderId,
                    created_at = null,
                    imageUrl = imageUrl,
                    senderName = senderName,
                    senderAvatarUrl = senderAvatarUrl
                )

                supabaseService.sendMessage(message).onSuccess {
                    fetchMessages(channelId)
                }.onFailure { e ->
                    _error.value = "Error sending message: ${e.message}"
                    Log.e("ChatViewModel", "Supabase send failed: ${e.message}", e)
                }

            } catch (e: Exception) {
                _error.value = "General error sending message: ${e.message}"
                Log.e("ChatViewModel", "General error sending message: ${e.message}", e)
            }
        }
    }

    fun sendImageMessage(
        uri: Uri,
        channelId: UUID,
        senderId: String?,
        senderName: String,
        senderAvatarUrl: String?
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Uploading image...", Toast.LENGTH_SHORT).show()
            }
            try {
                supabaseService.uploadImage(uri, "channel_images").onSuccess { imageUrl ->
                    sendMessage(channelId, "", senderId, senderName, senderAvatarUrl, imageUrl)
                }.onFailure { e ->
                    _error.value = "Error sending image: ${e.message}"
                }
            } catch (e: Exception) {
                _error.value = "Error sending image: ${e.message}"
            }
        }
    }

    fun fetchMessages(channelId: UUID) {
        viewModelScope.launch {
            try {
                supabaseService.getMessages(channelId).onSuccess { messages ->
                    _messages.value = messages
                }.onFailure { e ->
                    _error.value = "Error fetching messages: ${e.message}"
                    Log.e("ChatViewModel", "Error fetching messages: ${e.message}", e)
                }
            } catch (e: Exception) {
                _error.value = "Error fetching messages: ${e.message}"
                Log.e("ChatViewModel", "Error fetching messages: ${e.message}", e)
            }
        }
    }

    fun registerUserToChannel(channelId: UUID) {
        viewModelScope.launch {
            try {
                val userId = supabaseService.getCurrentUserId()
                if (userId != null) {
                    supabaseService.addUserToChannel(channelId, userId).onFailure { e ->
                        _error.value = "Error registering user to channel: ${e.message}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error registering user to channel: ${e.message}"
            }
        }
    }

    fun getChannelImage(channelId: String, onImageFetched: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val channel = supabaseService.getChannelById(channelId).getOrNull()
                val imageUrl = channel?.imageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    onImageFetched(Uri.parse(imageUrl))
                } else {
                    onImageFetched(null)
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching channel image: ${e.message}")
                onImageFetched(null)
            }
        }
    }
}