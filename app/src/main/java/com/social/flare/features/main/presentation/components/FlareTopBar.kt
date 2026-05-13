package com.social.flare.features.main.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlareTopBar(onSettingsClick: () -> Unit) {
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