package com.example.spooder.screen.room

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.spooder.R
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.CircularProgressIndicator
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.material3.TextButton

@SuppressLint("DefaultLocale")
@Composable
fun Room(viewModel: RoomViewModel = viewModel()) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pomodoro_settings", Context.MODE_PRIVATE) }

    var showSettings by remember { mutableStateOf(false) }

    var focusTimeMinutes by remember {
        mutableStateOf(sharedPrefs.getInt("focus_time", 25))
    }
    var breakTimeMinutes by remember {
        mutableStateOf(sharedPrefs.getInt("break_time", 5))
    }

    var remainingTime by remember { mutableStateOf(focusTimeMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }
    var tempFocusTime by remember { mutableStateOf(focusTimeMinutes.toString()) }
    var tempBreakTime by remember { mutableStateOf(breakTimeMinutes.toString()) }

    val currentSession by viewModel.currentSession.observeAsState()
    val todayStats by viewModel.todayStats.observeAsState()
    val error by viewModel.error.observeAsState()

    fun saveSettings(focusTime: Int, breakTime: Int) {
        with(sharedPrefs.edit()) {
            putInt("focus_time", focusTime)
            putInt("break_time", breakTime)
            apply()
        }
    }

    // Effect to handle session completion
    LaunchedEffect(remainingTime) {
        if (remainingTime == 0 && currentSession != null) {
            isRunning = false
        }
    }

    Column(
        modifier = Modifier
            .offset(x=10.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .background(color = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color(0xFFFFFFFF),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(top = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Pomodoro Timer",
                color = Color(0xFF000000),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp, start = 10.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 2.dp, start = 7.dp, end = 7.dp)
                    .fillMaxWidth()
            ) {
                // Left side - Stats
                Box(
                    modifier = Modifier
                        .width(185.dp)
                        .height(179.dp)
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            "Today's Stats",
                            color = Color(0xFF000000),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Focus",
                                    color = Color(0xFF46B456),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${todayStats?.total_focus_minutes ?: 0}min",
                                    color = Color(0xFF46B456),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = "Completed",
                                    color = Color(0xFFECB800),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${todayStats?.completed_pomodoros ?: 0}",
                                    color = Color(0xFFECB800),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Divider(
                            color = Color(0xFFE0E0E0),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Column {
                            Text(
                                text = "Total Time",
                                color = Color(0xFF000000),
                                fontSize = 14.sp
                            )
                            Text(
                                text = formatTotalTime(todayStats?.total_focus_minutes ?: 0),
                                color = Color(0xFF000000),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Right side - Timer
                Box(
                    modifier = Modifier
                        .width(185.dp)
                        .height(185.dp)
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    LaunchedEffect(isRunning) {
                        while (isRunning && remainingTime > 0) {
                            delay(1000L)
                            remainingTime--
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clickable { showSettings = true },
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = 1f,
                                modifier = Modifier.size(100.dp),
                                color = Color(0xFFE0E0E0),
                                strokeWidth = 8.dp
                            )
                            CircularProgressIndicator(
                                progress = remainingTime.toFloat() / (if (isBreak) breakTimeMinutes * 60f else focusTimeMinutes * 60f),
                                modifier = Modifier.size(100.dp),
                                color = if (isBreak)
                                    Color(0xFFECB800).copy(alpha = 0.7f)
                                else
                                    Color(0xFF46B456).copy(alpha = 0.7f),
                                strokeWidth = 8.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${remainingTime / 60}:${String.format("%02d", remainingTime % 60)}",
                                    color = if (isBreak) Color(0xFFECB800) else Color(0xFF46B456),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isBreak) "Break" else "Focus",
                                    color = if (isBreak) Color(0xFFECB800) else Color(0xFF46B456),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                isRunning = !isRunning
                            },
                            modifier = Modifier
                                .width(170.dp)
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = if (isRunning) "Pause" else "Start",
                                color = if (isBreak) Color(0xFFECB800) else Color(0xFF46B456)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = {
                Text(
                    "Pomodoro Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "Focus Time (minutes):",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = tempFocusTime,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^[0-9]*$"))) {
                                tempFocusTime = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Break Time (minutes):",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = tempBreakTime,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^[0-9]*$"))) {
                                tempBreakTime = it
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        tempFocusTime.toIntOrNull()?.let { newFocusTime ->
                            if (newFocusTime > 0) {
                                focusTimeMinutes = newFocusTime
                                saveSettings(newFocusTime, breakTimeMinutes)
                                if (!isBreak) {
                                    remainingTime = focusTimeMinutes * 60
                                }
                            }
                        }
                        tempBreakTime.toIntOrNull()?.let { newBreakTime ->
                            if (newBreakTime > 0) {
                                breakTimeMinutes = newBreakTime
                                saveSettings(focusTimeMinutes, newBreakTime)
                                if (isBreak) {
                                    remainingTime = breakTimeMinutes * 60
                                }
                            }
                        }
                        tempFocusTime = focusTimeMinutes.toString()
                        tempBreakTime = breakTimeMinutes.toString()
                        showSettings = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBreak) Color(0xFFECB800) else Color(0xFF46B456)
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        tempFocusTime = focusTimeMinutes.toString()
                        tempBreakTime = breakTimeMinutes.toString()
                        showSettings = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Error dialog
    if (error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
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

@Composable
fun TimeTableScreen() {
    val context = LocalContext.current
    val viewModel: RoomViewModel = viewModel(
        factory = RoomViewModelFactory(context.applicationContext as Application)
    )

    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    val mostTimeUsers by viewModel.mostTimeUsers.observeAsState(emptyList())
    val otherUsers by viewModel.otherUsers.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val firstUser = mostTimeUsers.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Stats bar with rounded corners and proper spacing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 250.dp)
                .height(64.dp)
                .background(
                    color = Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Completed Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = firstUser?.completedTasks?.toString() ?: "0",
                        color = Color(0xFF46B456),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed",
                        color = Color(0xFF46B456),
                        fontSize = 14.sp
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Pending Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = firstUser?.pendingTasks?.toString() ?: "0",
                        color = Color(0xFFECB800),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pending",
                        color = Color(0xFFECB800),
                        fontSize = 14.sp
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .width(1.dp)
                        .background(Color(0xFFE0E0E0))
                )

                // Total Time Column
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = firstUser?.hours ?: "0h",
                        color = Color(0xFF000000),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Time",
                        color = Color(0xFF000000),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Leaderboard Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 270.dp)
                .height(410.dp)
                .background(
                    color = Color(0xFF0B5D48),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Leaderboard",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.retryFetch() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                    else -> {
                        // Time section
                        Text(
                            text = "Time",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .height(110.dp)
                                .fillMaxWidth()
                        ) {
                            items(mostTimeUsers) { user ->
                                TimeEntryRow(
                                    name = user.name,
                                    hours = user.hours,
                                    avatarUrl = user.avatarUrl
                                )
                            }
                        }

                        // Score section
                        Text(
                            text = "Score",
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )

                        LazyColumn(
                            modifier = Modifier
                                .height(130.dp)
                                .fillMaxWidth()
                        ) {
                            items(otherUsers) { user ->
                                ScoreEntryRow(
                                    name = user.name,
                                    score = user.score,
                                    avatarUrl = user.avatarUrl
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeEntryRow(name: String, hours: String, avatarUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFF2BBA90),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Text(
            text = hours,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScoreEntryRow(name: String, score: Float, avatarUrl: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "User avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFF2BBA90),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = name,
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Text(
            text = "Score: ${score.toInt()}",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FooterRoom(navController: NavController) {
    Column(
        modifier = Modifier
            .offset(y = 780.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xFFFFFFFF),
            )
            .padding(vertical = 10.dp, horizontal = 36.dp)
            .zIndex(7F)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 6.dp)
                .fillMaxWidth()
        ) {
            IconButton(
                onClick = { navController.navigate("Home") },
                modifier = Modifier
                    .width(50.dp)
                    .height(40.dp)
                    .padding(end = 5.dp )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_alt_4),
                    contentDescription = "I",
                )
            }
            IconButton(
                onClick = { navController.navigate("Room") },
                modifier = Modifier
                    .padding(start = 25.dp , end = 25.dp , bottom = 10.dp)
                    .width(40.dp)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.group_3),
                    contentDescription = "I",
                )
            }
            IconButton(
                onClick = { navController.navigate("calendar") },
                modifier = Modifier
                    .padding(end = 35.dp, bottom = 10.dp)
                    .width(40.dp)
                    .height(40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = "I",
                )
            }
            IconButton(
                onClick = { navController.navigate("Chat") },
                modifier = Modifier
                    .padding(end = 25.dp, bottom = 10.dp)
                    .width(40.dp)
                    .height(40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vector1),
                    contentDescription = "I",
                )
            }
            IconButton(
                onClick = { navController.navigate("Accent") },
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .width(50.dp)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.vector),
                    contentDescription = "I",
                )
            }
        }
    }
}

class RoomViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}