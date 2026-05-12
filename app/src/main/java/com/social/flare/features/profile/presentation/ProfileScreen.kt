package com.social.flare.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.profile.presentation.components.ProfileGridItem

@Composable
fun ProfileScreen(
    citizenId: String?,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(LocalContext.current)),
    onNavigateToLogin: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(citizenId) {
        if (citizenId != null) {
            viewModel.loadActiveUserProfile(citizenId)
        }
    }
    Scaffold(containerColor = Color.Black) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (citizenId == null) {
                GuestProfileView(onNavigateToLogin)
            } else {
                when (val state = uiState) {
                    is ProfileUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFF5722))
                    }
                    is ProfileUiState.Success -> {
                        ProfileContent(state, userPosts = state.posts)
                    }
                    is ProfileUiState.UserNotFound -> {
                        GuestProfileView(onNavigateToLogin)
                    }
                    is ProfileUiState.Error -> {
                        Text("Error: ${state.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestProfileView(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PersonOutline, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Not Logged In", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Please log in to see your profile and posts.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 8.dp))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onNavigateToLogin,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Log In / Sign Up", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileUiState.Success,
    userPosts: List<Post>
) {
    val citizen by state.citizen.collectAsState(initial = null)
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "Posts" to Icons.Default.ViewList,
        "Saved" to Icons.Default.BookmarkBorder,
        "Shared" to Icons.Default.Share
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
    ) {
        if (citizen != null) {
            val safeCitizen = citizen!!
            item(span = { GridItemSpan(3) }) { ProfileHeaderSection(safeCitizen) }
            item(span = { GridItemSpan(3) }) { ProfileInfoSection(safeCitizen) }
        }
        item(span = { GridItemSpan(3) }) { ProfileStatsSection(state) }
        item(span = { GridItemSpan(3) }) {
            ProfileTabSection(tabs, selectedTab) { selectedTab = it }
        }
        if (selectedTab == 0) {
            if (userPosts.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Aún no hay publicaciones", color = Color.Gray)
                    }
                }
            } else {
                items(userPosts) { post ->
                    ProfileGridItem(
                        post = post,
                        onClick = {
                            // navController.navigate(Screen.PostDetail.route + "/${post.id}")
                        }
                    )
                }
            }
        } else if (selectedTab == 1) {
            item(span = { GridItemSpan(3) }) {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay posts guardados.", color = Color.DarkGray)
                }
            }
        } else {
            item(span = { GridItemSpan(3) }) {
                Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay posts compartidos.", color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderSection(citizen: CitizenEntity) {
    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        AsyncImage(
            model = citizen.banner_url,
            contentDescription = "Banner",
            placeholder = rememberVectorPainter(image = Icons.Default.Wallpaper),
            error = rememberVectorPainter(image = Icons.Default.Wallpaper),
            modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.DarkGray),
            contentScale = ContentScale.Crop
        )
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(
            Brush.verticalGradient(colors = listOf(Color.Transparent, Color(0xFF1F1F1F)), startY = 100f)
        ))
        AsyncImage(
            model = citizen.avatar_url,
            contentDescription = "Avatar",
            placeholder = rememberVectorPainter(image = Icons.Default.Person),
            error = rememberVectorPainter(image = Icons.Default.Person),
            modifier = Modifier.size(100.dp).align(Alignment.BottomCenter).clip(CircleShape).background(Color.Gray).border(4.dp, Color.Black, CircleShape),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ProfileInfoSection(citizen: CitizenEntity) {
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

@Composable
private fun ProfileStatsSection(state: ProfileUiState.Success) {
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
        selectedTabIndex = selectedTab, containerColor = Color.Black, contentColor = Color(0xFFFF5722),
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color(0xFFFF5722))
        }
    ) {
        tabs.forEachIndexed { index, pair ->
            Tab(selected = selectedTab == index, onClick = { onTabSelected(index) },
                text = { Text(pair.first, color = if (selectedTab == index) Color.White else Color.Gray) },
                icon = { Icon(pair.second, contentDescription = pair.first, tint = if (selectedTab == index) Color(0xFFFF5722) else Color.Gray) }
            )
        }
    }
}