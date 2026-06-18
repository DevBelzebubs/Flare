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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.post.presentation.components.AddPollSection
import com.social.flare.features.post.presentation.components.AddPostBottomToolbar
import com.social.flare.features.post.presentation.components.AddPostInputArea
import com.social.flare.features.post.presentation.components.AddPostMediaPreview
import com.social.flare.features.post.presentation.components.AddLocationSection
import com.social.flare.features.post.presentation.components.PollData
import com.social.flare.features.post.presentation.components.PostLocationData
import com.social.flare.features.post.presentation.components.AddPostTopBar
import kotlinx.coroutines.delay

@Composable
fun AddPostScreen(
    onNavigateBack: () -> Unit,
    onPostClick: (String, List<Uri>, String?, List<String>?, Long?, String?, Double?, Double?) -> Unit,
    isSuccess: Boolean = false,
    onSuccessHandled: () -> Unit = {},
    activeUserAvatarUrl: String? = null
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
    var pollData by remember { mutableStateOf<PollData?>(null) }
    var locationData by remember { mutableStateOf<PostLocationData?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    val hasPoll = pollData != null
    val hasLocation = locationData != null
    val isPostEnabled = content.isNotBlank() || selectedMedia.isNotEmpty()

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) selectedMedia = selectedMedia + uris
    }

    fun handlePost() {
        onPostClick(
            content,
            selectedMedia,
            pollData?.question?.takeIf { it.isNotBlank() },
            pollData?.nonEmptyOptions,
            null,
            locationData?.name?.takeIf { it.isNotBlank() },
            locationData?.lat,
            locationData?.lng
        )
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            AddPostTopBar(
                isPostEnabled = isPostEnabled,
                onNavigateBack = onNavigateBack,
                onPostClick = { handlePost() }
            )
        },
        bottomBar = {
            AddPostBottomToolbar(
                contentLength = content.length,
                onOpenGallery = {
                    mediaPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                },
                onPollToggle = { pollData = if (hasPoll) null else PollData() },
                onLocationToggle = { locationData = if (hasLocation) null else PostLocationData() },
                isPollActive = hasPoll,
                isLocationActive = hasLocation
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            AddPostInputArea(
                content = content,
                onContentChange = { content = it },
                avatarUrl = activeUserAvatarUrl
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (hasPoll) {
                pollData?.let { currentPoll ->
                    AddPollSection(
                        pollData = currentPoll,
                        onQuestionChange = { question -> pollData = currentPoll.copy(question = question) },
                        onOptionChange = { index, option ->
                            val newOptions = currentPoll.options.toMutableList()
                            if (index in newOptions.indices) newOptions[index] = option
                            pollData = currentPoll.copy(options = newOptions)
                        },
                        onAddOption = {
                            if (currentPoll.options.size < 4) {
                                pollData = currentPoll.copy(options = currentPoll.options + "")
                            }
                        },
                        onRemoveOption = { index ->
                            val newOptions = currentPoll.options.toMutableList()
                            if (index in newOptions.indices && newOptions.size > 2) newOptions.removeAt(index)
                            pollData = currentPoll.copy(options = newOptions)
                        },
                        onRemovePoll = { pollData = null }
                    )
                }
            }

            if (hasLocation) {
                Spacer(modifier = Modifier.height(8.dp))
                AddLocationSection(
                    location = locationData ?: PostLocationData(),
                    onLocationChanged = { locationData = it }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    .background(colorScheme.scrim.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = MaterialTheme.shapes.large
                ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "¡Publicado con éxito!",
                        color = colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tu publicación ya está en el feed",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
                }
            }
        }
    }
}
