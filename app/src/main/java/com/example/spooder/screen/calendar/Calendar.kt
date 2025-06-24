package com.example.spooder.screen.calendar

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.*
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spooder.R
import com.example.spooder.model.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import com.example.spooder.SupabaseService

data class DayItem(
    val day: String,
    val date: String,
    val isActive: Boolean = false
)

@Suppress("DEPRECATION")
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineCalendarScreen(
    taskViewModel: TaskViewModel = viewModel(),
    supabaseService: SupabaseService = SupabaseService.getInstance(LocalContext.current),
    selectedTimeStart: String,
    selectedTimeEnd: String) {
    val tasks by taskViewModel.tasks.observeAsState(initial = emptyList())
    val isLoading by taskViewModel.isLoading.observeAsState(initial = false)
    val error by taskViewModel.error.observeAsState(initial = null)
    val userId = supabaseService.getCurrentUserId().toString()

    var currentStartDate by rememberSaveable { mutableStateOf(Calendar.getInstance()) }
    var selectedDayDate by rememberSaveable {
        mutableStateOf(SimpleDateFormat("dd", Locale.ENGLISH).format(Calendar.getInstance().time))
    }
    var localSelectedTimeStart by rememberSaveable { mutableStateOf(selectedTimeStart) }
    var localSelectedTimeEnd by rememberSaveable { mutableStateOf(selectedTimeEnd) }
    var selectedScore by rememberSaveable { mutableStateOf(1) }
    var taskDate by rememberSaveable { mutableStateOf("") }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    // Initialize taskDate to today's date if empty
    if (taskDate.isEmpty()) {
        taskDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("EN")).format(Calendar.getInstance().time)
    }

    // Fetch tasks only once when screen is first loaded
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val formattedDate = dateFormat.format(calendar.time)
        taskViewModel.fetchTasksByDateAndUser(formattedDate, userId)
    }


    fun generateWeekDays(startDate: Calendar): List<DayItem> {
        val days = mutableListOf<DayItem>()
        val calendar = startDate.clone() as Calendar
        
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        (0..6).forEach { i ->
            val dayFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
            val dayName = dayFormat.format(calendar.time)
            val dayNumber = SimpleDateFormat("dd", Locale.ENGLISH).format(calendar.time)
            val today = Calendar.getInstance()
            val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            days.add(
                DayItem(
                    day = dayName,
                    date = dayNumber,
                    isActive = isToday
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    Column(
        modifier = Modifier
            .background(color = Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp)
                .background(color = Color(0xFFFFFFFF))
        ) {
            Text(
                text = "Today",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(end = 70.dp, start = 20.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd MMMM yyyy", Locale("EN")).format(Calendar.getInstance().time),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = 70.dp, start = 20.dp)
                )
                
                // Calendar Icon Button
                IconButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon),
                        contentDescription = "Select Date",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // DatePicker Dialog
            if (showDatePicker) {
                val datePickerDialog = DatePickerDialog(
                    LocalContext.current,
                    { _, year, month, dayOfMonth ->
                        val calendar = Calendar.getInstance()
                        calendar.set(year, month, dayOfMonth)
                        currentStartDate = calendar
                        selectedDayDate = SimpleDateFormat("dd", Locale.ENGLISH).format(calendar.time)
                        taskDate = SimpleDateFormat("dd MMMM yyyy", Locale("EN")).format(calendar.time)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        val formattedDate = dateFormat.format(calendar.time)
                        taskViewModel.fetchTasksByDateAndUser(formattedDate, userId)
                        showDatePicker = false
                    },
                    currentStartDate.get(Calendar.YEAR),
                    currentStartDate.get(Calendar.MONTH),
                    currentStartDate.get(Calendar.DAY_OF_MONTH)
                )
                LaunchedEffect(Unit) {
                    datePickerDialog.show()
                }
            }

            Scaffold(
                modifier = Modifier
                    .background(color = Color(0xFFFFFFFF))
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Color(0xFFFFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .offset(y = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp)
                                .padding(bottom = 8.dp)
                                .offset(y = (-10).dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            generateWeekDays(currentStartDate).forEach { day ->
                                DayItem(
                                    day = day,
                                    isSelected = day.date == selectedDayDate,
                                    onDaySelected = {
                                        selectedDayDate = day.date
                                        val selectedCal = currentStartDate.clone() as Calendar
                                        
                                        selectedCal.set(Calendar.DAY_OF_MONTH, day.date.toInt())
                                        
                                        taskDate = SimpleDateFormat("dd MMMM yyyy", Locale("EN")).format(selectedCal.time)
                                        
                                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                                        val formattedDate = dateFormat.format(selectedCal.time)
                                        
                                        taskViewModel.fetchTasksByDateAndUser(formattedDate, userId)
                                    }
                                )
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 4.dp, top = 0.dp)
                            .clip(shape = RoundedCornerShape(16.dp))
                            .background(color = Color(0xFF00664F), shape = RoundedCornerShape(16.dp))
                            .height(570.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        when {
                            isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(48.dp)
                                    )
                        }
                            }
                            error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                                    Text(
                                        "Error: $error",
                                        color = Color.Red,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            tasks.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No tasks for this day",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = tasks,
                                        key = { task -> task.id }
                                    ) { task ->
                                        TaskCardItem(
                                            task = task,
                                            onToggleCompletion = { taskId, is_completed ->
                                                taskViewModel.toggleTaskCompletion(taskId, is_completed)
                                            },
                                            onDeleteTask = { taskId ->
                                                taskViewModel.deleteTask(Task(
                                                        id = taskId,
                                                        title = "",
                                                        date = selectedDayDate,
                                                        time_start = "",
                                                        time_end = "",
                                                        color = "",
                                                        is_completed = false
                                                ))
                                            }
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

    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by rememberSaveable {
        mutableStateOf(false)
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen = false }
        ) {
            var title by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(630.dp)
                    .background(Color(0xFFFFFFFF))
            ) {
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(50.dp))
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(horizontal = 29.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 30.dp)
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Text(
                            "New Task",
                            color = Color(0xFF000000),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 28.dp, start = 17.dp)
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .padding(vertical = 0.dp)
                        ) {
                            Text(
                                "Title",
                                color = Color(0xFF626060),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 0.dp)
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = {
                                    title = it
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                ),
                                textStyle = TextStyle(
                                    fontSize = 25.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .padding(bottom = 0.dp, start = 10.dp)
                                    .offset(y = 0.dp, x = (-25).dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .padding(bottom = 0.dp, start = 13.dp)
                                .offset(y = -(18).dp)
                                .width(309.dp)
                                .height(1.dp)
                                .background(
                                    color = Color(0xFF000000),
                                )
                        ) {
                        }
                        NewTaskDate(
                            onDateSelected = { date ->
                                taskDate = date
                            }
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 0.dp)
                                .padding(vertical = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(bottom = 15.dp, start = 12.dp)
                                    .width(309.dp)
                                    .height(1.dp)
                                    .background(
                                        color = Color(0xFF000000),
                                    )
                            ) {}
                        }
                        NewTaskTime(
                            onTimeStartSelected = { time -> localSelectedTimeStart = time },
                            onTimeEndSelected = { time -> localSelectedTimeEnd = time }
                        )
                        Row(
                            modifier = Modifier
                                .offset(y = (-20).dp)
                                .padding(bottom = 20.dp, start = 13.dp, end = 30.dp)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(end = 29.dp)
                                    .width(144.dp)
                                    .height(1.dp)
                                    .background(
                                        color = Color(0xFF000000),
                                    )
                            ) {
                            }
                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                                    .height(1.dp)
                                    .background(
                                        color = Color(0xFF000000),
                                    )
                            ) {
                            }
                        }
                        NewTaskCategory(
                            selectedScore = selectedScore,
                            onScoreSelected = { score -> selectedScore = score }
                        )
                        OutlinedButton(
                            onClick = {
                                if (title.isNotEmpty() && taskDate.isNotEmpty()) {
                                    val timeStart = localSelectedTimeStart.ifEmpty { "00:00" }
                                    val timeEnd = localSelectedTimeEnd.ifEmpty { "00:00" }

                                    // Parse the selected date from taskDate
                                    val displayDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("EN"))
                                    val supabaseDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                                    
                                    val parsedDate = displayDateFormat.parse(taskDate)
                                    parsedDate?.let {
                                        val taskCal = Calendar.getInstance()
                                        taskCal.time = it
                                        val formattedDate = supabaseDateFormat.format(taskCal.time)

                                        val userId = supabaseService.getCurrentUserId().toString()
                                        
                                        val newTask = Task(
                                            id = UUID.randomUUID().toString(),
                                            title = title,
                                            date = formattedDate, // Using the formatted date from selected day
                                            time_start = timeStart,
                                            time_end = timeEnd,
                                            color = selectedScore.toString(),
                                            is_completed = false,
                                            user_id = userId
                                        )

                                        taskViewModel.addTask(newTask)
                                        // Update the selected day to show the new task
                                        selectedDayDate = SimpleDateFormat("dd", Locale.ENGLISH).format(taskCal.time)
                                        // Refresh tasks for the selected date
                                        taskViewModel.fetchTasksByDateAndUser(formattedDate, userId)
                                    }
                                }
                                isSheetOpen = false
                            },
                            border = BorderStroke(0.dp, Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                .padding(horizontal = 40.dp)
                                .clip(shape = RoundedCornerShape(20.dp))
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF00664F),
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 23.dp)
                            ) {
                                Text(
                                    "Create New Task",
                                    color = Color(0xFFFFFFFF),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    IconButton(
        onClick = {
            isSheetOpen = true
        },
        modifier = Modifier
            .offset(x = 330.dp, y = 680.dp)
            .width(60.dp)
            .height(60.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.group_5), contentDescription = "Add Task",
        )
    }
}

@Composable
private fun DayItem(
    day: DayItem,
    isSelected: Boolean,
    onDaySelected: () -> Unit
) {
    OutlinedButton(
        onClick = onDaySelected,
        border = BorderStroke(0.dp, Color.Transparent),
        modifier = Modifier
            .width(48.dp)
            .height(80.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(
                    color = if (isSelected) Color(0xFF1B5E20) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = day.day,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 14.sp
            )
            Text(
                text = day.date,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}



// Composable for individual Task Card
@SuppressLint("AutoboxingStateCreation")
@Composable
fun TaskCardItem(
    task: Task,
    onToggleCompletion: (String, Boolean) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dismissThreshold = 100f

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(300)
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        onDeleteTask(task.id)
                        showDeleteDialog = false
                    },
                    border = BorderStroke(1.dp, Color(0xFFEC0000)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEC0000))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    border = BorderStroke(1.dp, Color(0xFF2BBA90)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2BBA90))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Background
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2BBA90))
        ) {
            // Left side (Complete)
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

            // Right side (Delete)
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

        // Main card
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
                                    onToggleCompletion(task.id, !task.is_completed)
                                        offsetX = 0f
                                }
                                else -> {
                                        offsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
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
                    .padding(30.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    style = if (task.is_completed) TextStyle(textDecoration = TextDecoration.LineThrough) else TextStyle.Default,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Day of week box
                        Box(
                            modifier = Modifier
                                .height(32.dp)
                                .width(56.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(when (task.color.toIntOrNull() ?: 1) {
                                    1 -> Color(0xFF2BBA90)
                                    2 -> Color(0xFFECB800)
                                    3 -> Color(0xFFEC0000)
                                    else -> Color(0xFF2BBA90)
                                }),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = getDayOfWeek(task.date),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        // Progress indicators
                        ProgressIndicators(task)
                    }

                    // Time
                    Text(
                        text = task.time_start ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A3BF7)
                    )
                }
            }
        }
    }
}

