package com.example.ccaiexample.view

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ccaiexample.MainScreen
import co.ccai.example.compose_example.R
import com.example.ccaiexample.ui.theme.CCAIExampleTheme
import com.example.ccaiexample.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(navController: NavHostController) {
    val context = LocalContext.current
    val homeViewModel = viewModel<HomeViewModel>()

    var input by remember { mutableStateOf("") }
    val showProgress = homeViewModel.isLoading
    val errorMessage = homeViewModel.errorToast
    val showChatView = homeViewModel.showChatView

    LaunchedEffect(errorMessage) {
        errorMessage.takeIf { it.isNotEmpty() }?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(showChatView) {
        if (showChatView) {
            homeViewModel.resetShowChatView()
            val menuId = homeViewModel.menuId.toIntOrNull()
            val chat = homeViewModel.chat
            val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
            savedStateHandle?.set("menuId", menuId)
            savedStateHandle?.set("chat", chat)
            navController.navigate(MainScreen.ChatView.route) {
                launchSingleTop = true
            }
        }
    }

    CCAIExampleTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                LargeTopAppBar(
                    title = { Text(text = stringResource(R.string.customer_support)) },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                )
            }
        ) { innerPadding ->
            HomeViewContent(
                padding = innerPadding,
                inputText = input,
                onMenuIdChange = {
                    input = it
                    homeViewModel.menuId = it
                },
                isLoading = showProgress,
                onContactClick = homeViewModel::startContactCustomerSupport
            )
        }
    }
}
