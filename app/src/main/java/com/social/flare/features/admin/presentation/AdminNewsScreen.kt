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
import com.social.flare.features.admin.domain.model.NewsItem
import com.social.flare.features.admin.presentation.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewsScreen(
    viewModel: AdminViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingNews by remember { mutableStateOf<NewsItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadNews()
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Noticias", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar noticia", tint = Color(0xFFFF5722))
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
        } else if (uiState.news.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No hay noticias", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showCreateDialog = true }) {
                        Text("Crear primera noticia", color = Color(0xFFFF5722))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.news) { news ->
                    AdminNewsCard(
                        news = news,
                        onEdit = { editingNews = news },
                        onToggleActive = { viewModel.toggleNewsActive(news.newsId, !news.isActive) },
                        onDelete = { viewModel.deleteNews(news.newsId) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        NewsFormDialog(
            title = "Crear Noticia",
            initialTitle = "",
            initialDescription = "",
            onDismiss = { showCreateDialog = false },
            onSave = { t, d, _ ->
                viewModel.createNews(t, d, null)
                showCreateDialog = false
            }
        )
    }

    editingNews?.let { news ->
        NewsFormDialog(
            title = "Editar Noticia",
            initialTitle = news.title,
            initialDescription = news.description,
            onDismiss = { editingNews = null },
            onSave = { t, d, _ ->
                viewModel.updateNews(news.newsId, t, d, null)
                editingNews = null
            }
        )
    }
}

@Composable
private fun AdminNewsCard(
    news: NewsItem,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }

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
                        text = news.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (news.isActive) "Activa" else "Inactiva",
                        color = if (news.isActive) Color(0xFF4CAF50) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = news.description,
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateFormat.format(Date(news.createdAt)),
                    color = Color.DarkGray,
                    fontSize = 11.sp
                )
            }
            Row {
                IconButton(onClick = onToggleActive) {
                    Icon(
                        if (news.isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (news.isActive) "Desactivar" else "Activar",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFFF5722), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFF44336), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun NewsFormDialog(
    title: String,
    initialTitle: String,
    initialDescription: String,
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, imageUrl: String?) -> Unit
) {
    var newsTitle by remember { mutableStateOf(initialTitle) }
    var newsDescription by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        title = { Text(title, color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = newsTitle,
                    onValueChange = { newsTitle = it },
                    label = { Text("Título", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = Color(0xFFFF5722)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newsDescription,
                    onValueChange = { newsDescription = it },
                    label = { Text("Descripción", color = Color.Gray) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = Color(0xFFFF5722)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(newsTitle, newsDescription, null) },
                enabled = newsTitle.isNotBlank() && newsDescription.isNotBlank()
            ) {
                Text("Guardar", color = Color(0xFFFF5722))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
