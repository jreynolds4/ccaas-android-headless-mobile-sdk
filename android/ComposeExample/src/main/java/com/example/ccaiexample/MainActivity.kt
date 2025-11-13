package com.example.ccaiexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ccaiplatform.ccaiui.CCAIUI
import com.example.ccaiexample.view.ChatView
import com.example.ccaiexample.view.HomeView
import com.example.shared.InitController

sealed class MainScreen(val route: String) {
    data object HomeView : MainScreen("homeView")
    data object ChatView : MainScreen("chatView")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        InitController.instance.initializeSDK(this)
        // Initialize CCAIUI for push notifications
        CCAIUI.initialize(this)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = MainScreen.HomeView.route) {
                composable(route = MainScreen.HomeView.route) {
                    HomeView(navController)
                }
                composable(route = MainScreen.ChatView.route) {
                    ChatView(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeViewPreview() {
    val navController = rememberNavController()
    HomeView(navController)
}
