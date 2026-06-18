package com.social.flare.features.feed.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.location.Geocoder
import android.net.Uri
import com.google.android.gms.location.Priority
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.social.flare.features.feed.domain.model.FlareTrack
import com.social.flare.features.feed.presentation.MusicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class DrawnStroke(val points: List<Offset>, val color: Color = Color(0xFFFF5722), val strokeWidth: Float = 12f)
data class StoryText(val id: Int, val text: String, val offset: Offset = Offset(200f, 500f))

enum class StickerType { TIME, LOCATION, MENTION }
data class StorySticker(val id: Int, val type: StickerType, val value: String, val offset: Offset = Offset(300f, 400f))

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    selectedImageUri: Uri?,
    activeUserAvatarUrl: String?,
    musicViewModel: MusicViewModel,
    onCancel: () -> Unit,
    onShareToStory: (Uri, String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isDrawingMode by remember { mutableStateOf(false) }
    var showTextInput by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var isImageLoaded by remember { mutableStateOf(false) }
    var dragUpdateCounter by remember { mutableIntStateOf(0) }

    var strokes by remember { mutableStateOf(listOf<DrawnStroke>()) }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    val textElements = remember { mutableStateListOf<StoryText>() }
    var textIdCounter by remember { mutableStateOf(0) }

    val picture = remember { Picture() }
    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }

    var showMusicSheet by remember { mutableStateOf(false) }
    var selectedTrack by remember { mutableStateOf<FlareTrack?>(null) }
    var musicOffset by remember { mutableStateOf(Offset(300f, 600f)) }

    var showStickerSheet by remember { mutableStateOf(false) }
    val activeStickers = remember { mutableStateListOf<StorySticker>() }
    var stickerIdCounter by remember { mutableStateOf(0) }
    var pendingStickerType by remember { mutableStateOf<StickerType?>(null) }
    var stickerInputValue by remember { mutableStateOf("") }

    // --- LÓGICA DE UBICACIÓN AUTOMÁTICA ---
    var isFetchingLocation by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val fetchLocation = {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val location = Tasks.await(fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null))
                val lat = location.latitude
                val lng = location.longitude
                val locName = try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(lat, lng, 1)
                    val address = addresses?.firstOrNull()
                    val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea
                    val country = address?.countryName
                    if (city != null && country != null) "$city, $country" else city ?: country ?: "Ubicación Desconocida"
                } catch (_: Exception) {
                    "UBICACIÓN ACTUAL"
                }
                withContext(Dispatchers.Main) {
                    activeStickers.add(StorySticker(id = stickerIdCounter++, type = StickerType.LOCATION, value = locName.uppercase()))
                    isFetchingLocation = false
                    showStickerSheet = false
                }
            } catch (_: SecurityException) {
                withContext(Dispatchers.Main) {
                    isFetchingLocation = false
                    Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                try {
                    val lastLoc = Tasks.await(fusedLocationClient.lastLocation)
                    val lat = lastLoc.latitude
                    val lng = lastLoc.longitude
                    val locName = try {
                        val geocoder = Geocoder(context, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        val address = addresses?.firstOrNull()
                        val city = address?.locality ?: address?.subAdminArea ?: address?.adminArea
                        val country = address?.countryName
                        if (city != null && country != null) "$city, $country" else city ?: country ?: "Ubicación Desconocida"
                    } catch (_: Exception) {
                        "UBICACIÓN ACTUAL"
                    }
                    withContext(Dispatchers.Main) {
                    activeStickers.add(StorySticker(id = stickerIdCounter++, type = StickerType.LOCATION, value = locName.uppercase()))
                        isFetchingLocation = false
                        showStickerSheet = false
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        isFetchingLocation = false
                        Toast.makeText(context, "No se encontró ubicación (Enciende tu GPS)", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation()
        } else {
            isFetchingLocation = false
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val handleShare: () -> Unit = {
        if (!isSaving) {
            isSaving = true
            coroutineScope.launch {
                val newUri = createBitmapFromPicture(context, picture, viewWidth, viewHeight)
                if (newUri != null) {
                    onShareToStory(newUri, selectedTrack?.previewUrl)
                } else {
                    selectedImageUri?.let { onShareToStory(it, selectedTrack?.previewUrl) }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val triggerTextUpdate = textElements.toList()
                    val triggerStrokeUpdate = strokes.toList()
                    val triggerDrawUpdate = currentPoints.toList()
                    val triggerStickerUpdate = activeStickers.toList()
                    val triggerImageLoad = isImageLoaded
                    val triggerDragUpdate = dragUpdateCounter

                    val width = this.size.width.toInt()
                    val height = this.size.height.toInt()
                    viewWidth = width
                    viewHeight = height

                    onDrawWithContent {
                        synchronized(picture) {
                            val pictureCanvas = androidx.compose.ui.graphics.Canvas(picture.beginRecording(width, height))
                            draw(this, this.layoutDirection, pictureCanvas, this.size) {
                                this@onDrawWithContent.drawContent()
                            }
                            picture.endRecording()
                            drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
                        }
                    }
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(selectedImageUri).allowHardware(false).build(),
                contentDescription = "Selected Story Image", contentScale = ContentScale.Fit,
                onSuccess = { isImageLoaded = true },
                modifier = Modifier.fillMaxSize().padding(top = 90.dp, bottom = 100.dp).clip(RoundedCornerShape(16.dp))
            )

            Canvas(modifier = Modifier.fillMaxSize().pointerInput(isDrawingMode) {
                if (isDrawingMode) {
                    detectDragGestures(
                        onDragStart = { offset -> currentPoints = listOf(offset) },
                        onDrag = { change, _ -> change.consume(); currentPoints = currentPoints + change.position },
                        onDragEnd = {
                            if (currentPoints.isNotEmpty()) {
                                strokes = strokes + DrawnStroke(currentPoints)
                                currentPoints = emptyList()
                            }
                        }
                    )
                }
            }) {
                strokes.forEach { stroke ->
                    val path = Path().apply {
                        if (stroke.points.isNotEmpty()) {
                            moveTo(stroke.points.first().x, stroke.points.first().y)
                            for (i in 1 until stroke.points.size) lineTo(stroke.points[i].x, stroke.points[i].y)
                        }
                    }
                    drawPath(path = path, color = stroke.color, style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                if (currentPoints.isNotEmpty()) {
                    val path = Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) lineTo(currentPoints[i].x, currentPoints[i].y)
                    }
                    drawPath(path = path, color = Color(0xFFFF5722), style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            textElements.forEach { storyText ->
                DraggableTextItem(text = storyText, onDrag = { dragAmount ->
                    val textIndex = textElements.indexOfFirst { it.id == storyText.id }
                    if (textIndex >= 0) textElements[textIndex] = textElements[textIndex].copy(offset = textElements[textIndex].offset + dragAmount)
                    dragUpdateCounter++
                })
            }

            activeStickers.forEach { sticker ->
                DraggableStickerItem(sticker = sticker, onDrag = { dragAmount ->
                    val stickerIndex = activeStickers.indexOfFirst { it.id == sticker.id }
                    if (stickerIndex >= 0) activeStickers[stickerIndex] = activeStickers[stickerIndex].copy(offset = activeStickers[stickerIndex].offset + dragAmount)
                    dragUpdateCounter++
                })
            }

            if (selectedTrack != null) {
                Row(
                    modifier = Modifier
                        .offset { IntOffset(musicOffset.x.roundToInt(), musicOffset.y.roundToInt()) }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                musicOffset += dragAmount
                                dragUpdateCounter++
                            }
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = selectedTrack!!.coverUrl, contentDescription = null,
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(selectedTrack!!.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                        Text(selectedTrack!!.artist, color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(140.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent))))
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))

        IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 16.dp).size(48.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White, modifier = Modifier.size(32.dp))
        }

        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ToolIcon(icon = Icons.Default.TextFields, description = "Text", onClick = { showTextInput = true })
            ToolIcon(icon = Icons.Default.EmojiEmotions, description = "Stickers", onClick = { showStickerSheet = true })
            ToolIcon(icon = Icons.Default.MusicNote, description = "Music", isActive = selectedTrack != null, onClick = { showMusicSheet = true })
            ToolIcon(icon = Icons.Default.Edit, description = "Draw", isActive = isDrawingMode, onClick = { isDrawingMode = !isDrawingMode })
        }

        if (!showTextInput) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.2f)).clickable(onClick = handleShare).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.Gray)) {
                        if (activeUserAvatarUrl != null) AsyncImage(model = activeUserAvatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSaving) "Guardando..." else "Your Story", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White).clickable(onClick = handleShare),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                    else Icon(Icons.Default.ChevronRight, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(32.dp))
                }
            }
        }

        if (showTextInput) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextField(
                        value = inputText, onValueChange = { inputText = it },
                        placeholder = { Text("Escribe algo...", color = Color.Gray) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color(0xFFFF5722)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) textElements.add(StoryText(id = textIdCounter++, text = inputText))
                            inputText = ""
                            showTextInput = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                    ) { Text("Listo", fontWeight = FontWeight.Bold) }
                }
            }
        }
        if (pendingStickerType == StickerType.MENTION) {
            AlertDialog(
                onDismissRequest = { pendingStickerType = null; stickerInputValue = "" },
                title = { Text("Mención") },
                text = {
                    OutlinedTextField(
                        value = stickerInputValue,
                        onValueChange = { stickerInputValue = it },
                        placeholder = { Text("@usuario") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (stickerInputValue.isNotBlank()) {
                            val finalValue = if (!stickerInputValue.startsWith("@")) "@$stickerInputValue" else stickerInputValue
                            activeStickers.add(StorySticker(id = stickerIdCounter++, type = StickerType.MENTION, value = finalValue))
                        }
                        pendingStickerType = null
                        stickerInputValue = ""
                        showStickerSheet = false
                    }) { Text("Añadir", color = Color(0xFFFF5722)) }
                },
                dismissButton = {
                    TextButton(onClick = { pendingStickerType = null; stickerInputValue = "" }) { Text("Cancelar") }
                }
            )
        }

        if (showStickerSheet && pendingStickerType == null) {
            ModalBottomSheet(
                onDismissRequest = { showStickerSheet = false },
                containerColor = Color(0xFF1E1E1E)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Stickers", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))

                    if (isFetchingLocation) {
                        CircularProgressIndicator(color = Color(0xFFFF5722))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Obteniendo ubicación...", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StickerOptionItem(icon = Icons.Default.Schedule, label = "Hora") {
                                val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                                activeStickers.add(StorySticker(id = stickerIdCounter++, type = StickerType.TIME, value = time))
                                showStickerSheet = false
                            }
                            StickerOptionItem(icon = Icons.Default.LocationOn, label = "Ubicación") {
                                isFetchingLocation = true
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            StickerOptionItem(icon = Icons.Default.AlternateEmail, label = "Mención") {
                                pendingStickerType = StickerType.MENTION
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }

        if (showMusicSheet) {
            MusicBottomSheet(
                viewModel = musicViewModel,
                onTrackSelected = { track -> selectedTrack = track; showMusicSheet = false },
                onDismiss = { showMusicSheet = false }
            )
        }
    }
}

@Composable
fun StickerOptionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.DarkGray), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun DraggableStickerItem(sticker: StorySticker, onDrag: (Offset) -> Unit) {
    Box(
        modifier = Modifier
            .offset { IntOffset(sticker.offset.x.roundToInt(), sticker.offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
    ) {
        when (sticker.type) {
            StickerType.TIME -> {
                Text(
                    text = sticker.value, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp)).padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
            StickerType.LOCATION -> {
                Row(
                    modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9800))), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = sticker.value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            StickerType.MENTION -> {
                Text(
                    text = sticker.value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0))), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DraggableTextItem(text: StoryText, onDrag: (Offset) -> Unit) {
    Text(
        text = text.text, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset { IntOffset(text.offset.x.roundToInt(), text.offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
    )
}
@Composable
fun ToolIcon(icon: ImageVector, description: String, isActive: Boolean = false, onClick: () -> Unit) {
    val backgroundColor = if (isActive) Color.White else Color.Black.copy(alpha = 0.3f)
    val iconColor = if (isActive) Color(0xFFFF5722) else Color.White
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).background(backgroundColor).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Icon(imageVector = icon, contentDescription = description, tint = iconColor, modifier = Modifier.size(24.dp)) }
}

suspend fun createBitmapFromPicture(context: Context, picture: Picture, width: Int, height: Int): Uri? {
    if (width == 0 || height == 0) return null

    return withContext(Dispatchers.IO) {
        var bitmap: Bitmap? = null
        try {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            synchronized(picture) {
                val canvas = android.graphics.Canvas(bitmap!!)
                canvas.drawColor(android.graphics.Color.BLACK.toInt())
                canvas.drawPicture(picture)
            }

            val file = File(context.cacheDir, "story_export_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            bitmap?.recycle()
        }
    }
}