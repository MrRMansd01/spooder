package com.example.spooder.screen.accent_pass

import android.os.Build
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Trophy(
    val name: String,
    val score: Int,
    val dateTime: LocalDateTime,
    val category: TrophyCategory = TrophyCategory.GENERAL
)

enum class TrophyCategory(val displayName: String, val icon: ImageVector, val color: Color) {
    GENERAL("General", Icons.Default.Star, Color(0xFFFFD700)),
    ACHIEVEMENT("Achievement", Icons.Default.Favorite, Color(0xFFFF6B35)),
    MILESTONE("Milestone", Icons.Default.Notifications, Color(0xFF4ECDC4)),
    CHALLENGE("Challenge", Icons.Default.PlayArrow, Color(0xFFE74C3C))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedBeneficiary(modifier: Modifier = Modifier) {
    var trophies by remember { mutableStateOf(listOf<Trophy>()) }
    var showDialog by remember { mutableStateOf(false) }
    var currentScore by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf("") }
    var currentName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TrophyCategory.GENERAL) }
    var expandedCategory by remember { mutableStateOf(false) }

    // Calculate stats
    val totalTrophies = trophies.size
    val totalScore = trophies.sumOf { it.score }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8F9FA),
                        Color(0xFFE9ECEF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with gradient background
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF00664F),
                                    Color(0xFF00896C)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Trophy Collection",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.Default.Favorite,
                                value = totalTrophies.toString(),
                                label = "Trophies"
                            )
                            StatItem(
                                icon = Icons.Default.Star,
                                value = totalScore.toString(),
                                label = "Total Score"
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00664F)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add New Trophy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Empty state or Trophy List
            if (trophies.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trophies.sortedByDescending { it.score }) { trophy ->
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            EnhancedTrophyCard(trophy = trophy)
                        }
                    }
                }
            }
        }

        // Enhanced Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF00664F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add New Trophy",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = currentName,
                            onValueChange = { currentName = it },
                            label = { Text("Trophy Name") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountBox, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = currentScore,
                            onValueChange = { currentScore = it },
                            label = { Text("Score") },
                            leadingIcon = {
                                Icon(Icons.Default.Star, contentDescription = null)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        // Category Selector
                        ExposedDropdownMenuBox(
                            expanded = expandedCategory,
                            onExpandedChange = { expandedCategory = !expandedCategory }
                        ) {
                            OutlinedTextField(
                                value = selectedCategory.displayName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                leadingIcon = {
                                    Icon(
                                        selectedCategory.icon,
                                        contentDescription = null,
                                        tint = selectedCategory.color
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expandedCategory
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = expandedCategory,
                                onDismissRequest = { expandedCategory = false }
                            ) {
                                TrophyCategory.values().forEach { category ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    category.icon,
                                                    contentDescription = null,
                                                    tint = category.color,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(category.displayName)
                                            }
                                        },
                                        onClick = {
                                            selectedCategory = category
                                            expandedCategory = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val score = currentScore.toIntOrNull() ?: 0
                            val time = currentTime.toIntOrNull() ?: 0
                            if (score > 0 && time > 0 && currentName.isNotBlank()) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    trophies = trophies + Trophy(
                                        name = currentName,
                                        score = score,
                                        dateTime = LocalDateTime.now(),
                                        category = selectedCategory
                                    )
                                }
                                currentScore = ""
                                currentName = ""
                                selectedCategory = TrophyCategory.GENERAL
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00664F)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Trophy")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFE0E0E0)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Trophies Yet",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF757575)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add your first trophy to get started!",
            fontSize = 14.sp,
            color = Color(0xFFABABAB),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EnhancedTrophyCard(trophy: Trophy) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trophy Icon with category color
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        trophy.category.color.copy(alpha = 0.1f),
                        CircleShape
                    )
                    .border(
                        2.dp,
                        trophy.category.color.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = trophy.category.icon,
                    contentDescription = null,
                    tint = trophy.category.color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Trophy Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trophy.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = trophy.category.displayName,
                    fontSize = 12.sp,
                    color = trophy.category.color,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoChip(
                        icon = Icons.Default.Star,
                        text = "${trophy.score}",
                        color = Color(0xFF00664F)
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trophy.dateTime.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy • HH:mm")
                        ),
                        fontSize = 11.sp,
                        color = Color(0xFFABABAB)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}