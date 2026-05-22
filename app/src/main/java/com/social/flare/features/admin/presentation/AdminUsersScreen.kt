package com.social.flare.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.social.flare.features.admin.domain.model.AdminUser
import com.social.flare.features.admin.presentation.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStatusDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios", color = Color.White, fontWeight = FontWeight.Bold) },
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.users) { user ->
                    AdminUserCard(
                        user = user,
                        onStatusChange = { showStatusDialog = user.citizenId },
                        onDelete = { viewModel.deleteUser(user.citizenId) }
                    )
                }
            }
        }
    }

    showStatusDialog?.let { userId ->
        val user = uiState.users.find { it.citizenId == userId }
        if (user != null) {
            AlertDialog(
                onDismissRequest = { showStatusDialog = null },
                containerColor = Color(0xFF1A1A1A),
                title = { Text("Estado de ${user.displayName}", color = Color.White) },
                text = {
                    Column {
                        StatusOption("active", "Activo", user.status) {
                            viewModel.updateUserStatus(userId, "active")
                            showStatusDialog = null
                        }
                        StatusOption("blocked", "Bloqueado", user.status) {
                            viewModel.updateUserStatus(userId, "blocked")
                            showStatusDialog = null
                        }
                        StatusOption("suspended", "Suspendido", user.status) {
                            viewModel.updateUserStatus(userId, "suspended")
                            showStatusDialog = null
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showStatusDialog = null }) {
                        Text("Cancelar", color = Color(0xFFFF5722))
                    }
                }
            )
        }
    }
}

@Composable
private fun StatusOption(
    value: String,
    label: String,
    currentStatus: String,
    onClick: () -> Unit
) {
    val isSelected = currentStatus == value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF5722))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = if (isSelected) Color.White else Color.Gray)
    }
}

@Composable
private fun AdminUserCard(
    user: AdminUser,
    onStatusChange: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (user.isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ADMIN",
                            color = Color(0xFFFF5722),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = user.username,
                    color = Color(0xFFFF5722),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (user.status) {
                        "active" -> Color(0xFF4CAF50)
                        "blocked" -> Color(0xFFF44336)
                        "suspended" -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                    val statusLabel = when (user.status) {
                        "active" -> "Activo"
                        "blocked" -> "Bloqueado"
                        "suspended" -> "Suspendido"
                        else -> user.status
                    }
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (!user.isAdmin) {
                IconButton(onClick = onStatusChange) {
                    Icon(Icons.Default.Shield, contentDescription = "Cambiar estado", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFF44336), modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Eliminar usuario", color = Color.White) },
            text = { Text("¿Eliminar a ${user.displayName}? Esta acción no se puede deshacer.", color = Color.Gray) },
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
