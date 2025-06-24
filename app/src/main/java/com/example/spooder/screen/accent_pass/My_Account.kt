package com.example.spooder.screen.accent_pass

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spooder.R
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CodiaMainView(
    navController: NavController,
    viewModel: AccentViewModel = viewModel()
) {
   val context = LocalContext.current
   val scope = rememberCoroutineScope()
   val userInfo by viewModel.userState.collectAsState()

   // State variables for text fields
   var nameValue by remember { mutableStateOf(userInfo?.username ?: "") }
   var emailValue by remember { mutableStateOf(userInfo?.email ?: "") }

   // Image picker launcher
   val imagePickerLauncher = rememberLauncherForActivityResult(
       contract = ActivityResultContracts.GetContent()
   ) { uri: Uri? ->
       uri?.let {
           scope.launch {
               if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                   viewModel.uploadProfileImage(it)
               }
           }
       }
   }

   // Update fields when user info changes
   LaunchedEffect(userInfo) {
       userInfo?.let {
           nameValue = it.username ?: ""
           emailValue = it.email
       }
   }

   Box(
       contentAlignment = Alignment.TopStart,
       modifier = Modifier
           .background(Color(0xff00664f))
           .size(412.dp, 852.dp)
           .clipToBounds(),
   ) {
       Image(
           painter = painterResource(id = R.drawable.popover_body),
           contentDescription = null,
           contentScale = ContentScale.Fit,
           modifier = Modifier
               .align(Alignment.TopStart)
               .offset(x = 0.dp, y = 179.424.dp)
               .size(412.dp, 672.576.dp),
       )
       Text(
           modifier = Modifier
               .align(Alignment.TopCenter)
               .wrapContentSize()
               .offset(x = 1.dp, y = 249.dp),
           text = nameValue,
           color = Color(0xff3629b6),
           fontSize = 16.sp,
           fontWeight = FontWeight.Normal,
           textAlign = TextAlign.Left,
           overflow = TextOverflow.Ellipsis,
       )

       // Name TextField
       Box(
           contentAlignment = Alignment.TopStart,
           modifier = Modifier
               .align(Alignment.TopStart)
               .offset(x = 40.dp, y = 321.dp)
               .size(328.dp, 92.dp),
       ) {
           Text(
               text = "name",
               color = Color(0xff979797),
               fontSize = 12.sp,
               modifier = Modifier
                   .offset(y = (-20).dp)
           )
           TextField(
               value = nameValue,
               onValueChange = { nameValue = it },
               modifier = Modifier
                   .size(328.dp, 44.dp)
                   .border(1.dp, Color(0xffcacaca), RoundedCornerShape(15.dp)),
               textStyle = TextStyle(
                   color = Color(0xff333333),
                   fontSize = 14.sp,
                   fontWeight = FontWeight.Normal
               ),
               colors = TextFieldDefaults.colors(
                   unfocusedContainerColor = Color.Transparent,
                   focusedContainerColor = Color.Transparent,
                   unfocusedIndicatorColor = Color.Transparent,
                   focusedIndicatorColor = Color.Transparent
               )
           )
       }


       Box(
           contentAlignment = Alignment.TopStart,
           modifier = Modifier
               .align(Alignment.TopStart)
               .offset(x = 40.dp, y = 400.dp)
               .size(328.dp, 92.dp),
       ) {
           Text(
               text = "email",
               color = Color(0xff979797),
               fontSize = 12.sp,
               modifier = Modifier
                   .offset(y = (-20).dp)
           )
           TextField(
               value = emailValue,
               onValueChange = { emailValue = it },
               modifier = Modifier
                   .size(328.dp, 44.dp)
                   .border(1.dp, Color(0xffcacaca), RoundedCornerShape(15.dp)),
               textStyle = TextStyle(
                   color = Color(0xff333333),
                   fontSize = 14.sp,
                   fontWeight = FontWeight.Normal
               ),
               colors = TextFieldDefaults.colors(
                   unfocusedContainerColor = Color.Transparent,
                   focusedContainerColor = Color.Transparent,
                   unfocusedIndicatorColor = Color.Transparent,
                   focusedIndicatorColor = Color.Transparent
               )
           )
       }


       // Back Button Box
       Box(
           contentAlignment = Alignment.TopStart,
           modifier = Modifier
               .align(Alignment.TopStart)
               .offset(x = 0.dp, y = 41.97.dp)
               .size(412.dp, 55.611.dp)
               .clickable {
                   navController.navigateUp()
               },
       ) {
           Box(
               modifier = Modifier
                   .align(Alignment.TopStart)
                   .offset(x = 0.dp, y = 25.dp)
                   .size(375.dp, 28.dp),
           )
           Text(
               modifier = Modifier
                   .align(Alignment.Center)
                   .wrapContentSize()
                   .offset(x = (-112.5).dp, y = 12.194500000000001.dp),
               text = "Edit",
               color = Color(0xffffffff),
               fontSize = 20.sp,
               fontWeight = FontWeight.Normal,
               textAlign = TextAlign.Left,
               overflow = TextOverflow.Ellipsis,
           )
           Image(
               painter = painterResource(id = R.drawable.path),
               contentDescription = null,
               contentScale = ContentScale.Fit,
               modifier = Modifier
                   .align(Alignment.TopStart)
                   .offset(x = 10.dp, y = 32.527.dp)
                   .size(16.dp, 16.788.dp)
                   .clickable {
                       navController.navigateUp()
                   },
           )
       }


       Box(
           modifier = Modifier
               .align(Alignment.TopStart)
               .offset(x = 40.dp, y = 597.dp)
               .size(328.dp, 92.dp),
       )
       AsyncImage(
           model = userInfo?.avatar_url,
           contentDescription = "Profile Picture",
           fallback = painterResource(id = R.drawable._0),
           error = painterResource(id = R.drawable._0),
           contentScale = ContentScale.Crop,
           modifier = Modifier
               .align(Alignment.TopCenter)
               .offset(x = 0.dp, y = 117.dp)
               .size(120.dp, 120.dp)
               .clip(CircleShape),
       )

       // Camera button for image selection
       Box(
           contentAlignment = Alignment.Center,
           modifier = Modifier
               .align(Alignment.TopCenter)
               .offset(x = 35.dp, y = 207.dp)  // Positioned at bottom-right of profile picture
               .size(32.dp)
               .background(Color(0xFF00664F), CircleShape)
               .clickable {
                   imagePickerLauncher.launch("image/*")
               },
       ) {
           Image(
               painter = painterResource(id = R.drawable.camera),
               contentDescription = "Change Profile Picture",
               modifier = Modifier
                   .size(18.dp)
           )
       }

       // Confirm Button with updateUserProfile
       Box(
           contentAlignment = Alignment.TopStart,
           modifier = Modifier
               .align(Alignment.TopStart)
               .offset(x = 56.dp, y = 500.dp)
               .size(295.dp, 44.dp)
               // در فایل My_Account.kt

               .clickable {
                   scope.launch {
                       // ۱. آپدیت را انجام بده
                       val success = viewModel.updateUserProfile(
                           name = nameValue,
                           username = nameValue
                       )
                       // ۲. فقط در صورت موفقیت، از صفحه خارج شو
                       if (success) {
                           // این فراخوانی باعث می‌شود دفعه بعد که صفحه باز می‌شود، اطلاعات جدید لود شوند
                           viewModel.fetchUserProfile()
                           navController.navigateUp()
                       }
                   }
               },
       ) {
           Box(
               modifier = Modifier
                   .align(Alignment.TopStart)
                   .background(Color(0xff00664f), RoundedCornerShape(15.dp))
                   .size(295.dp, 44.dp),
           )
           Text(
               modifier = Modifier
                   .align(Alignment.TopStart)
                   .wrapContentSize()
                   .offset(x = 113.116.dp, y = 10.dp),
               text = "Confirm",
               color = Color(0xffffffff),
               fontSize = 16.sp,
               fontWeight = FontWeight.Normal,
               textAlign = TextAlign.Center,
               overflow = TextOverflow.Ellipsis,
           )
       }
   }
}