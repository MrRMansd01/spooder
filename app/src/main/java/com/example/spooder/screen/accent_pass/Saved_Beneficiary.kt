package com.example.spooder.screen.accent_pass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Trophy(
    val name: String,
    val score: Int,
    val timeSpent: Int, // در دقیقه
    val dateTime: LocalDateTime
)

@Composable
fun SavedBeneficiary(modifier: Modifier = Modifier) {
    var trophies by remember { mutableStateOf(listOf<Trophy>()) }
    var showDialog by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf("") }
    var currentName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Trophy List",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                "Your Achievements",
                color = Color(0xFF181D27),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 10.dp)
            )
            Text(
                "Track your progress and rewards",
                color = Color(0xFFABABAB),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }

        // Add Trophy Button
        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00664F))
        ) {
            Text("Add New Trophy")
        }

        // Trophy List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trophies.sortedByDescending { it.score }) { trophy ->
                TrophyCard(trophy = trophy)
            }
        }
    }

    // Add Trophy Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Trophy") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentName,
                        onValueChange = { currentName = it },
                        label = { Text("Trophy Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = currentScore,
                        onValueChange = { currentScore = it },
                        label = { Text("Score") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    OutlinedTextField(
                        value = currentTime,
                        onValueChange = { currentTime = it },
                        label = { Text("Time (minutes)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val score = currentScore.toIntOrNull() ?: 0
                        val time = currentTime.toIntOrNull() ?: 0
                        if (score > 0 && time > 0 && currentName.isNotBlank()) {
                            trophies = trophies + Trophy(
                                name = currentName,
                                score = score,
                                timeSpent = time,
                                dateTime = LocalDateTime.now()
                            )
                            currentScore = ""
                            currentTime = ""
                            currentName = ""
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00664F))
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TrophyCard(trophy: Trophy) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = trophy.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Score: ${trophy.score}",
                    color = Color(0xFF00664F)
                )
                Text(
                    text = "Time: ${trophy.timeSpent} min",
                    color = Color.Gray
                )
            }
            Text(
                text = "Date: ${trophy.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
