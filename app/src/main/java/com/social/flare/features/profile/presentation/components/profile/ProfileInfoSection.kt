package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.auth.data.local.entity.CitizenEntity

@Composable
fun ProfileInfoSection(citizen: CitizenEntity) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            OutlinedButton(
                onClick = { /* TODO: Navegar a Editar */ },
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = citizen.display_name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text(text = citizen.username, color = Color(0xFFFF5722), fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = citizen.bio ?: "No bio yet.", color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(24.dp))
    }
}
