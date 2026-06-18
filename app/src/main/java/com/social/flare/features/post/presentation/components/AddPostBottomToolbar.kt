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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
public fun AddPostBottomToolbar(
    contentLength: Int,
    onOpenGallery: () -> Unit,
    onPollToggle: () -> Unit = {},
    onLocationToggle: () -> Unit = {},
    isPollActive: Boolean = false,
    isLocationActive: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenGallery) {
            Icon(Icons.Outlined.Image, contentDescription = "Add Media", tint = colorScheme.primary)
        }
        IconButton(onClick = onPollToggle) {
            Icon(
                Icons.Outlined.Poll,
                contentDescription = "Add Poll",
                tint = if (isPollActive) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onLocationToggle) {
            Icon(
                Icons.Outlined.LocationOn,
                contentDescription = "Add Location",
                tint = if (isLocationActive) colorScheme.primary else colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "$contentLength/500",
            color = if (contentLength > 500) colorScheme.error else colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}
