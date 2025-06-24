package com.example.spooder.screen.login

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spooder.R

@Composable
fun Registration(navController: NavController) {
    val viewModel: RegistrationViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var context = LocalContext.current

    LaunchedEffect(key1 = uiState.value) {
        when (uiState.value) {
            is SignInStatef.Success -> {
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                navController.navigate("Home")
            }
            is SignInStatef.Error -> {
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(color = Color(0xFFFFFFFF))
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF00664F), Color(0xFFFFFFFF)),
                                    start = Offset.Zero,
                                    end = Offset(0F, Float.POSITIVE_INFINITY),
                                )
                            )
                            .padding(top = 110.dp, bottom = 300.dp)
                    ) {
                        Text(
                            "Join the room",
                            color = Color(0xFFFFFFFF),
                            fontSize = 31.sp,
                            modifier = Modifier
                                .padding(bottom = 39.dp, start = 125.dp)
                        )
                        Text(
                            "Username",
                            color = Color(0xFFFFFFFF),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(bottom = 10.dp, start = 20.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                fontSize = 25.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(bottom = 0.dp, start = 10.dp)
                                .offset(y = 18.dp, x = (-12).dp)
                        )
                        Column(
                            modifier = Modifier
                                .padding(bottom = 43.dp, start = 13.dp, end = 13.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFFFFF),
                                )
                                .height(2.dp)
                                .fillMaxWidth()
                        ) {
                        }
                        Text(
                            "Name",
                            color = Color(0xFFFFFFFF),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(bottom = 10.dp, start = 20.dp)
                        )
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                fontSize = 25.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(bottom = 0.dp, start = 10.dp)
                                .offset(y = 18.dp, x = (-12).dp)
                        )
                        Column(
                            modifier = Modifier
                                .padding(bottom = 43.dp, start = 13.dp, end = 13.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFFFFF),
                                )
                                .height(2.dp)
                                .fillMaxWidth()
                        ) {
                        }
                        Text(
                            "E-mail",
                            color = Color(0xFFFFFFFF),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(bottom = 10.dp, start = 20.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                fontSize = 25.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(bottom = 0.dp, start = 10.dp)
                                .offset(y = 18.dp, x = (-12).dp)
                        )
                        Column(
                            modifier = Modifier
                                .padding(bottom = 43.dp, start = 13.dp, end = 13.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFFFFF),
                                )
                                .height(2.dp)
                                .fillMaxWidth()
                        ) {
                        }
                        Text(
                            "Password",
                            color = Color(0xFFFFFFFF),
                            fontSize = 20.sp,
                            modifier = Modifier
                                .padding(bottom = 10.dp, start = 20.dp)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = TextStyle(
                                fontSize = 25.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier
                                .padding(bottom = 0.dp, start = 10.dp)
                                .offset(y = 18.dp, x = (-12).dp)
                        )
                        Column(
                            modifier = Modifier
                                .padding(bottom = 50.dp, start = 13.dp, end = 13.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFFFFFFF),
                                )
                                .height(2.dp)
                                .fillMaxWidth()
                        ) {
                        }
                        Row(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 25.dp)
                                .height(58.dp)
                                .fillMaxWidth()
                        ) {
                            Text("I agree with the terms and conditions",
                                color = Color(0xFFFFFFFF),
                                fontSize = 15.sp,
                                modifier = Modifier
                                    .padding(top = 0.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                            }
                            if (uiState.value == SignInStatef.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.padding(10.dp)
                                )
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        Toast.makeText(context, "Attempting to register...", Toast.LENGTH_SHORT).show()
                                        viewModel.register(username, name, email, password)
                                    },
                                    modifier = Modifier
                                        .height(60.dp)
                                        .width(120.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .offset(y = (-0).dp)
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.arrow_forward),
                                            contentDescription = "I",
                                            modifier = Modifier
                                                .padding(end = 15.dp, start = 0.dp)
                                                .width(25.dp)
                                                .height(25.dp)
                                        )
                                        Text(
                                            text = "JOIN",
                                            color = Color(0xFFFFFFFF),
                                            fontSize = 15.sp,
                                            modifier = Modifier
                                                .offset(y = (-0).dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


