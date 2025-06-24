package com.example.spooder

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.spooder.model.Channel
import com.example.spooder.model.Message
import com.example.spooder.model.Task
import com.example.spooder.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.modules.SerializersModule
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import com.example.spooder.model.ChannelMember
import kotlinx.coroutines.delay
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds


@Singleton
class SupabaseService @Inject constructor(
    @ApplicationContext private val context: Context
){
    companion object {
        private const val SUPABASE_URL = ""
        private const val SUPABASE_KEY = ""
        private const val MAX_RETRIES = 3
        private const val TIMEOUT_SECONDS = 30L
        private const val BUCKET_NAME = "spooderimage"

        @Volatile
        private var instance: SupabaseService? = null

        fun getInstance(context: Context): SupabaseService {
            return instance ?: synchronized(this) {
                instance ?: SupabaseService(context).also { instance = it }
            }
        }
    }

    private val appContext = context.applicationContext
    private val json = Json { ignoreUnknownKeys = true }

    private val serializersModule = SerializersModule {
        contextual(UUID::class, UUIDSerializer())
    }

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest) {
            defaultSerializer = KotlinXSerializer(json)
        }
        install(GoTrue)
        install(Realtime)
        install(Storage)
    }

    class UUIDSerializer : KSerializer<UUID> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): UUID {
            return UUID.fromString(decoder.decodeString())
        }
    }

    init {
        Log.d("SupabaseService", "Supabase client initialized with URL: ${client.supabaseUrl}")
        try {
            val currentUser = client.gotrue.currentUserOrNull()
            Log.d("SupabaseService", "Current user at initialization: ${currentUser?.id ?: "No user"}")
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error checking current user at initialization: ${e.message}")
        }
    }

    private suspend fun <T> executeWithRetry(
        maxRetries: Int = MAX_RETRIES,
        operation: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        var currentDelay = 1000L

        repeat(maxRetries) { attempt ->
            try {
                return@withContext Result.success(
                    withTimeout(TIMEOUT_SECONDS.seconds) {
                        operation()
                    }
                )
            } catch (e: Exception) {
                lastException = e
                Log.e("SupabaseService", "Attempt ${attempt + 1} failed: ${e.message}")
                if (attempt < maxRetries - 1) {
                    delay(currentDelay)
                    currentDelay *= 2
                }
            }
        }

        Result.failure(lastException ?: Exception("Operation failed after $maxRetries retries"))
    }

    suspend fun getAllTasks(): Result<List<Task>> = executeWithRetry {
        client.postgrest["tasks"].select().decodeList()
    }

    suspend fun getTasksByDate(date: String): Result<List<Task>> = executeWithRetry {
        client.postgrest["tasks"]
            .select {
                eq("date", date)
                order("time_start", Order.ASCENDING)
            }
            .decodeList<Task>()
    }

    suspend fun getChannels(): Result<List<Channel>> {
        return try {
            val userId = client.gotrue.currentUserOrNull()?.id ?: return Result.success(emptyList())

            // اول اعضای کانال‌هایی که کاربر در آن‌ها عضو است را بگیریم
            val membershipResponse = client.postgrest["channel_members"]
                .select {
                    eq("user_id", userId)
                }
                .decodeList<ChannelMember>()

            // بعد برای هر کانال، اطلاعات کانال را بگیریم
            val channels = mutableListOf<Channel>()
            for (membership in membershipResponse) {
                try {
                    val channelResponse = client.postgrest["channels"]
                        .select {
                            eq("id", membership.channel_id)
                        }
                        .decodeSingle<Channel>()
                    channels.add(channelResponse)
                } catch (e: Exception) {
                    Log.e("SupabaseService", "Error fetching channel ${membership.channel_id}: ${e.message}")
                }
            }

            Result.success(channels)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching channels: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getChannelById(id: String): Result<Channel> {
        return try {
            val response = client.postgrest["channels"]
                .select() {
                    eq("id", id)
                }
                .decodeSingle<JsonObject>()

            val channel = Channel(
                id = response["id"]?.jsonPrimitive?.content ?: "",
                name = response["name"]?.jsonPrimitive?.content ?: "Unnamed Channel",
                created_at = response["created_at"]?.jsonPrimitive?.content ?: "",
                imageUrl = response["imageUrl"]?.jsonPrimitive?.content
            )

            Result.success(channel)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching channel: ${e.message}")
            Result.failure(e)
        }
    }



    suspend fun getTasksByDateAndUser(date: String, userId: String): Result<List<Task>> = executeWithRetry {
        client.postgrest["tasks"]
            .select {
                eq("date", date)
                eq("user_id", userId)
                order("time_start", Order.ASCENDING)
            }
            .decodeList<Task>()
    }

    suspend fun addTask(task: Task): Result<Task> = executeWithRetry {
        client.postgrest["tasks"]
            .insert(task)
            .decodeSingle()
    }

    suspend fun toggleTaskCompletion(taskId: String, isCompleted: Boolean): Result<Boolean> = executeWithRetry {
        Log.d("SupabaseService", "Updating task $taskId completion status to: $isCompleted")
        val updates = mapOf("is_completed" to JsonPrimitive(isCompleted))
        client.postgrest["tasks"]
            .update(JsonObject(updates)) {
                eq("id", taskId)
            }
        true
    }

    suspend fun deleteTask(taskId: String): Result<Boolean> = executeWithRetry {
        client.postgrest["tasks"]
            .delete {
                eq("id", taskId)
            }
        true
    }

    suspend fun updateTask(task: Task): Result<Boolean> = executeWithRetry {
        client.postgrest["tasks"]
            .update(task) {
                eq("id", task.id)
            }
        true
    }


// فقط متد getAllUsers اصلاح شده - بقیه کد همان باشد

    suspend fun getAllUsers(): Result<List<User>> = executeWithRetry {
        try {
            Log.d("SupabaseService", "Fetching all users...")

            // فقط از decodeList بدون JsonObject استفاده کن
            val users = client.postgrest["profiles"]
                .select()
                .decodeList<User>()

            Log.d("SupabaseService", "Successfully fetched ${users.size} users")
            users

        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in getAllUsers: ${e.message}", e)
            throw e
        }
    }

    suspend fun getUserById(userId: String): Result<User> = executeWithRetry {
        client.postgrest["users"]
            .select {
                eq("id", userId)
            }
            .decodeSingle()
    }

    // Chat Operations
    suspend fun getMessages(channelId: UUID): Result<List<Message>> = withContext(Dispatchers.IO) {
        try {
            val messages = client.postgrest["messages"]
                .select {
                    eq("channel_id", channelId.toString())
                }
                .decodeList<Message>()
            Result.success(messages)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error fetching messages: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun sendMessage(message: Message): Result<Message> = withContext(Dispatchers.IO) {
        try {
            val newMessage = client.postgrest["messages"]
                .insert(message)
                .decodeSingle<Message>()
            Result.success(newMessage)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error sending message: ${e.message}")
            Result.failure(e)
        }
    }


    suspend fun createChannel(name: String, imageUrl: String?, memberIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val creatorId = client.gotrue.currentUserOrNull()?.id ?: throw IllegalStateException("User not logged in")

            // ۱. کانال جدید را بساز
            val newChannel = client.postgrest["channels"]
                .insert(Channel(name = name, imageUrl = imageUrl))
                .decodeSingle<Channel>()

            // ۲. اعضای انتخاب شده (به علاوه‌ی خود سازنده) را به جدول اعضا اضافه کن
            val allMemberIds = (memberIds + creatorId).toSet() // toSet برای جلوگیری از تکرار
            val members = allMemberIds.map { memberId ->
                ChannelMember(channel_id = newChannel.id!!, user_id = memberId)
            }

            client.postgrest["channel_members"].insert(members)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChannelUsers(channelId: String): Result<List<User>> = executeWithRetry {
        client.postgrest["channel_users"]
            .select {
                eq("channel_id", channelId)
            }
            .decodeList()
    }

    // Auth Operations
    fun getCurrentUserId(): UUID? {
        try {
            val user = client.gotrue.currentUserOrNull()
            Log.d("SupabaseService", "getCurrentUserId called, user: ${user?.id ?: "null"}")

            // اگر شناسه کاربر یک رشته است، آن را به UUID تبدیل کنید
            return if (user?.id is String) {
                UUID.fromString(user.id as String)
            } else {
                user?.id as UUID?
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error getting current user ID: ${e.message}", e)
            return null
        }
    }

    fun getCurrentUserEmail(): String? {
        try {
            val user = client.gotrue.currentUserOrNull()
            Log.d("SupabaseService", "Current user email: ${user?.email}")
            return user?.email
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error getting current user email: ${e.message}")
            return null
        }
    }

    suspend fun getCurrentUser(): Result<User?> = executeWithRetry {
        try {
            val user = client.gotrue.currentUserOrNull()
            if (user == null) {
                Log.d("SupabaseService", "No current user found")
            null
        } else {
                Log.d("SupabaseService", "Current user found: ${user.id}")
                val userId = user.id.toString()
                client.postgrest["profiles"]
                .select {
                    eq("id", userId)
                }
                .decodeSingleOrNull()
            }
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error in getCurrentUser: ${e.message}")
            null
        }
    }

    suspend fun getTasksByDateRange(startDate: String, endDate: String): Result<List<Task>> = executeWithRetry {
        client.postgrest["tasks"]
            .select {
                gte("date", startDate)  // greater than or equal to start date
                lte("date", endDate)    // less than or equal to end date
                order("date", Order.ASCENDING)
                order("time_start", Order.ASCENDING)
            }
            .decodeList<Task>()
    }

    suspend fun getTasksForLastWeek(userId: String): Result<List<Task>> = executeWithRetry {
        // Calculate dates for the last week
        val calendar = Calendar.getInstance()
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Go back 7 days
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        client.postgrest["tasks"]
            .select {
                eq("user_id", userId)
                gte("date", startDate)
                lte("date", endDate)
                order("date", Order.ASCENDING)
                order("time_start", Order.ASCENDING)
            }
            .decodeList<Task>()
    }

    suspend fun uploadImage(uri: Uri, string: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val file = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                val fileName = "${UUID.randomUUID()}.jpg"
                val bucket = "spooderimage"
                client.storage[bucket].upload(fileName, bytes)
                // Get the public URL
                val publicUrl = client.storage[bucket].publicUrl(fileName)
                publicUrl
            }
            file?.let { Result.success(it) } ?: Result.failure(Exception("Failed to upload image"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun addUserToChannel(channelId: UUID, userId: UUID?): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (userId == null) {
                return@withContext Result.failure(Exception("User ID is null"))
            }

            // Check if the user is already in the channel
            val existingUser = client.postgrest["channel_users"]
                .select {
                    eq("channel_id", channelId.toString())
                    eq("user_id", userId.toString())
                }
                .decodeList<JsonObject>()

            if (existingUser.isNotEmpty()) {
                Log.d("SupabaseService", "User $userId already exists in channel $channelId")
                return@withContext Result.success(true) // User already in channel, return success
            }

            val data = mapOf(
                "channel_id" to channelId.toString(),
                "user_id" to userId.toString()
            )

            client.postgrest["channel_users"].insert(data)
            Result.success(true)
        } catch (e: Exception) {
            Log.e("SupabaseService", "Error adding user to channel: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun uploadChannelImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Validate file type
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType == null || !mimeType.startsWith("image/")) {
                return@withContext Result.failure(Exception("Invalid file type. Only images are allowed."))
            }

            // Use the existing uploadImage method with the correct bucket name
            val result = uploadImage(uri, "channel_images")
            if (result.isSuccess) {
                Result.success(result.getOrNull() ?: throw Exception("Failed to get image URL"))
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Failed to upload image"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class User(
    val id: UUID,
    val username: String
)
