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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.spooder.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spooder.model.User

@RequiresApi(Build.VERSION_CODES.O)
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
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = Color(0xFFFFFFFF),
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color(0xFFF9F9F9),
                )
                .padding(top = 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Profile",
                color = Color(0xFF181D27),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 15.dp, start = 17.dp, top = 50.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 14.dp, start = 16.dp, end = 16.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF00664F),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .shadow(
                        elevation = 44.dp,
                        spotColor = Color(0x0D000000),
                    )
                    .padding(17.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(end = 94.dp)
                        .width(223.dp)
                        .padding(end = 13.dp)
                ) {
                    AsyncImage(
                        model = userInfo?.avatar_url,
                        contentDescription = "Profile",
                        fallback = painterResource(id = R.drawable._0),
                        error = painterResource(id = R.drawable._0),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .offset(x = (-10).dp)
                            .width(63.dp)
                            .height(62.dp)
                            .clip(CircleShape)
                            .zIndex(1F)
                    )
                Column(
                        modifier = Modifier
                            .width(134.dp)
                    ) {
                        Text(
                            text = userInfo?.username ?: userInfo?.email ?: "Loading...",
                            color = Color(0xFFFFFFFF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                        )
                        Text(
                            text = userInfo?.email ?: "...",
                            color = Color(0xFFD7D7D7),
                            fontSize = 11.sp,
                        )
                    }
                }
                IconButton(
                    onClick = { navController.navigate("My_Account") },
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .width(50.dp)
                        .height(50.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_outline_mode_edit_outline),
                        contentDescription = "Edit",
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(end = 10.dp, start = 10.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(vertical = 24.dp, horizontal = 17.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 25.dp)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group_1233456789),
                        contentDescription = "I",
                        modifier = Modifier
                            .padding(end = 17.dp)
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .weight(1f)
                    ) {
                        Text(
                            "My Account ",
                            color = Color(0xFF181D27),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        )
                        Text(
                            "Make changes to your account",
                            color = Color(0xFFABABAB),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate("My_Account") },
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .width(50.dp)
                            .height(50.dp)){
                    Image(
                        painter = painterResource(id = R.drawable.month_chevron),
                        contentDescription = "I",
                        modifier = Modifier
                            .width(15.dp)
                            .height(15.dp)
                    )}
//                }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 25.dp)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group_1233456789),
                        contentDescription = "I",
                        modifier = Modifier
                            .padding(end = 17.dp)
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .weight(1f)
                    ) {
                        Text(
                            "Gems and Point",
                            color = Color(0xFF181D27),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        )
                        Text(
                            "yor reward ",
                            color = Color(0xFFABABAB),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate("Saved_Beneficiary") },
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .width(50.dp)
                            .height(50.dp)){
                    Image(
                        painter = painterResource(id = R.drawable.month_chevron),
                        contentDescription = "I",
                        modifier = Modifier
                            .width(15.dp)
                            .height(15.dp)
                    )}
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 25.dp)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group_123345),
                        contentDescription = "I",
                        modifier = Modifier
                            .padding(end = 17.dp)
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .weight(1f)
                    ) {
                        Text(
                            "Notifications",
                            color = Color(0xFF181D27),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 11.dp)
                        )
                        Text(
                            "Manage your notifications",
                            color = Color(0xFFABABAB),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    val context = LocalContext.current
                    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
                    val scope = rememberCoroutineScope()

                    // Check notification status when this composable is first displayed
                    LaunchedEffect(Unit) {
                        viewModel.checkNotificationStatus(context)
                    }

                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                viewModel.toggleNotifications(context)
                            }
                            scope.launch {
                                delay(500)
                                viewModel.checkNotificationStatus(context)
                                viewModel.fetchUserProfile()
                            }
                        },
                        modifier = Modifier
                            .padding(top = 5.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00664F),
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.White
                        )
                    )

                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.group_12334567),
                        contentDescription = "I",
                        modifier = Modifier
                            .padding(end = 17.dp)
                            .width(50.dp)
                            .height(50.dp)
                    )
                    Column(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .weight(1f)
                    ) {
                        Text(
                            "خروج از حساب کاربری",
                            color = Color(0xFFEC0000),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        )
                        Text(
                            "برای خروج از حساب کاربری خود کلیک کنید",
                            color = Color(0xFFABABAB),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .width(50.dp)
                            .height(50.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.month_chevron),
                            contentDescription = "I",
                            modifier = Modifier
                                .width(15.dp)
                                .height(15.dp)
                        )
                    }
                }
            }
            Text(
                "More",
                color = Color(0xFF181D27),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 11.dp, top = 11.dp, end = 10.dp)
                    .offset(x = 10.dp)
            )
            Column(
                modifier = Modifier
                    .padding(end = 10.dp, start = 10.dp, bottom = 90.dp)
                    .clip(shape = RoundedCornerShape(5.dp))
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(5.dp)
                    )

                    .padding(vertical = 24.dp, horizontal = 17.dp)
            ){
                Column(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(5.dp))
                        .background(
                            color = Color(0xFFFFFFFF),
                            shape = RoundedCornerShape(5.dp)
                        )

                        .padding(0.dp)
                ){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 15.dp)
                            .fillMaxWidth()
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.group_123345678),
                            contentDescription = "I",
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .width(50.dp)
                                .height(50.dp)
                        )
                        Text(
                            "Help & Support",
                            color = Color(0xFF181D27),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ){
                        }
                        IconButton(
                            onClick = { navController.navigate("Help_Support") },
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .width(50.dp)
                                .height(50.dp)){
                        Image(
                            painter = painterResource(id = R.drawable.month_chevron),
                            contentDescription = "I",
                            modifier = Modifier
                                .width(15.dp)
                                .height(15.dp)
                        )}
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(50.dp)
                            .fillMaxWidth()
                    ){
                        Image(
                            painter = painterResource(id = R.drawable.group_12334),
                            contentDescription = "I",
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .width(50.dp)
                                .height(50.dp)
                        )
                        Text(
                            "About App",
                            color = Color(0xFF181D27),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ){
                        }
                        IconButton(
                            onClick = { navController.navigate("AboutApp") },
                            modifier = Modifier
                                .padding(bottom = 6.dp)
                                .width(50.dp)
                                .height(50.dp)){
                        Image(
                            painter = painterResource(id = R.drawable.month_chevron),
                            contentDescription = "I",
                            modifier = Modifier
                                .width(15.dp)
                                .height(15.dp)
                        )}
                    }
                }
            }
        }
    }
}

@Composable
fun FooterAccent(navController: NavController) {
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
                .padding(bottom = 10.dp)
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
                    .padding(start = 25.dp , end = 25.dp )
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
                    .padding(bottom = 6.dp)
                    .width(50.dp)
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.user_1),
                    contentDescription = "I",
                )
            }
        }
    }
}
