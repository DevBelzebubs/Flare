package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
public fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}