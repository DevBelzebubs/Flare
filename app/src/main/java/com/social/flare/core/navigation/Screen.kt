package com.social.flare.core.navigation

sealed class Screen(val route: String) {
    object Feed : Screen("feed")
    object Search : Screen("search")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object AddPost : Screen("add_post")
}