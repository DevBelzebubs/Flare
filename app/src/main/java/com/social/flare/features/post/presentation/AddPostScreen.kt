package com.social.flare.features.post.presentation


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.feed.presentation.components.VideoPlayer

@Composable
fun AddPostScreen(
    onNavigateBack: () -> Unit,
    onPostClick: (String, List<Uri>) -> Unit
) {
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPostTopBar(
    isPostEnabled: Boolean,
    onNavigateBack: () -> Unit,
    onPostClick: () -> Unit
) {
    TopAppBar(
        title = { Text("New Post", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
        },
        actions = {
            Button(
                onClick = onPostClick,
                enabled = isPostEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF5722),
                    disabledContainerColor = Color(0xFF4A1F11)
                ),
                modifier = Modifier.padding(end = 16.dp).height(36.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("Post", color = if (isPostEnabled) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
    )
}

@Composable
private fun AddPostInputArea(
    content: String,
    onContentChange: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray)
        }

        Spacer(modifier = Modifier.width(12.dp))

        TextField(
            value = content,
            onValueChange = { if (it.length <= 500) onContentChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("¿Qué tienes en mente?", color = Color.DarkGray, fontSize = 16.sp) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFFFF5722), focusedTextColor = Color.White, unfocusedTextColor = Color.White
            ),
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, lineHeight = 24.sp)
        )
    }
}
@Composable
private fun AddPostMediaPreview(
    mediaUris: List<Uri>,
    onRemoveMedia: (Uri) -> Unit
) {
    val context = LocalContext.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 56.dp)
    ) {
        items(mediaUris) { uri ->
            val mimeType = context.contentResolver.getType(uri)
            val isVideo = mimeType?.startsWith("video") == true

            Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))) {
                if (isVideo) {
                    VideoPlayer(
                        videoUrl = uri.toString(),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                        .clip(CircleShape).background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onRemoveMedia(uri) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddPostBottomToolbar(
    contentLength: Int,
    onOpenGallery: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenGallery) {
            Icon(Icons.Outlined.Image, contentDescription = "Add Media", tint = Color(0xFFFF5722))
        }
        IconButton(onClick = { /* TODO */ }) { Icon(Icons.Outlined.Poll, null, tint = Color.Gray) }
        IconButton(onClick = { /* TODO */ }) { Icon(Icons.Outlined.LocationOn, null, tint = Color.Gray) }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "$contentLength/500",
            color = if (contentLength > 500) Color.Red else Color.Gray,
            fontSize = 12.sp
        )
    }
}