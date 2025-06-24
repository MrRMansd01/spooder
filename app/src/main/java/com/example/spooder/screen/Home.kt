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
import androidx.compose.ui.zIndex
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

    Box {
        Hero()
        Mobile()
        TodoApp(viewModel, navController)
        Footer(navController)
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

    when {
        isLoading -> LoadingIndicator()
        error != null -> ErrorMessage(error!!)
        else -> TodoList(todos = tasks, navController = navController, viewModel = viewModel)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .offset(y = 300.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.White,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun ErrorMessage(error: String) {
    Box(
        modifier = Modifier
            .offset(y = 300.dp)
            .fillMaxWidth(),
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
    navController: NavController,
    viewModel: TaskViewModel
) {
    Column(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .offset(y = 240.dp)
            .zIndex(5f),
    ) {
        Header(navController)

        LazyColumn(
            modifier = Modifier
                .padding(start = 8.dp, end = 20.dp, top = 5.dp)
                .clip(shape = RoundedCornerShape(16.dp))
                .background(color = Color(0xFF00664F), shape = RoundedCornerShape(16.dp))
                .height(435.dp)
                .width(380.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 70.dp)
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
                    key(task.id) {
                        TodoItem(task,viewModel)
                    }
                }
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
            .height(IntrinsicSize.Min)
            .padding(horizontal = 8.dp, vertical = 8.dp)
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
                    .padding(25.dp)
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
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black,
        style = if (task.is_completed) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default,
        modifier = Modifier.padding(bottom = 12.dp)
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
            .height(42.dp)
            .width(68.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(getTaskColor(task.color)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getDayOfWeek(task.date),
            fontSize = 18.sp,
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
        fontSize = 22.sp,
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
        // اگر تاریخ در فرمت yyyy-MM-dd نبود، فرمت dd رو امتحان کن
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
    Image(
        painter = painterResource(id = R.drawable.image),
        contentDescription = "Hero image",
        modifier = Modifier
            .width(400.dp)
            .height(400.dp)
            .offset(x = 10.dp, y = (-80).dp)
            .zIndex(3f)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .background(color = Color(0xFF00664F))
            .zIndex(1F)
    )
}

@Composable
fun Mobile() {
    Column(
        modifier = Modifier
            .padding(bottom = 70.dp, start = 6.dp, end = 6.dp, top = 250.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(600.dp)
            .background(
                color = Color(0xFF00664F),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(top = 19.dp, bottom = 52.dp)
            .zIndex(3F)
    ){}
    Column(
        modifier = Modifier
            .padding(bottom = 0.dp, start = 0.dp, end = 0.dp, top = 240.dp)
            .clip(shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .height(700.dp)
            .background(
                color = Color(0xFFFFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(top = 10.dp, bottom = 0.dp)
            .zIndex(2F)
    ){}
}

@Composable
private fun Header(navController: NavController) {
    OutlinedButton(
        onClick = { navController.navigate("calendar") },
        border = BorderStroke(0.dp, Color.Transparent),
        modifier = Modifier
            .padding(bottom = 0.dp, start = 15.dp, end = 190.dp, top = 5.dp)
            .fillMaxWidth()
            .offset(x = (-10).dp, y = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .offset(y = 5.dp, x = 0.dp)
                .zIndex(4F)
        ) {
            Icon(
                painter = painterResource(R.drawable.icon1),
                contentDescription = "Todo",
                tint = Color.White,
                modifier = Modifier
                    .size(43.dp)
                    .offset(x = (-0).dp, y = 5.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp).padding(top = 5.dp).offset(y = 5.dp))
        Text("To-Do", color = Color.White, fontSize = 40.sp)
    }
}

@Composable
fun Footer(navController: NavController) {
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
                    .padding(end = 5.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.home_alt_2),
                    contentDescription = "I",
                )

            }
            IconButton(
                onClick = { navController.navigate("Room") },
                modifier = Modifier
                    .padding(start = 25.dp, end = 25.dp)
                    .width(40.dp)
                    .height(40.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.icons8_user_account_96),
                    contentDescription = "I",
                    modifier = Modifier
                        .padding(end = 5.dp, bottom = 10.dp)
                        .width(50.dp)
                        .height(40.dp)
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

