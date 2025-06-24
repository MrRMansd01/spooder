package com.example.spooder.screen.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow<SignInStatef>(SignInStatef.Nothing)
    val state: StateFlow<SignInStatef> = _state.asStateFlow()

    fun register(username: String, name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _state.value = SignInStatef.Loading

                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .build()

                    val json = JSONObject().apply {
                        put("username", username)
                        put("name", name)
                        put("email", email)
                        put("password", password)
                    }

                    val requestBody = json.toString().toRequestBody("application/json".toMediaType())

                    val request = Request.Builder()
                        .url("http://10.0.2.2:3001/register")
                        .post(requestBody)
                        .build()

                    try {
                        val response = client.newCall(request).execute()

                        if (response.isSuccessful) {
                            Log.d("Registration", "Registration successful")
                            _state.value = SignInStatef.Success
                        } else {
                            Log.e("Registration", "Registration failed with response: ${response.code} ${response.message}")
                            _state.value = SignInStatef.Error
                        }
                    } catch (e: Exception) {
                        Log.e("Registration", "Exception during registration: ${e.message}")
                        _state.value = SignInStatef.Error
                    }
                }
            } catch (e: Exception) {
                Log.e("Registration", "General exception: ${e.message}")
                _state.value = SignInStatef.Error
            }
        }
    }
}

sealed class SignInStatef {
    object Nothing : SignInStatef()
    object Loading : SignInStatef()
    object Success : SignInStatef()
    object Error : SignInStatef()
}

