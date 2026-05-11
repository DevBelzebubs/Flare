package com.social.flare.features.main.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

// Importaciones cruciales de tus features y core
import com.social.flare.core.navigation.Screen
import com.social.flare.core.ui.components.AuthDialog
import com.social.flare.features.auth.presentation.LoginScreen
import com.social.flare.features.auth.presentation.SignUpScreen
import com.social.flare.features.feed.presentation.FeedScreen
import com.social.flare.features.search.presentation.SearchScreen

// Importaciones NUEVAS para el Perfil
import com.social.flare.FlareApp
import com.social.flare.features.profile.presentation.ProfileScreen
import com.social.flare.features.profile.presentation.SettingsScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current

    var showAuthDialog by remember { mutableStateOf(false) }

    var citizenIdLoadedByRoomForTesting by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        citizenIdLoadedByRoomForTesting = null
    }

    Scaffold(
        topBar = {
            if (currentRoute != Screen.Login.route && currentRoute != Screen.SignUp.route) {
                FlareTopBar(onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                })
            }
        },
        bottomBar = {
            val isMainTab = listOf(
                Screen.Feed.route, Screen.Search.route, Screen.AddPost.route,
                Screen.Notifications.route, Screen.Profile.route
            ).contains(currentRoute)

            if (isMainTab) {
                FlareBottomNavigation(
                    currentRoute = currentRoute ?: Screen.Feed.route,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Feed.route
            ) {
                composable(Screen.Feed.route) {
                    FeedScreen(
                        activeCitizenId = citizenIdLoadedByRoomForTesting,
                        onRequireAuth = { showAuthDialog = true }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen()
                }
                composable(Screen.AddPost.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Add Post Screen", color = Color.White)
                    }
                }
                composable(Screen.Notifications.route) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Notifications Screen", color = Color.White)
                    }
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        citizenId = citizenIdLoadedByRoomForTesting,
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                        onLoginSuccess = { tokenOrId ->
                            citizenIdLoadedByRoomForTesting = tokenOrId
                            navController.navigate(Screen.Feed.route) { popUpTo(0) }
                        }
                    )
                }
                composable(Screen.SignUp.route) {
                    SignUpScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                        onSignUpSuccess = { tokenOrId ->
                            citizenIdLoadedByRoomForTesting = tokenOrId
                            navController.navigate(Screen.Feed.route) { popUpTo(0) }
                        }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onNavigateBack = { navController.popBackStack() })
                }
            }
        }

        if (showAuthDialog) {
            AuthDialog(
                onDismiss = { showAuthDialog = false },
                onLoginClick = {
                    showAuthDialog = false
                    navController.navigate(Screen.Login.route)
                },
                onSignUpClick = {
                    showAuthDialog = false
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlareTopBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = "Flare", color = Color(0xFFFF5722), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

@Composable
private fun FlareBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Feed.route,
            onClick = { onNavigate(Screen.Feed.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Search.route,
            onClick = { onNavigate(Screen.Search.route) },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.AddPost.route,
            onClick = { onNavigate(Screen.AddPost.route) },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = Color(0xFFFF5722), modifier = Modifier.size(36.dp)) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Notifications.route,
            onClick = { onNavigate(Screen.Notifications.route) },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Activity") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
    }
}