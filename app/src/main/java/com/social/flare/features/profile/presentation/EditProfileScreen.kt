package com.social.flare.features.profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.social.flare.features.auth.data.local.entity.CitizenEntity
import com.social.flare.features.profile.presentation.components.profile.ProfileEditTextField
import com.social.flare.features.profile.presentation.viewmodel.EditProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    citizen: CitizenEntity,
    viewModel: EditProfileViewModel,
    onNavigateBack: () -> Unit
) {
    var displayName by remember { mutableStateOf(citizen.display_name) }
    var username by remember { mutableStateOf(citizen.username) } // <--- ¡Añadido!
    var bio by remember { mutableStateOf(citizen.bio ?: "") }

    var avatarUri by remember { mutableStateOf<Uri?>(citizen.avatar_url?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }) }
    var bannerUri by remember { mutableStateOf<Uri?>(citizen.banner_url?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }) }

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSuccess by viewModel.isSuccess.collectAsStateWithLifecycle()

    val avatarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) avatarUri = uri
    }
    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) bannerUri = uri
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) onNavigateBack()
    }
    fun saveProfile() {
        viewModel.updateProfile(citizen, displayName, bio, avatarUri, bannerUri)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.background),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, tint = MaterialTheme.colorScheme.onBackground, contentDescription = null)
                }

                Text(
                    text = "Edit Profile",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                TextButton(
                    onClick = { saveProfile() },
                    enabled = !isLoading,
                    modifier = Modifier.width(60.dp)
                ) {
                    Text(
                        "Save",
                        color = if (isLoading) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp)) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                            AsyncImage(
                                model = bannerUri,
                                contentDescription = "Banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        bannerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    }
                            )
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .size(90.dp) // Tamaño del avatar
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .clickable {
                                    avatarLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                }
                        ) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Superposición de cámara en el centro del avatar
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    ProfileEditTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = "Display Name",
                        placeholder = "Juan Diego"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ProfileEditTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        placeholder = "@juan_diego",
                        trailingIcon = {  }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    ProfileEditTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = "Bio",
                        placeholder = "I write code and ride horses...",
                        singleLine = false,
                        modifier = Modifier.height(120.dp)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            Button(
                onClick = { saveProfile() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Save",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
