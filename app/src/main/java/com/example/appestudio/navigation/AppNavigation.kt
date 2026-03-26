package com.example.appestudio.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.appestudio.ui.theme.Slate400

import com.example.appestudio.ui.screens.auth.WelcomeScreen
import com.example.appestudio.ui.screens.chat.ChatScreen
import com.example.appestudio.ui.screens.chat.ChatDetailScreen
import com.example.appestudio.ui.screens.chat.NewChatScreen
import com.example.appestudio.ui.screens.dashboard.DashboardScreen
import com.example.appestudio.ui.screens.feed.FeedScreen
import com.example.appestudio.ui.screens.profile.ProfileScreen
import com.example.appestudio.ui.screens.video.VideosScreen

import com.example.appestudio.data.SessionManager

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route,
    sessionManager: SessionManager? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(Screen.Feed.route) {
            FeedScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(Screen.Chat.route) {
            ChatScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(Screen.Videos.route) {
            VideosScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(
            route = Screen.ChatDetail.route,
            arguments = listOf(
                navArgument("chatId")   { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType; defaultValue = "Chat" }
            )
        ) { backStackEntry ->
            val chatId   = backStackEntry.arguments?.getString("chatId")
            val chatName = backStackEntry.arguments?.getString("chatName") ?: "Chat"
            ChatDetailScreen(navController = navController, chatId = chatId, chatName = chatName, sessionManager = sessionManager)
        }
        composable(Screen.NewChat.route) {
            NewChatScreen(navController = navController, sessionManager = sessionManager)
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, color = Slate400)
    }
}
