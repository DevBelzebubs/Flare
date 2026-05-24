package com.social.flare.features.main.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.social.flare.core.navigation.Screen

@Composable
fun FlareBottomNavigation(
    currentRoute: String,
    isGuest: Boolean,
    onRequireAuth: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val handleNavigate = { route: String ->
        if (currentRoute != route) {
            onNavigate(route)
        }
    }

    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Feed.route,
            onClick = { handleNavigate(Screen.Feed.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Search.route,
            onClick = { handleNavigate(Screen.Search.route) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.AddPost.route,
            onClick = { handleNavigate(Screen.AddPost.route) },
            icon = {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = "Add",
                    tint = Color(0xFFFF5722),
                    modifier = Modifier.size(36.dp)
                )
            },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Notifications.route,
            onClick = { handleNavigate(Screen.Notifications.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Activity") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute?.startsWith(Screen.Profile.route) == true,
            onClick = { handleNavigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}