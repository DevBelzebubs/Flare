package com.social.flare.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.admin.presentation.components.AddAiProfileDialog
import com.social.flare.features.admin.presentation.components.AdminStatCard
import com.social.flare.features.admin.presentation.viewmodel.AdminViewModel
import com.social.flare.features.ai.domain.model.AiPersona

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminViewModel,
    onNavigateToUsers: () -> Unit,
    onNavigateToPosts: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddAiDialog by remember { mutableStateOf(false) }
    var editingBot by remember { mutableStateOf<AiPersona?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<AiPersona?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    if (showAddAiDialog) {
        AddAiProfileDialog(
            onDismiss = { showAddAiDialog = false },
            onConfirm = { username, displayName, prompt, temp ->
                viewModel.createAiProfile(username, displayName, prompt, temp)
                showAddAiDialog = false
            }
        )
    }

    editingBot?.let { bot ->
        AddAiProfileDialog(
            editingBot = bot,
            onDismiss = { editingBot = null },
            onConfirm = { username, displayName, prompt, temp ->
                viewModel.updateAiProfile(bot.citizenId, username, displayName, prompt, temp)
                editingBot = null
            }
        )
    }

    showDeleteConfirm?.let { bot ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Eliminar Bot") },
            text = { Text("¿Estás seguro de eliminar al bot \"${bot.displayName}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteBot(bot.citizenId)
                    showDeleteConfirm = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it) }
        viewModel.clearMessages()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Admin Panel", color = colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Dashboard",
                color = colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            } else {
                val data = uiState.dashboard
                if (data != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            label = "Usuarios",
                            value = data.totalUsers.toString(),
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = "Posts",
                            value = data.totalPosts.toString(),
                            color = Color(0xFFFF5722),
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = "Activos",
                            value = data.activeUsers.toString(),
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AdminStatCard(
                            label = "Bloqueados",
                            value = data.blockedUsers.toString(),
                            color = Color(0xFFF44336),
                            modifier = Modifier.weight(1f)
                        )
                        AdminStatCard(
                            label = "Noticias",
                            value = data.totalNews.toString(),
                            color = Color(0xFF9C27B0),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Gestión",
                color = colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            AdminMenuItem(
                icon = Icons.Default.People,
                title = "Usuarios",
                subtitle = "Gestionar cuentas y estados",
                onClick = onNavigateToUsers
            )
            AdminMenuItem(
                icon = Icons.Default.Article,
                title = "Publicaciones",
                subtitle = "Moderar contenido",
                onClick = onNavigateToPosts
            )
            AdminMenuItem(
                icon = Icons.Default.Newspaper,
                title = "Noticias",
                subtitle = "Administrar sección de noticias",
                onClick = onNavigateToNews
            )
            AdminMenuItem(
                icon = Icons.Default.Build,
                title = "Agentes IA",
                subtitle = "Crear y configurar vecinos virtuales",
                onClick = { showAddAiDialog = true }
            )
            if (uiState.bots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Agentes IA Registrados",
                    color = colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                uiState.bots.forEach { bot ->
                    BotItem(
                        persona = bot,
                        onEdit = { editingBot = bot },
                        onDelete = { showDeleteConfirm = bot },
                        onToggle = { isChecked ->
                            viewModel.toggleBotStatus(bot.citizenId, isChecked)
                        },
                        isLoading = "bot:${bot.citizenId}" in uiState.actionLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(subtitle, color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colorScheme.onSurfaceVariant)
        }
    }
}
@Composable
fun BotItem(
    persona: AiPersona,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: (Boolean) -> Unit,
    isLoading: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar bot",
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = persona.displayName,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${persona.username}",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    color = colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar bot",
                            tint = colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Switch(
                        checked = persona.isActive,
                        onCheckedChange = { onToggle(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colorScheme.onPrimary,
                            checkedTrackColor = colorScheme.primary,
                            uncheckedThumbColor = colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
