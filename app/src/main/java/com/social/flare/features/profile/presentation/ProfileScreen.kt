package com.social.flare.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.social.flare.features.auth.data.local.entity.CitizenEntity

@Composable
fun ProfileScreen(
    citizenId: String,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(citizenId) {
        viewModel.loadActiveUserProfile(citizenId)
    }

    Scaffold(
        containerColor = Color.Black
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFF5722))
                }
                is ProfileUiState.Success -> {
                    ProfileContent(state)
                }
                is ProfileUiState.UserNotFound -> {
                    Text("Session error: Citizen not found.", color = Color.Gray, modifier = Modifier.align(Alignment.Center))
                }
                is ProfileUiState.Error -> {
                    Text("Error: ${state.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(state: ProfileUiState.Success) {
    val citizen = state.citizen
    // Corrección de mutableIntOf a mutableStateOf
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Photos" to Icons.Default.GridOn, "Saved" to Icons.Default.BookmarkBorder)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            ProfileHeaderSection(citizen)
        }

        item {
            ProfileInfoSection(citizen)
        }

        item {
            ProfileStatsSection(state)
        }

        item {
            ProfileTabSection(tabs, selectedTab) { selectedTab = it }
        }

        item {
            Box(modifier = Modifier.height(300.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Grid content for ${tabs[selectedTab].first}...",
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun ProfileHeaderSection(citizen: CitizenEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        // En lugar de R.drawable, usamos un icono vectorial de Material como placeholder
        AsyncImage(
            model = citizen.banner_url,
            contentDescription = "Banner",
            placeholder = rememberVectorPainter(image = Icons.Default.Wallpaper),
            error = rememberVectorPainter(image = Icons.Default.Wallpaper),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.DarkGray), // Fondo gris si no hay imagen
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF1F1F1F)),
                    startY = 100f
                )
            ))

        AsyncImage(
            model = citizen.avatar_url,
            contentDescription = "Avatar",
            placeholder = rememberVectorPainter(image = Icons.Default.Person),
            error = rememberVectorPainter(image = Icons.Default.Person),
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomCenter)
                .clip(CircleShape)
                .background(Color.Gray) // Fondo gris si no hay imagen
                .border(4.dp, Color.Black, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ProfileInfoSection(citizen: CitizenEntity) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        Text(
            text = citizen.display_name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Text(
            text = citizen.username,
            color = Color(0xFFFF5722),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = citizen.bio ?: "No bio yet.",
            color = Color.LightGray,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ProfileStatsSection(state: ProfileUiState.Success) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color(0xFF121212))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(state.postsCount.toString(), "Posts")
        HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
        StatItem(state.followersCount.toString(), "Followers")
        HorizontalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color.DarkGray)
        StatItem(state.followingCount.toString(), "Following")
    }
}

@Composable
private fun StatItem(number: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileTabSection(tabs: List<Pair<String, ImageVector>>, selectedTab: Int, onTabSelected: (Int) -> Unit) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Black,
        contentColor = Color(0xFFFF5722),
        indicator = { tabPositions ->
            // Corrección de la firma del Indicator para Material 3
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = Color(0xFFFF5722)
            )
        }
    ) {
        tabs.forEachIndexed { index, pair ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = { Text(pair.first, color = if (selectedTab == index) Color.White else Color.Gray) },
                icon = { Icon(pair.second, contentDescription = pair.first, tint = if (selectedTab == index) Color(0xFFFF5722) else Color.Gray) }
            )
        }
    }
}