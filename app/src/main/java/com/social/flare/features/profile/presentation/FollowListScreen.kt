package com.social.flare.features.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.presentation.viewmodel.FollowListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    userId: String,
    type: String,
    activeCitizenId: String?,
    viewModel: FollowListViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    val users by viewModel.users.collectAsState()
    val followedIds by viewModel.followedIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val title = if (type == "followers") "Followers" else "Following"

    LaunchedEffect(userId, type, activeCitizenId) {
        viewModel.load(userId, type, activeCitizenId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFFFF5722),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (users.isEmpty()) {
                Text(
                    text = if (type == "followers") "No followers yet" else "Not following anyone yet",
                    color = Color.Gray,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(users, key = { it.citizen_id }) { citizen ->
                        FollowUserItem(
                            citizen = citizen,
                            isFollowing = citizen.citizen_id in followedIds,
                            activeCitizenId = activeCitizenId,
                            onFollowClick = { viewModel.toggleFollow(citizen.citizen_id) },
                            onProfileClick = { onNavigateToProfile(citizen.citizen_id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowUserItem(
    citizen: CitizenEntity,
    isFollowing: Boolean,
    activeCitizenId: String?,
    onFollowClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        AsyncImage(
            model = citizen.avatar_url,
            contentDescription = "Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = citizen.display_name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = citizen.username,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (citizen.citizen_id != activeCitizenId) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.DarkGray else Color(0xFFFF5722)
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = if (isFollowing) "Siguiendo" else "Seguir",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
