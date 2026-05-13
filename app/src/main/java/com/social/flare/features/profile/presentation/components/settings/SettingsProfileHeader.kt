package com.social.flare.features.profile.presentation.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsProfileHeader() {
    Row(
        modifier = Modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(15.dp), tint = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722))
                    .align(Alignment.BottomEnd)
                    .border(2.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Jordan Flare", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("@jordan_ignite", color = Color.Gray, fontSize = 14.sp)
        }
    }
}