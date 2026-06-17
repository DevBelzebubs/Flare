package com.social.flare.features.post.presentation.components

import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class PostLocationData(
    val name: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
) {
    val isValid: Boolean get() = name.isNotBlank() && lat != 0.0 && lng != 0.0
}

@Composable
fun AddLocationSection(
    location: PostLocationData,
    onLocationChanged: (PostLocationData?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionDenied by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                try {
                    val locationResult = withContext(Dispatchers.IO) {
                        Tasks.await(fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
                    }
                    if (locationResult != null) {
                        val lat = locationResult.latitude
                        val lng = locationResult.longitude
                        val name = withContext(Dispatchers.IO) {
                            try {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val addresses = geocoder.getFromLocation(lat, lng, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    listOfNotNull(
                                        addr.locality,
                                        addr.adminArea,
                                        addr.countryName
                                    ).take(2).joinToString(", ")
                                } else "Ubicación actual"
                            } catch (_: Exception) {
                                "Ubicación actual"
                            }
                        }
                        onLocationChanged(PostLocationData(name = name, lat = lat, lng = lng))
                    }
                    } catch (_: Exception) {
                        try {
                            val fallback = withContext(Dispatchers.IO) {
                                Tasks.await(fusedClient.lastLocation)
                            }
                            if (fallback != null) {
                                val lat = fallback.latitude
                                val lng = fallback.longitude
                                val name = withContext(Dispatchers.IO) {
                                    try {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            val addr = addresses[0]
                                            listOfNotNull(addr.locality, addr.adminArea, addr.countryName).take(2).joinToString(", ")
                                        } else "Ubicación actual"
                                    } catch (_: Exception) { "Ubicación actual" }
                                }
                                onLocationChanged(PostLocationData(name = name, lat = lat, lng = lng))
                            }
                        } catch (_: Exception) { }
                    }
                }
        } else {
            permissionDenied = true
        }
    }

    if (location.isValid) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(location.name, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { onLocationChanged(null) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove location", tint = Color.Gray, modifier = Modifier.size(16.dp))
            }
        }
    } else {
        Button(
            onClick = { locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFF5722), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Agregar ubicación", color = Color.White, fontSize = 13.sp)
        }
        if (permissionDenied) {
            Text(
                "Permiso de ubicación denegado. Actívalo en Ajustes.",
                color = Color.Red,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
