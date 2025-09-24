package com.example.spooder.screen.room

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
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
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import android.content.Context
import androidx.compose.runtime.remember

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Room(navController: NavController, viewModel: RoomViewModel = viewModel()) {
    Scaffold(
        bottomBar = { FooterRoom(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(color = Color(0xFFFFFFFF))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PomodoroSection(viewModel)
            Spacer(modifier = Modifier.height(24.dp))
            TimeTableScreen(viewModel)
        }
    }
}

@Composable
fun PomodoroSection(viewModel: RoomViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pomodoro_settings", Context.MODE_PRIVATE) }

    var showSettings by remember { mutableStateOf(false) }
    var focusTimeMinutes by remember { mutableStateOf(sharedPrefs.getInt("focus_time", 25)) }
    var breakTimeMinutes by remember { mutableStateOf(sharedPrefs.getInt("break_time", 5)) }
    var remainingTime by remember { mutableStateOf(focusTimeMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var isBreak by remember { mutableStateOf(false) }
    var tempFocusTime by remember { mutableStateOf(focusTimeMinutes.toString()) }
    var tempBreakTime by remember { mutableStateOf(breakTimeMinutes.toString()) }

    val todayStats by viewModel.todayStats.observeAsState()
    val error by viewModel.error.observeAsState()

    fun saveSettings(focusTime: Int, breakTime: Int) {
        with(sharedPrefs.edit()) {
            putInt("focus_time", focusTime)
            putInt("break_time", breakTime)
            apply()
        }
    }

    Text(
        "Pomodoro Timer",
        color = Color(0xFF000000),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Left side - Stats
        Box(
            modifier = Modifier
                .weight(1f)
                .height(185.dp)
                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Today's Stats",
                    color = Color(0xFF000000),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                // Stats content
            }
        }

        // Right side - Timer
        Box(
            modifier = Modifier
                .weight(1f)
                .height(185.dp)
                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Timer content
        }
    }
    if (showSettings) {
        // ... AlertDialog code
    }

    if (error != null) {
        // ... Error AlertDialog code
    }
}


@Composable
fun TimeTableScreen(viewModel: RoomViewModel) {

    LaunchedEffect(Unit) {
        viewModel.fetchUserData()
    }

    val mostTimeUsers by viewModel.mostTimeUsers.observeAsState(emptyList())
    val otherUsers by viewModel.otherUsers.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val firstUser = mostTimeUsers.firstOrNull()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Stats bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
        ) {
            // Stats bar content
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leaderboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF0B5D48), shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Leaderboard",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when {
                    isLoading -> {
                        // Loading indicator
                    }
                    error != null -> {
                        // Retry button
                    }
                    else -> {
                        Text(
                            text = "Time",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 150.dp)
                                .fillMaxWidth()
                        ) {
                            items(mostTimeUsers) { user ->
                                TimeEntryRow(user = user)
                            }
                        }

                        Text(
                            text = "Score",
                            color = Color.White,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        LazyColumn(
                            modifier = Modifier
                                .heightIn(max = 150.dp)
                                .fillMaxWidth()
                        ) {
                            items(otherUsers) { user ->
                                ScoreEntryRow(user = user)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeEntryRow(user: UserData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "User avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(id = R.drawable._0)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = user.name, color = Color.White, fontSize = 16.sp)
        }
        Text(text = user.hours, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ScoreEntryRow(user: UserData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "User avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                fallback = painterResource(id = R.drawable._0)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = user.name, color = Color.White, fontSize = 16.sp)
        }
        Text(text = "Score: ${user.score.toInt()}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FooterRoom(navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("Home") },
            icon = { Image(painter = painterResource(id = R.drawable.home_alt_4), contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("Room") },
            icon = { Image(painter = painterResource(id = R.drawable.group_3), contentDescription = "Room") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("calendar") },
            icon = { Image(painter = painterResource(id = R.drawable.icon), contentDescription = "Calendar") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("Chat") },
            icon = { Image(painter = painterResource(id = R.drawable.vector1), contentDescription = "Chat") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("Accent") },
            icon = { Image(painter = painterResource(id = R.drawable.vector), contentDescription = "Profile") }
        )
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
