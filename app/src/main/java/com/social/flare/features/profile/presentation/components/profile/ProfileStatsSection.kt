package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState

@Composable
fun ProfileStatsSection(
    state: ProfileUiState.Success,
    followersCount: Int,
    followingCount: Int,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(modifier = Modifier.clickable { }) {
                StatItem(state.postsCount.toString(), "Posts")
            }
            HorizontalDivider(modifier = Modifier.height(36.dp).width(1.dp), color = MaterialTheme.colorScheme.outline)
            Box(modifier = Modifier.clickable { onFollowersClick() }) {
                StatItem(followersCount.toString(), "Followers")
            }
            HorizontalDivider(modifier = Modifier.height(36.dp).width(1.dp), color = MaterialTheme.colorScheme.outline)
            Box(modifier = Modifier.clickable { onFollowingClick() }) {
                StatItem(followingCount.toString(), "Following")
            }
        }
    }
}
