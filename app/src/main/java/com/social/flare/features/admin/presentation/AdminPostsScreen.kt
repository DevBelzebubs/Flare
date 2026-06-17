package com.social.flare.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.admin.domain.model.AdminPost
import com.social.flare.features.admin.presentation.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPostsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadPosts()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Publicaciones", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF5722))
            }
        } else if (uiState.posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hay publicaciones", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.posts, key = { it.postId }) { post ->
                    val onDelete = remember(post) { { viewModel.deletePost(post.postId) } }
                    AdminPostCard(
                        post = post,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminPostCard(
    post: AdminPost,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.authorDisplayName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.authorUsername,
                        color = Color(0xFFFF5722),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = post.content ?: "(sin texto)",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(post.likesCount.toString(), color = Color.Gray, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(post.commentsCount.toString(), color = Color.Gray, fontSize = 12.sp)
                    }
                    Text(
                        text = dateFormat.format(Date(post.createdAt)),
                        color = Color.DarkGray,
                        fontSize = 11.sp
                    )
                }
            }
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar post", tint = Color(0xFFF44336), modifier = Modifier.size(20.dp))
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Eliminar publicación", color = Color.White) },
            text = { Text("¿Eliminar esta publicación de ${post.authorDisplayName}?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Eliminar", color = Color(0xFFF44336))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = Color(0xFFFF5722))
                }
            }
        )
    }
}
