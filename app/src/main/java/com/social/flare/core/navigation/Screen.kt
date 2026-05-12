package com.social.flare.core.navigation

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Search : Screen("search")
    object AddPost : Screen("add_post")
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Settings : Screen("settings")
    object PostDetail: Screen("post_details")
}