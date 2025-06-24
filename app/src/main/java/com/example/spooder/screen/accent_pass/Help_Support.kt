package com.example.spooder.screen.accent_pass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupport(navController: NavController) {
    val primaryColor = Color(0xFF00664F)
    val backgroundColor = Color.White
    val textColor = Color(0xFF333333)
    val subtextColor = Color(0xFF666666)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = " Spooder Privacy Policy",
                color = primaryColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            PrivacySection(
                title = "Information We Collect",
                content = "TaskMaster collects minimal personal information to provide and improve our task management service.",
                primaryColor = primaryColor,
                textColor = textColor,
                subtextColor = subtextColor
            )

            PrivacySection(
                title = "How We Use Your Information",
                content = "We use the collected information to authenticate and manage your account, sync tasks across devices, and improve app functionality.",
                primaryColor = primaryColor,
                textColor = textColor,
                subtextColor = subtextColor
            )

            PrivacySection(
                title = "Data Security",
                content = "We implement industry-standard security measures to protect your personal information and task data. All data is encrypted during transmission and storage.",
                primaryColor = primaryColor,
                textColor = textColor,
                subtextColor = subtextColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Last Updated: March 2024",
                color = subtextColor,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
private fun PrivacySection(
    title: String,
    content: String,
    primaryColor: Color,
    textColor: Color,
    subtextColor: Color
) {
    Column(
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            color = primaryColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = content,
            color = subtextColor,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}