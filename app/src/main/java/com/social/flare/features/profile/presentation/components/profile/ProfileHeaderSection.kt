package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.social.flare.features.auth.data.local.entity.CitizenEntity

@Composable
public fun ProfileHeaderSection(citizen: CitizenEntity) {
    val bannerHeight = 180
    Box(modifier = Modifier.fillMaxWidth().height(260.dp).statusBarsPadding()) {
        AsyncImage(
            model = citizen.banner_url,
            contentDescription = "Banner",
            placeholder = rememberVectorPainter(image = Icons.Default.Wallpaper),
            error = rememberVectorPainter(image = Icons.Default.Wallpaper),
            modifier = Modifier.fillMaxWidth().height(bannerHeight.dp).background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxWidth().height(bannerHeight.dp).background(
            Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xFF1F1F1F)), startY = bannerHeight.toFloat() * 0.55f)
        ))
        AsyncImage(
            model = citizen.avatar_url,
            contentDescription = "Avatar",
            placeholder = rememberVectorPainter(image = Icons.Default.Person),
            error = rememberVectorPainter(image = Icons.Default.Person),
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    shadowElevation = 8f
                    shape = CircleShape
                    clip = true
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}
