package com.social.flare.features.feed.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.social.flare.features.feed.presentation.components.PostCard
import com.social.flare.features.feed.presentation.components.StoryCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { FlareTopBar() },
        bottomBar = { FlareBottomNavigation() },
        containerColor = Color.Black
    ) { paddingValues ->

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF5722))
                }
            }
            uiState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Error: ${uiState.error}", color = Color.Red)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item {
                        StoryCarousel()
                        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 1.dp)
                    }

                    items(uiState.posts) { post ->
                        PostCard(post = post)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlareTopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Flare",
                color = Color(0xFFFF5722),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        },
        actions = {
            IconButton(onClick = { /* TODO: Notificaciones */ }) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black
        )
    )
}

@Composable
private fun FlareBottomNavigation() {
    NavigationBar(
        containerColor = Color.Black,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFFF5722),
                unselectedIconColor = Color.Gray,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Add", tint = Color(0xFFFF5722), modifier = Modifier.size(36.dp)) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Activity") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
        )
        NavigationBarItem(
            selected = false,
            onClick = { /*TODO*/ },
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = Color.Gray)
        )
    }
}