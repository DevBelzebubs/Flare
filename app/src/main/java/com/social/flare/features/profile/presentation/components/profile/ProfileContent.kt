package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.profile.presentation.components.profile.ProfileHeaderSection
import com.social.flare.features.profile.presentation.components.profile.ProfileInfoSection
import com.social.flare.features.profile.presentation.components.profile.ProfileStatsSection
import com.social.flare.features.profile.presentation.components.profile.ProfileTabSection
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.profile.presentation.components.ProfileGridItem

@Composable
fun ProfileContent(
    state: ProfileUiState.Success,
    userPosts: List<Post>,
    onPostClick: (String) -> Unit
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
                        onClick = { onPostClick(post.id) }
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