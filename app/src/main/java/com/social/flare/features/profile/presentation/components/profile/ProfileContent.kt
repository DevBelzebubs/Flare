package com.social.flare.features.profile.presentation.components.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. IMPORTAMOS EL MODELO DE DOMINIO CORRECTO
import com.social.flare.features.feed.domain.model.Post
import com.social.flare.features.profile.presentation.components.ProfileGridItem
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState

@Composable
fun ProfileContent(
    state: ProfileUiState.Success,
    myPosts: List<Post>,
    savedPosts: List<Post>,
    sharedPosts: List<Post>,
    onPostClick: (String) -> Unit,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {},
    isOtherProfile: Boolean = false,
    activeCitizenId: String? = null,
    isFollowingByMe: Boolean = false,
    onToggleFollow: () -> Unit = {}
) {
    val citizen = state.citizen
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Posts" to Icons.Default.ViewList,
        "Saved" to Icons.Default.BookmarkBorder,
        "Shared" to Icons.Default.Share
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        citizen?.let { safeCitizen ->
            item(span = { GridItemSpan(3) }) { ProfileHeaderSection(safeCitizen) }
            item(span = { GridItemSpan(3) }) { ProfileInfoSection(safeCitizen) }
            if (isOtherProfile && activeCitizenId != null) {
                item(span = { GridItemSpan(3) }) {
                    Button(
                        onClick = onToggleFollow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowingByMe) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = if (isFollowingByMe) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = if (isFollowingByMe) "Siguiendo" else "Seguir",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
        item(span = { GridItemSpan(3) }) {
            ProfileStatsSection(
                state = state,
                onFollowersClick = onFollowersClick,
                onFollowingClick = onFollowingClick
            )
        }
        item(span = { GridItemSpan(3) }) {
            ProfileTabSection(tabs, selectedTab) { selectedTab = it }
        }

        if (selectedTab == 0) {
            if (myPosts.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    EmptyTabPlaceholder(icon = Icons.Default.ViewList, text = "Aún no hay publicaciones")
                }
            } else {
                items(
                    items = myPosts,
                    key = { it.id }
                ) { post ->
                    ProfileGridItem(
                        post = post,
                        onClick = { onPostClick(post.id) }
                    )
                }
            }
        } else if (selectedTab == 1) {
            if (savedPosts.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    EmptyTabPlaceholder(icon = Icons.Default.BookmarkBorder, text = "No hay posts guardados")
                }
            } else {
                items(
                    items = savedPosts,
                    key = { it.id }
                ) { post ->
                    ProfileGridItem(
                        post = post,
                        onClick = { onPostClick(post.id) }
                    )
                }
            }
        } else {
            if (sharedPosts.isEmpty()) {
                item(span = { GridItemSpan(3) }) {
                    EmptyTabPlaceholder(icon = Icons.Default.Share, text = "No hay posts compartidos")
                }
            } else {
                items(
                    items = sharedPosts,
                    key = { it.id }
                ) { post ->
                    ProfileGridItem(
                        post = post,
                        onClick = { onPostClick(post.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTabPlaceholder(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Box(
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
