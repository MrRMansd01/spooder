package com.example.spooder.screen.accent_pass

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spooder.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Accent(navController: NavController,
           viewModel: AccentViewModel = hiltViewModel()) {
    val userInfo by viewModel.userState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("خروج از حساب کاربری") },
            text = { Text("آیا مطمئن هستید که می‌خواهید از حساب کاربری خود خارج شوید؟") },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.logout()
                        showLogoutDialog = false
                        navController.navigate("Join"){
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    border = BorderStroke(1.dp, Color(0xFFEC0000)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEC0000))
                ) {
                    Text("خروج")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    border = BorderStroke(1.dp, Color(0xFF2BBA90)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2BBA90))
                ) {
                    Text("انصراف")
                }
            }
        )
    }

    Scaffold(
        bottomBar = { FooterAccent(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = Color(0xFFF9F9F9))
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Profile",
                color = Color(0xFF181D27),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
            )

            // Profile Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color(0xFF00664F))
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = userInfo?.avatar_url,
                    contentDescription = "Profile",
                    fallback = painterResource(id = R.drawable._0),
                    error = painterResource(id = R.drawable._0),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userInfo?.username ?: userInfo?.email ?: "Loading...",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = userInfo?.email ?: "...",
                        color = Color(0xFFD7D7D7),
                        fontSize = 12.sp
                    )
                }
                IconButton(onClick = { navController.navigate("My_Account") }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_outline_mode_edit_outline),
                        contentDescription = "Edit"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options Section
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .padding(vertical = 8.dp)
            ) {
                ProfileOptionItem(icon = R.drawable.group_1233456789, title = "My Account", subtitle = "Make changes to your account") {
                    navController.navigate("My_Account")
                }
                ProfileOptionItem(icon = R.drawable.group_123345, title = "Gems and Point", subtitle = "Your rewards") {
                    navController.navigate("Saved_Beneficiary")
                }
                NotificationOptionItem(viewModel = viewModel)
                ProfileOptionItem(icon = R.drawable.group_12334567, title = "خروج از حساب کاربری", subtitle = "برای خروج از حساب کاربری خود کلیک کنید", isLogout = true) {
                    showLogoutDialog = true
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "More",
                color = Color(0xFF181D27),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Column(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(16.dp))
                    .background(color = Color.White)
                    .padding(vertical = 8.dp)
            ) {
                ProfileOptionItem(icon = R.drawable.group_123345678, title = "Help & Support", subtitle = "") {
                    navController.navigate("Help_Support")
                }
                ProfileOptionItem(icon = R.drawable.group_12334, title = "About App", subtitle = "") {
                    navController.navigate("AboutApp")
                }
            }
        }
    }
}

@Composable
fun ProfileOptionItem(icon: Int, title: String, subtitle: String, isLogout: Boolean = false, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (isLogout) Color(0xFFEC0000) else Color(0xFF181D27),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    color = Color(0xFFABABAB),
                    fontSize = 12.sp,
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.month_chevron),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NotificationOptionItem(viewModel: AccentViewModel) {
    val context = LocalContext.current
    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.checkNotificationStatus(context)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.group_123345),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Notifications",
                color = Color(0xFF181D27),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Manage your notifications",
                color = Color(0xFFABABAB),
                fontSize = 12.sp,
            )
        }
        Switch(
            checked = notificationEnabled,
            onCheckedChange = {
                viewModel.toggleNotifications(context)
                scope.launch {
                    delay(500)
                    viewModel.checkNotificationStatus(context)
                }
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00664F),
                checkedTrackColor = Color(0xFF4CAF50),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color.White
            )
        )
    }
}

@Composable
fun FooterAccent(navController: NavController) {
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
            selected = true,
            onClick = { navController.navigate("Accent") },
            icon = { Image(painter = painterResource(id = R.drawable.user_1), contentDescription = "Profile") }
        )
    }
}
