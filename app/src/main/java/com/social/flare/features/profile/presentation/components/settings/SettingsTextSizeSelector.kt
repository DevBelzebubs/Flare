package com.social.flare.features.profile.presentation.components.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsTextSizeSelector(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text("A", color = Color.Gray, fontSize = 12.sp)
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            steps = 1,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF5722),
                activeTrackColor = Color(0xFFFF5722),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
        Text("A", color = Color.White, fontSize = 18.sp)
    }
}
