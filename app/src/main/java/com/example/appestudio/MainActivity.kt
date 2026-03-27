package com.example.appestudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.appestudio.navigation.AppNavigation
import com.example.appestudio.navigation.Screen
import com.example.appestudio.ui.components.BottomNav
import com.example.appestudio.ui.theme.AppEstudioTheme
import com.example.appestudio.ui.theme.Slate900
import com.example.appestudio.data.SessionManager
import com.example.appestudio.data.network.RetrofitClient
import com.example.appestudio.data.network.SocketHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Initialize Socket.IO
        SocketHandler.setSocket()
        SocketHandler.establishConnection()
        setContent {
            AppEstudioTheme {
                val sessionManager = remember { SessionManager(applicationContext) }
                // Wire JWT interceptor + 401 auto-logout
                remember(sessionManager) { RetrofitClient.init(sessionManager); sessionManager }
                val startDestination = if (sessionManager.isLoggedIn()) Screen.Dashboard.route else Screen.Welcome.route
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route ?: startDestination

                // Avoid showing bottom bar on Welcome
                val showBottomBar = currentRoute != Screen.Welcome.route && !currentRoute.startsWith("chat/")

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            BottomNav(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate900)
                            .padding(innerPadding)
                    ) {
                        AppNavigation(navController = navController, startDestination = startDestination, sessionManager = sessionManager)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SocketHandler.closeConnection()
    }
}