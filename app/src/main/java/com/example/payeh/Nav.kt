package com.example.payeh // <<< نام پکیج به payeh تغییر کرد

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.payeh.screen.room.Room
import com.example.payeh.screen.HomeScreen
import com.example.payeh.screen.Join
import com.example.payeh.screen.accent_pass.AboutApp
import com.example.payeh.screen.accent_pass.Accent
import com.example.payeh.screen.accent_pass.CodiaMainView
import com.example.payeh.screen.accent_pass.HelpSupport
import com.example.payeh.screen.accent_pass.SavedBeneficiary
import com.example.payeh.screen.calendar.TimelineCalendarScreen
import com.example.payeh.screen.chat.ChatHome
import com.example.payeh.screen.chat.ChatSccren
import com.example.payeh.screen.login.JoinViewModel
import com.example.payeh.screen.login.Registration

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Myappnav() {
    val navController = rememberNavController()

    // این ViewModel فقط برای تعیین صفحه شروع استفاده می‌شود
    val joinViewModel: JoinViewModel = hiltViewModel()
    val startDestination = if (joinViewModel.isLoggedIn()) "Home" else "Join"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("Home") {
            HomeScreen(navController)
        }
        composable("Accent") {
            // هر صفحه ViewModel خودش را می‌گیرد
            Accent(navController = navController)
        }
        composable("Join") {
            Join(navController)
        }
        composable("Registration") {
            Registration(navController)
        }
        composable("Chat") {
            ChatHome(navController)
        }
        composable(
            route = "chat/{channelId}&{channelName}",
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType },
                navArgument("channelName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: ""
            val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
            ChatSccren(navController, channelId, channelName)
        }
        composable("calendar") {
            TimelineCalendarScreen(selectedTimeStart = "", selectedTimeEnd = "")
        }
        composable("Room") {
            // صفحه Room به صورت داخلی ViewModel خودش (RoomViewModel) را می‌سازد
            // و دیگر خطای Type Mismatch رخ نمی‌دهد
            Room(navController = navController)
        }
        composable("Saved_Beneficiary") {
            SavedBeneficiary()
        }
        composable("My_Account") {
            CodiaMainView(navController = navController)
        }
        composable("Help_Support") {
            HelpSupport(navController)
        }
        composable("AboutApp") {
            AboutApp(navController)
        }
    }
}