package com.social.flare.features.profile.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.social.flare.core.data.SettingsManager
import com.social.flare.core.ui.theme.textSizeScaleToFontScale
import com.social.flare.features.profile.presentation.components.settings.SettingsDarkModeSelector
import com.social.flare.features.profile.presentation.components.settings.SettingsItem
import com.social.flare.features.profile.presentation.components.settings.SettingsProfileHeader
import com.social.flare.features.profile.presentation.components.settings.SettingsSectionTitle
import com.social.flare.features.profile.presentation.components.settings.SettingsTextSizeSelector
import com.social.flare.features.profile.presentation.components.settings.SettingsToggleItem
import com.social.flare.features.profile.presentation.viewmodel.ProfileUiState
import com.social.flare.features.profile.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    activeCitizenId: String?,
    profileViewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onLogin: () -> Unit = {},
    onChangePassword: suspend (String) -> Result<Unit> = {
        Result.failure(Exception("Change password is not available"))
    },
    onNavigateToAdmin: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTermsOfService: () -> Unit = {},
    onNavigateToHelpCenter: () -> Unit = {}
) {
    val isGuest = activeCitizenId == null
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val pushNotificationsEnabled by settingsManager.pushNotificationsEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val emailNotificationsEnabled by settingsManager.emailNotificationsEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val darkModeEnabled by settingsManager.darkModeEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val textSizeScale by settingsManager.textSizeScaleFlow.collectAsStateWithLifecycle(initialValue = 0.5f)
    val privateAccountEnabled by settingsManager.privateAccountEnabledFlow.collectAsStateWithLifecycle(initialValue = false)
    val showActivityStatusEnabled by settingsManager.showActivityStatusEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val allowProfileSearchEnabled by settingsManager.allowProfileSearchEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val currentDensity = LocalDensity.current
    val settingsFontScale = textSizeScaleToFontScale(textSizeScale)
    //var supportDialog by remember { mutableStateOf<SettingsSupportDialog?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPrivacySettingsDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            settingsManager.setPushNotificationsEnabled(isGranted)
        }
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(activeCitizenId) {
        if (activeCitizenId != null) {
            profileViewModel.loadActiveUserProfile(activeCitizenId)
        }
    }
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme
    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (isGuest) {
                SettingsProfileHeader(
                    avatarUrl = null,
                    displayName = "Guest User",
                    username = "guest",
                    showEditButton = false
                )
            } else {
                when (profileState) {
                    is ProfileUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = colorScheme.primary)
                        }
                    }

                    is ProfileUiState.Success -> {
                        val success = profileState as ProfileUiState.Success
                        val currentCitizen = success.citizen

                        SettingsProfileHeader(
                            avatarUrl = currentCitizen?.avatar_url,
                            displayName = currentCitizen?.display_name,
                            username = currentCitizen?.username,
                            onEditClick = onNavigateToEditProfile
                        )

                        if (currentCitizen?.is_admin == true) {
                            SettingsSectionTitle("ADMIN")
                            SettingsItem(
                                icon = Icons.Default.AdminPanelSettings,
                                title = "Admin Panel",
                                onClick = onNavigateToAdmin
                            )
                        }
                    }

                    else -> {
                        SettingsProfileHeader(
                            avatarUrl = null,
                            displayName = "User",
                            username = "",
                            onEditClick = onNavigateToEditProfile
                        )
                    }
                }
            }

            if (!isGuest) {
                SettingsSectionTitle("ACCOUNT")
                SettingsItem(Icons.Default.Person, "Edit Profile", onClick = onNavigateToEditProfile)
                SettingsItem(Icons.Default.Lock, "Change Password", onClick = { showChangePasswordDialog = true })
                SettingsItem(
                    icon = Icons.Default.Shield,
                    title = "Privacy Settings",
                    onClick = { showPrivacySettingsDialog = true }
                )
            }

            SettingsSectionTitle("NOTIFICATIONS")
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                checked = pushNotificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        val requiresPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        val permissionGranted = !requiresPermission ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED

                        if (permissionGranted) {
                            scope.launch {
                                settingsManager.setPushNotificationsEnabled(true)
                            }
                        } else {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        scope.launch {
                            settingsManager.setPushNotificationsEnabled(false)
                        }
                    }
                }
            )
            if (!isGuest) {
                SettingsToggleItem(
                    icon = Icons.Default.Email,
                    title = "Email Notifications",
                    checked = emailNotificationsEnabled,
                    onCheckedChange = { enabled ->
                        scope.launch {
                            settingsManager.setEmailNotificationsEnabled(enabled)
                        }
                    }
                )
            }

            SettingsSectionTitle("DISPLAY")
            SettingsDarkModeSelector(
                checked = darkModeEnabled,
                onCheckedChange = { enabled ->
                    scope.launch {
                        settingsManager.setDarkModeEnabled(enabled)
                    }
                }
            )
            SettingsTextSizeSelector(
                value = textSizeScale,
                onValueChange = { value ->
                    scope.launch {
                        settingsManager.setTextSizeScale(value)
                    }
                }
            )

            SettingsSectionTitle("SUPPORT")
            SettingsItem(
                icon = Icons.Default.Description,
                title = "Privacy Policy",
                onClick = onNavigateToPrivacyPolicy
            )
            SettingsItem(
                icon = Icons.Default.Assignment,
                title = "Terms of Service",
                onClick = onNavigateToTermsOfService
            )
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help Center",
                onClick = onNavigateToHelpCenter
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = if (isGuest) onLogin else onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGuest) colorScheme.primary else colorScheme.error.copy(alpha = 0.10f),
                    contentColor = if (isGuest) colorScheme.onPrimary else colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isGuest) null else BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.35f))
            ) {
                Text(
                    text = if (isGuest) "Log In" else "Log Out",
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = onChangePassword,
            onSuccess = {
                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPrivacySettingsDialog && !isGuest) {
        PrivacySettingsDialog(
            privateAccountEnabled = privateAccountEnabled,
            showActivityStatusEnabled = showActivityStatusEnabled,
            allowProfileSearchEnabled = allowProfileSearchEnabled,
            onPrivateAccountChange = { enabled ->
                scope.launch {
                    settingsManager.setPrivateAccountEnabled(enabled)
                }
            },
            onShowActivityStatusChange = { enabled ->
                scope.launch {
                    settingsManager.setShowActivityStatusEnabled(enabled)
                }
            },
            onAllowProfileSearchChange = { enabled ->
                scope.launch {
                    settingsManager.setAllowProfileSearchEnabled(enabled)
                }
            },
            onDismiss = { showPrivacySettingsDialog = false }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: suspend (String) -> Result<Unit>,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (!isSaving) {
                onDismiss()
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Change Password", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("Confirm new password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                errorMessage?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    errorMessage = validatePasswordChange(newPassword, confirmPassword)
                    if (errorMessage != null) {
                        return@TextButton
                    }

                    scope.launch {
                        isSaving = true
                        val result = onChangePassword(newPassword)
                        isSaving = false

                        if (result.isSuccess) {
                            onDismiss()
                            onSuccess()
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Unable to update password"
                        }
                    }
                }
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Update Password",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

private fun validatePasswordChange(newPassword: String, confirmPassword: String): String? {
    return when {
        newPassword.isBlank() -> "New password cannot be empty"
        confirmPassword.isBlank() -> "Confirm password cannot be empty"
        newPassword.length < 6 -> "Password must be at least 6 characters"
        newPassword != confirmPassword -> "Passwords do not match"
        else -> null
    }
}

@Composable
private fun PrivacySettingsDialog(
    privateAccountEnabled: Boolean,
    showActivityStatusEnabled: Boolean,
    allowProfileSearchEnabled: Boolean,
    onPrivateAccountChange: (Boolean) -> Unit,
    onShowActivityStatusChange: (Boolean) -> Unit,
    onAllowProfileSearchChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Privacy Settings", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PrivacySettingSwitchRow(
                    title = "Private Account",
                    checked = privateAccountEnabled,
                    onCheckedChange = onPrivateAccountChange
                )
                PrivacySettingSwitchRow(
                    title = "Show Activity Status",
                    checked = showActivityStatusEnabled,
                    onCheckedChange = onShowActivityStatusChange
                )
                PrivacySettingSwitchRow(
                    title = "Allow Profile Search",
                    checked = allowProfileSearchEnabled,
                    onCheckedChange = onAllowProfileSearchChange
                )
                Text(
                    text = "These privacy preferences are saved on this device. Backend enforcement can be added when remote privacy fields are available.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun PrivacySettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}
