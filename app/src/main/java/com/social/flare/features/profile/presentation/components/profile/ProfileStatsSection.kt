package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.social.flare.features.profile.presentation.ProfileUiState
import com.social.flare.features.profile.presentation.components.profile.StatItem

@Composable
fun ProfileStatsSection(state: ProfileUiState.Success) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).background(Color(0xFF121212)).padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(state.postsCount.toString(), "Posts")
        HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
        StatItem(state.followersCount.toString(), "Followers")
        HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
        StatItem(state.followingCount.toString(), "Following")
    }
}