// Helper function to get day of week
fun getDayOfWeek(dateString: String): String {
    return try {
        val dateFormat = SimpleDateFormat("dd", Locale.ENGLISH)
        val date = dateFormat.parse(dateString)
        val calendar = Calendar.getInstance()
        calendar.time = date ?: Calendar.getInstance().time
        SimpleDateFormat("EEE", Locale.ENGLISH).format(calendar.time)
    } catch (_: Exception) {
        "Mon"
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

// Progress indicators for task card
@Composable
fun ProgressIndicators(task: Task) {
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
fun DatePickerField(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("EN"))
            val formattedDate = dateFormat.format(calendar.time)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { datePickerDialog.show() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color(0xFFF3E5F5).copy(alpha = 0f)
        ),
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = selectedDate.ifEmpty { "pick up a date" },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimePickerFieldStart(
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("h:mm a", Locale("EN"))
            val formattedTime = timeFormat.format(calendar.time)
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    OutlinedCard(
        modifier = Modifier
            .width(120.dp)
            .padding(vertical = 8.dp)
            .clickable { timePickerDialog.show() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Start time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedTime.ifEmpty { "1:00 PM" },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimePickerFieldEnd(
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("h:mm a", Locale("EN"))
            val formattedTime = timeFormat.format(calendar.time)
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    OutlinedCard(
        modifier = Modifier
            .width(120.dp)
            .offset(y = 0.dp)
            .padding(vertical = 8.dp)
            .clickable { timePickerDialog.show() },
        colors = CardDefaults.outlinedCardColors(
            containerColor = Color.White
        ),
        border = BorderStroke(0.dp, Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "End time",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedTime.ifEmpty { "2:00 PM" },
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun NewTaskDate(
    onDateSelected: (String) -> Unit
) {
    var selectedDate by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .offset(y = 20.dp)
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        DatePickerField(
            selectedDate = selectedDate,
            onDateSelected = { date ->
                selectedDate = date
                onDateSelected(date)
            }
        )
    }
}

@Composable
fun NewTaskTime(
    onTimeStartSelected: (String) -> Unit,
    onTimeEndSelected: (String) -> Unit
) {
    var selectedTimeEnd by remember { mutableStateOf("") }
    var selectedTimeStart by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .offset(y = 5.dp)
            .fillMaxWidth()
            .padding(0.dp)
    ) {
        TimePickerFieldStart(
            selectedTime = selectedTimeStart,
            onTimeSelected = { time ->
                selectedTimeStart = time
                onTimeStartSelected(time)
            }
        )
        Spacer(modifier = Modifier.width(50.dp))
        TimePickerFieldEnd(
            selectedTime = selectedTimeEnd,
            onTimeSelected = { time ->
                selectedTimeEnd = time
                onTimeEndSelected(time)
            }
        )
    }
}

@Composable
fun NewTaskCategory(
    selectedScore: Int,
    onScoreSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(bottom = 27.dp)
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            "Category",
            color = Color(0xFF626060),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp, start = 25.dp)
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CategoryButton(
                score = 1,
                color = Color(0xFF2BBA90),
                isSelected = selectedScore == 1,
                onClick = { onScoreSelected(1) }
            )
            CategoryButton(
                score = 2,
                color = Color(0xFFECB800),
                isSelected = selectedScore == 2,
                onClick = { onScoreSelected(2) }
            )
            CategoryButton(
                score = 3,
                color = Color(0xFFEC0000),
                isSelected = selectedScore == 3,
                onClick = { onScoreSelected(3) }
            )
        }
    }
}

@Composable
private fun CategoryButton(
    score: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(0.dp, Color.Transparent),
        contentPadding = PaddingValues(),
        modifier = Modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else Color(0xFFDEDEDE),
                shape = RoundedCornerShape(15.dp)
            )
            .clip(shape = RoundedCornerShape(15.dp))
            .width(105.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                "Score $score",
                color = Color(0xFF626060),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun NavCalender(navController: NavController) {
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
                    painter = painterResource(id = R.drawable.home_alt_4),
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
                    painter = painterResource(id = R.drawable.group_23),
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

