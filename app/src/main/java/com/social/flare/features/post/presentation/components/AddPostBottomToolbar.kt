package com.social.flare.features.post.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
public fun AddPostBottomToolbar(
    contentLength: Int,
    onOpenGallery: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenGallery) {
            Icon(Icons.Outlined.Image, contentDescription = "Add Media", tint = Color(0xFFFF5722))
        }
        IconButton(onClick = { /* TODO */ }) { Icon(Icons.Outlined.Poll, null, tint = Color.Gray) }
        IconButton(onClick = { /* TODO */ }) { Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray) }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "$contentLength/500",
            color = if (contentLength > 500) Color.Red else Color.Gray,
            fontSize = 12.sp
        )
    }
}