package com.example.spooder.screen

import android.app.Application
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spooder.R
import com.example.spooder.model.Task
import com.example.spooder.screen.calendar.TaskViewModel
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

// ViewModel Factory
class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

// Main Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(context.applicationContext as Application)
    )

    val today by rememberSaveable {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(Calendar.getInstance().time))
    }

    LaunchedEffect(today) {
        viewModel.fetchTasksByDate(today)
    }

    Scaffold(
        bottomBar = { Footer(navController = navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Hero()
            TodoApp(viewModel, navController)
        }
    }
}

@Composable
fun TodoApp(
    viewModel: TaskViewModel,
    navController: NavController
) {
    val tasks by viewModel.tasks.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val error by viewModel.error.observeAsState()

    // This Column will be placed correctly by the Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 240.dp) // Adjust this padding as needed to position below the Hero
    ) {
        Header(navController)
        when {
            isLoading -> LoadingIndicator()
            error != null -> ErrorMessage(error!!)
            else -> TodoList(todos = tasks, viewModel = viewModel)
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF00664F),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Error: $error",
            color = Color.Red,
            fontSize = 16.sp
        )
    }
}

@Composable
fun TodoList(
    todos: List<Task>,
    viewModel: TaskViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(color = Color(0xFF00664F)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
    ) {
        if (todos.isEmpty()) {
            item {
                EmptyTasksMessage()
            }
        } else {
            items(
                items = todos,
                key = { task -> task.id }
            ) { task ->
                TodoItem(task, viewModel)
            }
        }
    }
}

@Composable
private fun EmptyTasksMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "No tasks for today",
            color = Color.White,
            fontSize = 18.sp
        )
    }
}

@Composable
fun TodoItem(
    task: Task,
    viewModel: TaskViewModel
) {
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val dismissThreshold = 100f

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(300),
        label = "offset"
    )

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteTask(task)
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        SwipeBackground()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX < -dismissThreshold -> {
                                    showDeleteDialog = true
                                    offsetX = 0f
                                }
                                offsetX > dismissThreshold -> {
                                    viewModel.toggleTaskCompletion(task.id, !task.is_completed)
                                    offsetX = 0f
                                }
                                else -> offsetX = 0f
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(-200f, 200f)
                        }
                    )
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TaskTitle(task)
                TaskDetails(task)
            }
        }
    }
}

@Composable
private fun SwipeBackground() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2BBA90))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Complete Task",
                tint = Color.White,
                modifier = Modifier
                    .padding(start = 24.dp)
                    .size(30.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(Color(0xFFEC0000)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Task",
                tint = Color.White,
                modifier = Modifier
                    .padding(end = 24.dp)
                    .size(30.dp)
            )
        }
    }
}

@Composable
private fun TaskTitle(task: Task) {
    Text(
        text = task.title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        style = if (task.is_completed) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun TaskDetails(task: Task) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskInfo(task)
        TaskTime(task)
    }
}

@Composable
private fun TaskInfo(task: Task) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DayOfWeekBox(task)
        ProgressIndicators(task)
    }
}

@Composable
private fun DayOfWeekBox(task: Task) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .width(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(getTaskColor(task.color)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getDayOfWeek(task.date),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun ProgressIndicators(task: Task) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val score = task.color.toIntOrNull() ?: 1
        val color = when (score) {
            3 -> Color(0xFFEC0000)  // red
            2 -> Color(0xFFECB800)  // yellow
            1 -> Color(0xFF2BBA90)  // green
            else -> Color(0xFF2BBA90)
        }

        repeat(3) { index ->
            ProgressIndicator(
                color = color,
                isActive = index < score
            )
        }
    }
}

@Composable
private fun TaskTime(task: Task) {
    Text(
        text = task.time_start ?: "",
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF4A3BF7)
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Task") },
        text = { Text("Are you sure you want to delete this task?") },
        confirmButton = {
            OutlinedButton(
                onClick = onConfirm,
                border = BorderStroke(1.dp, Color(0xFFEC0000)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEC0000))
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color(0xFF2BBA90)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2BBA90))
            ) {
                Text("Cancel")
            }
        }
    )
}

// Helper Functions
private fun getTaskColor(color: String): Color {
    return when (color.lowercase()) {
        "1", "green" -> Color(0xFF2BBA90)
        "2", "yellow" -> Color(0xFFECB800)
        "3", "red" -> Color(0xFFEC0000)
        else -> Color(0xFF2BBA90)
    }
}

fun getDayOfWeek(dateString: String): String {
    return try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date ?: Calendar.getInstance().time
        SimpleDateFormat("EEE", Locale.ENGLISH).format(calendar.time)
    } catch (e: Exception) {
        try {
            val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
            val date = dayFormat.parse(dateString)
            val calendar = Calendar.getInstance()
            calendar.time = date ?: Calendar.getInstance().time
            SimpleDateFormat("EEE", Locale.ENGLISH).format(calendar.time)
        } catch (e: Exception) {
            "Mon"
        }
    }
}

// Progress indicator component
@Composable
fun ProgressIndicator(color: Color, isActive: Boolean) {
    Box(
        modifier = Modifier
            .size(width = 30.dp, height = 15.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, topStart = 0.dp, topEnd = 2.dp, bottomEnd = 2.dp))
            .background(
                if (isActive) color
                else Color.LightGray.copy(alpha = 0.3f)
            )
    )
}

// Layout Components
@Composable
fun Hero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Adjusted height
            .background(color = Color(0xFF00664F))
    ) {
        Image(
            painter = painterResource(id = R.drawable.image),
            contentDescription = "Hero image",
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter)
        )
    }
}


@Composable
private fun Header(navController: NavController) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { navController.navigate("calendar") }
    ) {
        Icon(
            painter = painterResource(R.drawable.icon1),
            contentDescription = "Todo",
            tint = Color.White,
            modifier = Modifier
                .size(36.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("To-Do", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Footer(navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color.Gray
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate("Home") },
            icon = { Image(painter = painterResource(id = R.drawable.home_alt_2), contentDescription = "Home") }
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("Room") },
            icon = { Image(painter = painterResource(id = R.drawable.icons8_user_account_96), contentDescription = "Room") }
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
