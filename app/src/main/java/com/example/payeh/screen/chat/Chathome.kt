package com.example.payeh.screen.chat

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.payeh.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHome(navController: NavController) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val channels by viewModel.channels.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var addChannel by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredChannels by remember(searchQuery, channels) {
        derivedStateOf {
            if (searchQuery.isEmpty()) {
                channels
            } else {
                channels.filter { channel ->
                    channel.name.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { addChannel = true },
                shape = CircleShape,
                containerColor = Color(0xFF00FF9F),
                modifier = Modifier
                    .offset(y = -60.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.group_5),
                    contentDescription = "Add Chat",
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                "Chatroom",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search chats...") },
                leadingIcon = { Icon(painterResource(id = R.drawable.search_1__1), contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00664F),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF00664F)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (filteredChannels.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No chats found" else "No chats available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(filteredChannels, key = { it.id!! }) { channel ->
                            ChannelItem(
                                channelName = channel.name,
                                channelId = channel.id!!,
                                imageUrl = channel.imageUrl,
                                onClick = {
                                    navController.navigate("chat/${channel.id}&${channel.name}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (addChannel) {
        ModalBottomSheet(
            onDismissRequest = { addChannel = false },
            sheetState = sheetState
        ) {
            AddChannelDialog(
                viewModel = viewModel,
                onDismiss = { addChannel = false },
                onAddChannel = { name, imageUri, userIds ->
                    viewModel.addChannel(name, imageUri, userIds)
                }
            )
        }
    }
}

@Composable
fun ChannelItem(
    channelName: String,
    channelId: String,
    imageUrl: String?,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Channel Avatar",
                fallback = painterResource(id = R.drawable._0),
                error = painterResource(id = R.drawable._0),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = channelName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun AddChannelDialog(
    viewModel: HomeViewModel,
    onDismiss: () -> Unit,
    onAddChannel: (String, Uri?, List<String>) -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var channelName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val selectedUsers = remember { mutableStateOf(setOf<String>()) }

    // Fetch users when dialog opens
    LaunchedEffect(Unit) {
        viewModel.fetchAllUsers()
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Create Private Chat",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = channelName,
            onValueChange = { channelName = it },
            label = { Text("Chat Name (e.g., Project X)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isLoading
            ) {
                Text(if (imageUri == null) "Select Image" else "Change Image")
            }
            Spacer(modifier = Modifier.width(16.dp))
            AsyncImage(
                model = imageUri,
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                fallback = painterResource(id = R.drawable._0),
                error = painterResource(id = R.drawable._0)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Select Members",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF00664F))
            }
        } else if (allUsers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No users found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(allUsers, key = { it.id.toString() }) { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val currentSelection = selectedUsers.value.toMutableSet()
                                val userId = user.id.toString()
                                if (userId in currentSelection) {
                                    currentSelection.remove(userId)
                                } else {
                                    currentSelection.add(userId)
                                }
                                selectedUsers.value = currentSelection
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = user.id.toString() in selectedUsers.value,
                            onCheckedChange = null // Click handled by Row
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(user.username ?: "Unknown User")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                onAddChannel(channelName, imageUri, selectedUsers.value.toList())
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && channelName.isNotBlank() && selectedUsers.value.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text("Create Chat")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun NavChat(navController: NavController) {
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
                    painter = painterResource(id = R.drawable.comment_1),
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