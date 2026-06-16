package com.social.flare.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.AuthViewModelFactory(LocalContext.current))
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val colorScheme = MaterialTheme.colorScheme

    val passwordStrengthLevel by remember(password) {
        derivedStateOf {
            if (password.isEmpty()) 0
            else {
                var score = 0
                if (password.length >= 8) score++
                if (password.any { it.isDigit() }) score++
                if (password.any { it.isUpperCase() }) score++
                if (password.any { !it.isLetterOrDigit() }) score++
                
                // Asegurar al menos nivel 1 si no está vacío
                maxOf(1, score)
            }
        }
    }

    val strengthText = when (passwordStrengthLevel) {
        0 -> ""
        1 -> "WEAK"
        2 -> "FAIR"
        3 -> "GOOD"
        else -> "STRONG"
    }

    val strengthColor = when (passwordStrengthLevel) {
        1 -> colorScheme.error.copy(alpha = 0.85f)
        2 -> Color(0xFFFF9800) // Orange
        3 -> Color(0xFFFF7043) // Deep Orange
        4 -> colorScheme.primary // Flare Orange
        else -> Color.Transparent
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Botón de retroceso
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    // Logo
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
                        text = "Register",
                        color = colorScheme.primary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mostrar error
                    errorMessage?.let {
                        Text(
                            text = it,
                            color = colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Campos de texto
                    SignUpField(
                        label = "NAME",
                        value = firstName,
                        onValueChange = { firstName = it; errorMessage = null },
                        icon = Icons.Default.Person,
                        placeholder = "Your first name"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SignUpField(
                        label = "LAST NAME",
                        value = lastName,
                        onValueChange = { lastName = it; errorMessage = null },
                        icon = Icons.Default.Person,
                        placeholder = "Your last name"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SignUpField(
                        label = "MAIL",
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        icon = Icons.Default.Email,
                        placeholder = "ejemplo@gmail.com"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SignUpField(
                        label = "PASSWORD",
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        icon = Icons.Default.Lock,
                        placeholder = "Create a strong password",
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Password Strength Indicator
                    if (password.isNotEmpty()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("PASSWORD STRENGTH: ", color = colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text(strengthText, color = strengthColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(4) { index ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(3.dp)
                                            .background(
                                                if (index < passwordStrengthLevel) strengthColor else colorScheme.outline.copy(alpha = 0.35f),
                                                RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SignUpField(
                        label = "PASSWORD",
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it; errorMessage = null },
                        icon = Icons.Default.Lock,
                        placeholder = "repeat the password:",
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Registro
                    Button(
                        onClick = {
                            if (firstName.isNotBlank() && lastName.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                if (password == repeatPassword) {
                                    viewModel.registerUser(
                                        displayName = "$firstName $lastName",
                                        username = "@" + email.split("@")[0].lowercase(),
                                        email = email,
                                        pass = password,
                                        onSuccess = onSignUpSuccess,
                                        onError = { error -> errorMessage = error }
                                    )
                                } else {
                                    errorMessage = "Passwords do not match"
                                }
                            } else {
                                errorMessage = "Please fill all fields"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary,
                            contentColor = colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }



                    Spacer(modifier = Modifier.height(20.dp))

                    Row {
                        Text("Already have an account? ", color = colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        Text(
                            text = "Sign In",
                            color = colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun SignUpField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String,
    isPassword: Boolean = false
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 13.sp) },
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
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
}
