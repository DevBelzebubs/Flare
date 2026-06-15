package com.social.flare.features.profile.presentation.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun SettingsProfileHeader(
    avatarUrl: String?,
    displayName: String?,
    username: String?,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = true,
    onEditClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .let {
                        if (showEditButton) {
                            it.clickable { onEditClick() }
                        } else {
                            it
                        }
                    }
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showEditButton) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .align(Alignment.BottomEnd)
                        .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .clickable { onEditClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = displayName ?: "User",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = username?.let { "$it" } ?: "",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}
