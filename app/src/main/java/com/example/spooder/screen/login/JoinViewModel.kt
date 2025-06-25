package com.example.spooder.screen.login

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor(
    private val application: Application
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    private val prefs: SharedPreferences = application.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    private val supabase = createSupabaseClient(
        supabaseUrl = "https://lholzspyazziknxqopmi.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imxob2x6c3B5YXp6aWtueHFvcG1pIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDIwMjc0MTAsImV4cCI6MjA1NzYwMzQxMH0.uku06OF-WapBhuV-A_rJBXu3x24CKKkSTM0SnmPIOOE"
    ) {
        install(Postgrest) {
            defaultSerializer = io.github.jan.supabase.serializer.KotlinXSerializer(json)
        }
        install(GoTrue)
        install(Storage)
    }

    init {
        Log.d("JoinViewModel", "Supabase client initialized with URL: ${supabase.supabaseUrl}")
    }

    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state = _state.asStateFlow()

    fun signIn(email: String, password: String) {
        _state.value = SignInState.Loading

        viewModelScope.launch {
            try {
                supabase.gotrue.loginWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Save login state
                prefs.edit().putBoolean("is_logged_in", true).apply()
                _state.value = SignInState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _state.value = SignInState.Error
            }
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }
}

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Success : SignInState()
    object Error : SignInState()
}
