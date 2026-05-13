package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
public fun GuestProfileView(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Not Logged In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Please log in to see your profile and posts.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNavigateToLogin,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Log In / Sign Up", fontWeight = FontWeight.Bold)
        }
    }
}