package com.social.flare.features.profile.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsProfileHeader()

            SettingsSectionTitle("ACCOUNT")
            SettingsItem(Icons.Default.Person, "Edit Profile")
            SettingsItem(Icons.Default.Lock, "Change Password")
            SettingsItem(Icons.Default.Shield, "Privacy Settings")

            SettingsSectionTitle("NOTIFICATIONS")
            SettingsToggleItem(Icons.Default.Notifications, "Push Notifications", true)
            SettingsToggleItem(Icons.Default.Email, "Email Notifications", false)

            SettingsSectionTitle("DISPLAY")
            SettingsDarkModeSelector()
            SettingsTextSizeSelector()

            SettingsSectionTitle("SUPPORT")
            SettingsItem(Icons.Default.Description, "Privacy Policy", isExternal = true)
            SettingsItem(Icons.Default.Assignment, "Terms of Service", isExternal = true)
            SettingsItem(Icons.Default.Help, "Help Center", isExternal = true)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0000)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF440000))
            ) {
                Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun SettingsProfileHeader() {
    Row(
        modifier = Modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.fillMaxSize().padding(15.dp), tint = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5722))
                    .align(Alignment.BottomEnd)
                    .border(2.dp, Color.Black, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Black)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text("Jordan Flare", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("@jordan_ignite", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        color = Color(0xFFFF5722),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    )
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, isExternal: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (isExternal) Icons.Default.OpenInNew else Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.DarkGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsToggleItem(icon: ImageVector, title: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFF5722),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
fun SettingsDarkModeSelector() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text("Dark Mode", color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))

        // Custom Toggle ON/OFF como en Figma
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF121212))
                .padding(2.dp)
        ) {
            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFFF5722)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("ON", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                Text("OFF", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsTextSizeSelector() {
    var sliderValue by remember { mutableStateOf(0.5f) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text("A", color = Color.Gray, fontSize = 12.sp)
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFF5722),
                activeTrackColor = Color(0xFFFF5722),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
        Text("A", color = Color.White, fontSize = 18.sp)
    }
}