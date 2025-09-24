package com.example.payeh.screen.accent_pass

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.payeh.SupabaseService
import com.example.payeh.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
@HiltViewModel
@Suppress("CAST_NEVER_SUCCEEDS")
class AccentViewModel @Inject constructor(
    private val supabaseService: SupabaseService
) : ViewModel() {

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val _notificationEnabled = MutableStateFlow(false)
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus: StateFlow<String?> = _updateStatus.asStateFlow()


    init {
        fetchUserProfile()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                supabaseService.client.gotrue.logout()
                _userState.value = null
                _notificationEnabled.value = false
                Log.d("AccentViewModel", "User logged out successfully")
            } catch (e: Exception) {
                Log.e("AccentViewModel", "Error during logout: ${e.message}")
            }
        }
    }

    fun checkNotificationStatus(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        _notificationEnabled.value = notificationManager.areNotificationsEnabled()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleNotifications(context: Context, onComplete: () -> Unit = {}) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
        onComplete()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val user = supabaseService.client.gotrue.currentUserOrNull()
                if (user != null) {
                    try {
                        val response = supabaseService.client.postgrest["profiles"]
                            .select { eq("id", user.id) }
                            .decodeSingle<JsonObject>()
                        Log.d("AccentViewModel", "Profile response: $response")
                        _userState.value = User(
                            id = response["id"]?.jsonPrimitive?.content ?: user.id,
                            email = response["email"]?.jsonPrimitive?.content ?: user.email ?: "",
                            username = response["username"]?.jsonPrimitive?.content,
                            total_minutes = response["total_minutes"]?.jsonPrimitive?.intOrNull ?: 0,
                            completed_tasks = response["completed_tasks"]?.jsonPrimitive?.intOrNull ?: 0,
                            pending_tasks = response["pending_tasks"]?.jsonPrimitive?.intOrNull ?: 0,
                            created_at = response["created_at"]?.jsonPrimitive?.content ?: "",
                            updated_at = response["updated_at"]?.jsonPrimitive?.content ?: "",
                            avatar_url = response["avatar_url"]?.jsonPrimitive?.content,
                            last_sign_in = response["last_sign_in"]?.jsonPrimitive?.content
                        )
                    } catch (e: Exception) {
                        Log.e("AccentViewModel", "Error fetching profile from db, creating fallback: ${e.message}")
                        val metadata = user.userMetadata
                        val name = metadata?.get("username") as? String ?: user.email ?: "Guest User"
                        // **اصلاح خطا: پاس دادن مقادیر خالی برای پارامترهای خواسته شده**
                        _userState.value = User(
                            id = user.id,
                            email = user.email ?: "",
                            username = name,
                            created_at = "", // اضافه شد
                            updated_at = ""  // اضافه شد
                        )
                    }
                } else {
                    Log.d("AccentViewModel", "No user found")
                    _userState.value = null
                }
            } catch (e: Exception) {
                Log.e("AccentViewModel", "Error in fetchUserProfile: ${e.message}")
                _userState.value = null
            }
        }
    }

    suspend fun updateUserProfile(name: String, username: String): Boolean {
        _updateStatus.value = "در حال آپدیت..."
        return try {
            val user = supabaseService.client.gotrue.currentUserOrNull() ?: throw Exception("User not logged in")
            Log.d("AccentViewModel", "Updating profile for user: ${user.id} with name: $name")

            val updates = buildJsonObject {
                put("username", JsonPrimitive(username))
                put("name", JsonPrimitive(name))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    put("updated_at", JsonPrimitive(Instant.now().toString()))
                }
            }

            supabaseService.client.postgrest["profiles"]
                .update(updates) { eq("id", user.id) }

            Log.d("AccentViewModel", "Update request sent successfully.")
            _updateStatus.value = "آپدیت موفقیت‌آمیز بود"

            // **اصلاح مهم: حذف فراخوانی fetchUserProfile برای جلوگیری از کرش**
            // fetchUserProfile()

            true
        } catch (e: Exception) {
            Log.e("AccentViewModel", "Error updating profile: ${e.message}", e)
            _updateStatus.value = "خطا در آپدیت: ${e.message}"
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    // **اصلاح هشدار: حذف پارامتر context که استفاده نمی‌شد**
    suspend fun uploadProfileImage(imageUri: Uri): Boolean {
        _updateStatus.value = "در حال آپلود تصویر..."
        return try {
            val user = supabaseService.client.gotrue.currentUserOrNull() ?: throw Exception("User not logged in")
            Log.d("AccentViewModel", "Starting image upload for user: ${user.id}")

            val result = supabaseService.uploadImage(imageUri, "channel_images")

            if (result.isSuccess) {
                val publicUrl = result.getOrThrow()
                Log.d("AccentViewModel", "File uploaded. Public URL: $publicUrl")

                val updates = buildJsonObject {
                    put("avatar_url", JsonPrimitive(publicUrl))
                    put("updated_at", JsonPrimitive(Instant.now().toString()))
                }

                supabaseService.client.postgrest["profiles"]
                    .update(updates) { eq("id", user.id) }

                Log.d("AccentViewModel", "Profile updated with new avatar")
                _updateStatus.value = "تصویر با موفقیت آپلود شد"

                // **اصلاح مهم: حذف فراخوانی fetchUserProfile برای جلوگیری از کرش**
                // fetchUserProfile()

                true
            } else {
                throw result.exceptionOrNull() ?: Exception("Unknown upload error")
            }
        } catch (e: Exception) {
            Log.e("AccentViewModel", "Error uploading profile image: ${e.message}", e)
            _updateStatus.value = "خطا در آپلود: ${e.message}"
            false
        }
    }
}