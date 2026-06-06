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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fondo con resplandores (estilo Figma)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.0f to Color(0xFFFF9800).copy(alpha = 0.15f),
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
                        0.0f to Color(0xFFFF5722).copy(alpha = 0.1f),
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
                        0.0f to Color.White.copy(alpha = 0.08f),
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
                    .border(0.5.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A).copy(alpha = 0.85f)),
                shape = RoundedCornerShape(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to FLARE",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Logo (Flame icon inside chat bubble style)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.White, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Login",
                        color = Color(0xFFFF5722),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Mostrar error si existe
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Campo EMAIL
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("EMAIL", color = Color(0xFFFF5722), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                errorMessage = null
                            },
                            placeholder = { Text("example@gmail.com", color = Color.Gray.copy(alpha = 0.6f), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF252525),
                                unfocusedContainerColor = Color(0xFF252525),
                                focusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo PASSWORD
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PASSWORD", color = Color(0xFFFF5722), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                errorMessage = null
                            },
                            placeholder = { Text("Must have at least 8 characters", color = Color.Gray.copy(alpha = 0.6f), fontSize = 14.sp) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF252525),
                                unfocusedContainerColor = Color(0xFF252525),
                                focusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    Text(
                        text = "Forgot Password?",
                        color = Color(0xFFFF5722),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clickable { /* Lógica */ },
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                    ) {
                        Text("Login", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row {
                        Text("Don't have account? ", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Text(
                            text = "Register",
                            color = Color(0xFFFF5722),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToSignUp() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Footer (Or continue with)
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                Text(
                    text = " Or continue with ",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialIconPlaceholder(text = "G", color = Color.White)
                SocialIconPlaceholder(text = "f", color = Color.White)
            }
        }
    }
}

@Composable
fun SocialIconPlaceholder(text: String, color: Color) {
    Surface(
        modifier = Modifier
            .size(56.dp)
            .clickable { },
        shape = CircleShape,
        color = Color(0xFF1A1A1A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
    }
}
