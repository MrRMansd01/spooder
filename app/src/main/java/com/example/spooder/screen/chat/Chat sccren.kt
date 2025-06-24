package com.example.spooder.screen.chat


import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spooder.R
import com.example.spooder.model.Message
import com.example.spooder.screen.accent_pass.AccentViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSccren(navController: NavController, channelId: String, channelName: String) {
    val viewModel: ChatViewModel = hiltViewModel()
    val accentViewModel: AccentViewModel = hiltViewModel()
    val userInfo by accentViewModel.userState.collectAsState()
    val context = LocalContext.current

    val uuidChannelId = remember(channelId) { UUID.fromString(channelId) }
    val channelImage = remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        ).also {
            cameraImageUri.value = it
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let {
                viewModel.sendImageMessage(
                    uri = it,
                    channelId = uuidChannelId,
                    senderId = userInfo?.id,
                    senderName = userInfo?.username ?: "ناشناс",
                    senderAvatarUrl = userInfo?.avatar_url
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(createImageUri())
        } else {
            showPermissionDialog = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.sendImageMessage(
                uri = it,
                channelId = uuidChannelId,
                senderId = userInfo?.id,
                senderName = userInfo?.username ?: "ناشناс",
                senderAvatarUrl = userInfo?.avatar_url
            )
        }
    }

    LaunchedEffect(key1 = uuidChannelId) {
        viewModel.fetchMessages(uuidChannelId)
        viewModel.registerUserToChannel(uuidChannelId)
        viewModel.getChannelImage(channelId) { imageUri ->
            channelImage.value = imageUri
        }
    }

    val messages by viewModel.messages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = channelImage.value,
                            contentDescription = "Channel Avatar",
                            fallback = painterResource(id = R.drawable._0),
                            error = painterResource(id = R.drawable._0),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = channelName, color = Color.Black, fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painter = painterResource(R.drawable.back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9))
        ) {
            ChatMessages(
                modifier = Modifier.weight(1f),
                messages = messages,
                currentUserId = userInfo?.id,
                onSendMessage = { messageText ->
                    viewModel.sendMessage(
                        channelId = uuidChannelId,
                        content = messageText,
                        senderId = userInfo?.id,
                        senderName = userInfo?.username ?: "ناشناس",
                        senderAvatarUrl = userInfo?.avatar_url
                    )
                },
                onImageClicked = { galleryLauncher.launch("image/*") }
            )
        }
    }
}

@Composable
fun ChatMessages(
    modifier: Modifier = Modifier,
    messages: List<Message>,
    currentUserId: String?,
    onSendMessage: (String) -> Unit,
    onImageClicked: () -> Unit
) {
    val hideKeyboardController = LocalSoftwareKeyboardController.current
    var msg by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message, currentUserId = currentUserId)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = msg,
                onValueChange = { msg = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (msg.isNotBlank()) {
                        onSendMessage(msg)
                        msg = ""
                        hideKeyboardController?.hide()
                    }
                }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onImageClicked) {
                Icon(painter = painterResource(R.drawable.clip__attachment), contentDescription = "Send image")
            }
            IconButton(onClick = {
                if (msg.isNotBlank()) {
                    onSendMessage(msg)
                    msg = ""
                    hideKeyboardController?.hide()
                }
            }) {
                Icon(painter = painterResource(R.drawable.send), contentDescription = "Send message")
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, currentUserId: String?) {
    val isCurrentUser = message.sender_id == currentUserId
    val bubbleColor = if (isCurrentUser) Color(0xFFE7FFDB) else Color.White
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start

    Box(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = horizontalArrangement
        ) {
            if (!isCurrentUser) {
                SenderAvatar(message)
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName ?: "ناشناس",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(color = bubbleColor, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .widthIn(max = 280.dp)
                ) {
                    if (!message.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = message.imageUrl,
                            contentDescription = "Sent image",
                            modifier = Modifier
                                .widthIn(max = 250.dp)
                                .heightIn(max = 300.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text(
                            text = message.content,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }
            }
            if (isCurrentUser) {
                Spacer(modifier = Modifier.width(8.dp))
                SenderAvatar(message)
            }
        }
    }
}

@Composable
fun SenderAvatar(message: Message) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (!message.senderAvatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = message.senderAvatarUrl,
                contentDescription = "Sender Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                fallback = painterResource(id = R.drawable._0),
                error = painterResource(id = R.drawable._0)
            )
        } else {
            Text(
                text = message.senderName?.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}