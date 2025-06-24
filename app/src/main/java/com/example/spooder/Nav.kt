package com.example.spooder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.spooder.screen.HomeScreen
import com.example.spooder.screen.Join
import com.example.spooder.screen.calendar.NavCalender
import com.example.spooder.screen.calendar.TimelineCalendarScreen
import com.example.spooder.screen.accent_pass.AboutApp
// این ایمپورت را اضافه کنید
import com.example.spooder.screen.accent_pass.Accent
import com.example.spooder.screen.accent_pass.CodiaMainView
import com.example.spooder.screen.accent_pass.FooterAccent
import com.example.spooder.screen.accent_pass.HelpSupport
import com.example.spooder.screen.accent_pass.SavedBeneficiary
import com.example.spooder.screen.room.FooterRoom
import com.example.spooder.screen.room.Room
import com.example.spooder.screen.room.TimeTableScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.spooder.screen.chat.ChatHome
import com.example.spooder.screen.chat.ChatSccren
import com.example.spooder.screen.login.JoinViewModel
import com.example.spooder.screen.login.Registration
// ایمپورت AccentViewModel
import com.example.spooder.screen.accent_pass.AccentViewModel
import com.example.spooder.screen.chat.NavChat

@Composable
fun Myappnav(){
    val navController = rememberNavController()
    var selectedTimeStart by remember { mutableStateOf("") }
    var selectedTimeEnd by remember { mutableStateOf("") }

    val viewModel: JoinViewModel = hiltViewModel()
    val startDestination = if (viewModel.isLoggedIn()) "Home" else "Join"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("Home"){
            HomeScreen(navController)
        }
        composable("Accent"){
            val accentViewModel: AccentViewModel = hiltViewModel()
            Accent(navController = navController, viewModel = accentViewModel)
            FooterAccent(navController)
        }
        composable("Join"){
            Join(navController)
        }
        composable("Registration"){
            Registration(navController)
        }
        composable("Chat"){
            ChatHome(navController)
            NavChat(navController)
        }
        composable("chat/{channelId}&{channelName}", arguments = listOf(
            navArgument("channelId") {
                type = NavType.StringType
            },
            navArgument("channelName") {
                type = NavType.StringType
            }
        )) {
            val channelId = it.arguments?.getString("channelId") ?: ""
            val channelName = it.arguments?.getString("channelName") ?: ""
            ChatSccren(navController, channelId,channelName)
        }
        composable("calendar"){
            TimelineCalendarScreen(
                selectedTimeStart = selectedTimeStart,
                selectedTimeEnd = selectedTimeEnd)
            NavCalender(navController)
        }
        composable("Room"){
            Room()
            TimeTableScreen(navController)
            FooterRoom(navController)
        }
        composable("Saved_Beneficiary"){
            SavedBeneficiary(modifier = Modifier)
        }
        composable("My_Account"){
            // برای My_Account هم باید از hiltViewModel استفاده شود
            val accentViewModel: AccentViewModel = hiltViewModel()
            CodiaMainView(navController, accentViewModel)
        }
        composable("Help_Support"){
            HelpSupport(navController)
        }
        composable("AboutApp"){
            AboutApp(navController)
        }
    }
}
