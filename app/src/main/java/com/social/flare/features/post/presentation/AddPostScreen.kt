package com.social.flare.features.post.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.post.presentation.components.AddPostTopBar
import com.social.flare.features.post.presentation.components.AddPostBottomToolbar
import com.social.flare.features.post.presentation.components.AddPostInputArea
import com.social.flare.features.post.presentation.components.AddPostMediaPreview
import kotlinx.coroutines.delay

@Composable
fun AddPostScreen(
    onNavigateBack: () -> Unit,
    onPostClick: (String, List<Uri>) -> Unit,
    isSuccess: Boolean = false,
    onSuccessHandled: () -> Unit = {}
) {
    var showSuccessOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            showSuccessOverlay = true
            delay(1200)
            onSuccessHandled()
        }
    }
    var content by remember { mutableStateOf("") }
    var selectedMedia by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val isPostEnabled = content.isNotBlank() || selectedMedia.isNotEmpty()
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) selectedMedia = selectedMedia + uris
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            AddPostTopBar(
                isPostEnabled = isPostEnabled,
                onNavigateBack = onNavigateBack,
                onPostClick = { onPostClick(content, selectedMedia) }
            )
        },
        bottomBar = {
            AddPostBottomToolbar(
                contentLength = content.length,
                onOpenGallery = {
                    mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            AddPostInputArea(
                content = content,
                onContentChange = { content = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedMedia.isNotEmpty()) {
                AddPostMediaPreview(
                    mediaUris = selectedMedia,
                    onRemoveMedia = { uriToRemove ->
                        selectedMedia = selectedMedia.filter { it != uriToRemove }
                    }
                )
            }
        }

        if (showSuccessOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "¡Publicado con éxito!",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu publicación ya está en el feed",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}