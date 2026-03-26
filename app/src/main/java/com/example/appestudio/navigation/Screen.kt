package com.example.appestudio.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Dashboard : Screen("dashboard")
    object Feed : Screen("feed?tag={tag}")
    object Chat : Screen("chat")
    object Videos : Screen("videos")
    object Profile : Screen("profile")
    object ChatDetail : Screen("chat/{chatId}/{chatName}")
    object NewChat : Screen("new_chat")
}
