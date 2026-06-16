package com.social.flare.features.auth.presentation
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.AuthViewModelFactory(LocalContext.current))
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showResetPasswordDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    if (showResetPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showResetPasswordDialog = false },
            containerColor = colorScheme.surface,
            title = { Text("Reset Password", color = colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter your email to receive a reset link:", color = colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        placeholder = { Text("example@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = colorScheme.surfaceVariant,
                            unfocusedContainerColor = colorScheme.surfaceVariant,
                            focusedTextColor = colorScheme.onSurface,
                            unfocusedTextColor = colorScheme.onSurface,
                            focusedBorderColor = colorScheme.primary,
                            unfocusedBorderColor = colorScheme.outline,
                            cursorColor = colorScheme.primary,
                            focusedPlaceholderColor = colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = colorScheme.onSurfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showResetPasswordDialog = false
                        errorMessage = "If an account exists for $resetEmail, a reset link will be sent."
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary,
                        contentColor = colorScheme.onPrimary
                    )
                ) {
                    Text("Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetPasswordDialog = false }) {
                    Text("Cancel", color = colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Fondo con resplandores (estilo Figma)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.0f to colorScheme.primary.copy(alpha = 0.16f),
                        0.5f to Color.Transparent,
                        center = androidx.compose.ui.geometry.Offset(500f, 0f),
                        radius = 1000f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.0f to colorScheme.primary.copy(alpha = 0.1f),
                        0.6f to Color.Transparent,
                        center = androidx.compose.ui.geometry.Offset(0f, 1000f),
                        radius = 800f
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.0f to colorScheme.surfaceVariant.copy(alpha = 0.28f),
                        0.5f to Color.Transparent,
                        center = androidx.compose.ui.geometry.Offset(1000f, 2000f),
                        radius = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Card Principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.92f)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to FLARE",
                        color = colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logo (Flame icon inside chat bubble style)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colorScheme.onPrimary, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Login",
                        color = colorScheme.primary,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Mostrar error si existe
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Campo EMAIL
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EMAIL", color = colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                errorMessage = null
                            },
                            placeholder = { Text("example@gmail.com", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surfaceVariant,
                                unfocusedContainerColor = colorScheme.surfaceVariant,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                                focusedTextColor = colorScheme.onSurface,
                                unfocusedTextColor = colorScheme.onSurface,
                                cursorColor = colorScheme.primary,
                                focusedPlaceholderColor = colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = colorScheme.onSurfaceVariant
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo PASSWORD
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PASSWORD", color = colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("Must have at least 8 characters", fontSize = 14.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surfaceVariant,
                                unfocusedContainerColor = colorScheme.surfaceVariant,
                                focusedBorderColor = colorScheme.primary,
                                unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.5f),
                                focusedTextColor = colorScheme.onSurface,
                                unfocusedTextColor = colorScheme.onSurface,
                                cursorColor = colorScheme.primary,
                                focusedPlaceholderColor = colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = colorScheme.onSurfaceVariant
                            ),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "Forgot Password?",
                        color = colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable { 
                                resetEmail = email
                                showResetPasswordDialog = true
                            },
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Botón Login
                    Button(
                        onClick = {
                            viewModel.login(
                                username = email,
                                pass = password,
                                onSuccess = onLoginSuccess,
                                onError = { error -> errorMessage = error }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row {
                        Text("Don't have account? ", color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text(
                            text = "Register",
                            color = colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }
        }
    }
}
