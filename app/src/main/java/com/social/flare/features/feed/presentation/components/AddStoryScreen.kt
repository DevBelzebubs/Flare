package com.social.flare.features.feed.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Picture
import android.graphics.Rect
import android.graphics.RectF
import android.location.Geocoder
import android.os.Build
import android.net.Uri
import com.google.android.gms.location.Priority
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Undo
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
import kotlin.math.min
import kotlin.math.roundToInt

data class DrawnStroke(val points: List<Offset>, val color: Color = Color(0xFFFF5722), val strokeWidth: Float = 12f)
data class StoryText(
    val id: Int,
    val text: String,
    val offset: Offset = Offset(200f, 500f),
    val fontSize: Float = 36f,
    val color: Color = Color.White,
    val isBold: Boolean = true
)

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
    onShareToStory: (Uri, musicUrl: String?, musicTitle: String?, musicArtist: String?, musicCoverUrl: String?) -> Unit
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
    var editingTextId by remember { mutableStateOf<Int?>(null) }
    var currentFontSize by remember { mutableFloatStateOf(36f) }
    var currentTextColor by remember { mutableStateOf(Color.White) }
    var isBold by remember { mutableStateOf(true) }
    var selectedColor by remember { mutableStateOf(Color(0xFFFF5722)) }

    val picture = remember { Picture() }
    DisposableEffect(picture) {
        onDispose {
            try {
                val closeMethod = Picture::class.java.getMethod("close")
                closeMethod.invoke(picture)
            } catch (_: Exception) {}
        }
    }
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
                val hasOverlays = strokes.isNotEmpty() || textElements.isNotEmpty() || activeStickers.isNotEmpty() || selectedTrack != null
                val uri = if (hasOverlays) {
                    renderOverlaysToBitmap(
                        context = context,
                        imageUri = selectedImageUri ?: return@launch,
                        width = viewWidth, height = viewHeight,
                        strokes = strokes,
                        textElements = textElements.toList(),
                        stickers = activeStickers.toList(),
                        selectedTrack = selectedTrack,
                        musicOffsetX = musicOffset.x, musicOffsetY = musicOffset.y
                    ) ?: selectedImageUri
                } else {
                    selectedImageUri
                }
                if (uri != null) {
                    onShareToStory(uri, selectedTrack?.previewUrl, selectedTrack?.title, selectedTrack?.artist, selectedTrack?.coverUrl)
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
                                strokes = strokes + DrawnStroke(currentPoints, color = selectedColor)
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
                    drawPath(path = path, color = selectedColor, style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            textElements.forEach { storyText ->
                DraggableTextItem(
                    text = storyText,
                    onDrag = { dragAmount ->
                        val textIndex = textElements.indexOfFirst { it.id == storyText.id }
                        if (textIndex >= 0) textElements[textIndex] = textElements[textIndex].copy(offset = textElements[textIndex].offset + dragAmount)
                        dragUpdateCounter++
                    },
                    onTap = {
                        editingTextId = storyText.id
                        inputText = storyText.text
                        currentFontSize = storyText.fontSize
                        currentTextColor = storyText.color
                        isBold = storyText.isBold
                        showTextInput = true
                    }
                )
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
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ToolIcon(icon = Icons.Default.TextFields, description = "Texto", onClick = { showTextInput = true })
            ToolIcon(icon = Icons.Default.EmojiEmotions, description = "Stickers", onClick = { showStickerSheet = true })
            ToolIcon(icon = Icons.Default.MusicNote, description = "Música", isActive = selectedTrack != null, onClick = { showMusicSheet = true })
            ToolIcon(icon = Icons.Default.Edit, description = "Dibujar", isActive = isDrawingMode, onClick = { isDrawingMode = !isDrawingMode })
            if (isDrawingMode && strokes.isNotEmpty()) {
                ToolIcon(icon = Icons.AutoMirrored.Filled.Undo, description = "Deshacer", onClick = { strokes = strokes.dropLast(1) })
            }
        }

        if (isDrawingMode) {
            val drawColors = listOf(
                Color(0xFFFF5722), Color.White, Color.Red, Color.Blue,
                Color.Green, Color.Yellow, Color(0xFFFF69B4), Color.Black
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                drawColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(if (color == selectedColor) 32.dp else 28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (color == selectedColor) 3.dp else 1.dp,
                                color = if (color == selectedColor) Color.White else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }
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
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = currentFontSize.sp,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            color = currentTextColor,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        val textColors = listOf(Color.White, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color(0xFFFF69B4), Color(0xFFFF5722), Color.Black)
                        textColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(if (color == currentTextColor) 28.dp else 24.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(2.dp, if (color == currentTextColor) Color.White else Color.Transparent, CircleShape)
                                    .clickable { currentTextColor = color }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("${currentFontSize.toInt()}sp", color = Color.White, fontSize = 13.sp)
                    Slider(
                        value = currentFontSize,
                        onValueChange = { currentFontSize = it },
                        valueRange = 16f..72f,
                        modifier = Modifier.width(200.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF5722),
                            activeTrackColor = Color(0xFFFF5722)
                        )
                    )
                    Text(
                        text = "Negrita: ${if (isBold) "Sí" else "No"}",
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { isBold = !isBold }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val editingId = editingTextId
                                if (editingId != null) {
                                    val idx = textElements.indexOfFirst { it.id == editingId }
                                    if (idx >= 0) textElements[idx] = textElements[idx].copy(
                                        text = inputText,
                                        fontSize = currentFontSize,
                                        color = currentTextColor,
                                        isBold = isBold
                                    )
                                } else {
                                    textElements.add(StoryText(
                                        id = textIdCounter++, text = inputText,
                                        fontSize = currentFontSize, color = currentTextColor, isBold = isBold
                                    ))
                                }
                            }
                            inputText = ""
                            showTextInput = false
                            editingTextId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                    ) { Text(if (editingTextId != null) "Guardar" else "Listo", fontWeight = FontWeight.Bold) }
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
fun DraggableTextItem(text: StoryText, onDrag: (Offset) -> Unit, onTap: () -> Unit) {
    Text(
        text = text.text, color = text.color, fontSize = text.fontSize.sp,
        fontWeight = if (text.isBold) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .offset { IntOffset(text.offset.x.roundToInt(), text.offset.y.roundToInt()) }
            .clickable { onTap() }
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

private suspend fun renderOverlaysToBitmap(
    context: Context,
    imageUri: Uri,
    width: Int,
    height: Int,
    strokes: List<DrawnStroke>,
    textElements: List<StoryText>,
    stickers: List<StorySticker>,
    selectedTrack: FlareTrack?,
    musicOffsetX: Float,
    musicOffsetY: Float
): Uri? {
    if (width == 0 || height == 0) return null

    return withContext(Dispatchers.IO) {
        var sourceBitmap: Bitmap? = null
        var result: Bitmap? = null
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            sourceBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (sourceBitmap == null) return@withContext null

            val density = context.resources.displayMetrics.density
            val paddingTop = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90f, context.resources.displayMetrics).toInt()
            val paddingBottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, context.resources.displayMetrics).toInt()

            result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            canvas.drawColor(android.graphics.Color.BLACK)

            val drawAreaH = height - paddingTop - paddingBottom
            val scale = min(width.toFloat() / sourceBitmap.width, drawAreaH.toFloat() / sourceBitmap.height)
            val scaledW = (sourceBitmap.width * scale).toInt()
            val scaledH = (sourceBitmap.height * scale).toInt()
            val left = (width - scaledW) / 2f
            val top = paddingTop + (drawAreaH - scaledH) / 2f
            val srcRect = Rect(0, 0, sourceBitmap.width, sourceBitmap.height)
            val dstRect = RectF(left, top, left + scaledW, top + scaledH)
            canvas.drawBitmap(sourceBitmap, srcRect, dstRect, null)

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            for (stroke in strokes) {
                if (stroke.points.size < 2) continue
                strokePaint.color = Color(
                    red = (stroke.color.red * 255).toInt(),
                    green = (stroke.color.green * 255).toInt(),
                    blue = (stroke.color.blue * 255).toInt(),
                    alpha = (stroke.color.alpha * 255).toInt()
                ).hashCode()
                strokePaint.strokeWidth = stroke.strokeWidth
                val aPath = AndroidPath().apply {
                    moveTo(stroke.points.first().x, stroke.points.first().y)
                    for (i in 1 until stroke.points.size) {
                        lineTo(stroke.points[i].x, stroke.points[i].y)
                    }
                }
                canvas.drawPath(aPath, strokePaint)
            }

            for (textElement in textElements) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color(
                        red = (textElement.color.red * 255).toInt(),
                        green = (textElement.color.green * 255).toInt(),
                        blue = (textElement.color.blue * 255).toInt(),
                        alpha = (textElement.color.alpha * 255).toInt()
                    ).hashCode()
                    textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textElement.fontSize, context.resources.displayMetrics)
                    isFakeBoldText = textElement.isBold
                }
                canvas.drawText(textElement.text, textElement.offset.x, textElement.offset.y + textPaint.textSize, textPaint)
            }

            val stickerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                isFakeBoldText = true
            }
            val roundRect = RectF()
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            for (sticker in stickers) {
                when (sticker.type) {
                    StickerType.TIME -> {
                        stickerTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 48f, context.resources.displayMetrics)
                        val textW = stickerTextPaint.measureText(sticker.value)
                        val textH = stickerTextPaint.textSize
                        val padH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, context.resources.displayMetrics).toInt()
                        val padV = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                        roundRect.set(
                            sticker.offset.x, sticker.offset.y,
                            sticker.offset.x + textW + padH * 2,
                            sticker.offset.y + textH + padV * 2
                        )
                        bgPaint.color = android.graphics.Color.argb(128, 0, 0, 0)
                        canvas.drawRoundRect(roundRect, 16f * density, 16f * density, bgPaint)
                        canvas.drawText(sticker.value, sticker.offset.x + padH, sticker.offset.y + textH + padV.toFloat(), stickerTextPaint)
                    }
                    StickerType.LOCATION -> {
                        stickerTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, context.resources.displayMetrics)
                        val textW = stickerTextPaint.measureText(sticker.value)
                        val textH = stickerTextPaint.textSize
                        val padH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, context.resources.displayMetrics).toInt()
                        val padV = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                        roundRect.set(
                            sticker.offset.x, sticker.offset.y,
                            sticker.offset.x + textW + padH * 2 + 20f * density,
                            sticker.offset.y + textH + padV * 2
                        )
                        bgPaint.color = android.graphics.Color.rgb(255, 87, 34)
                        canvas.drawRoundRect(roundRect, 8f * density, 8f * density, bgPaint)
                        canvas.drawText(sticker.value, sticker.offset.x + padH + 20f * density, sticker.offset.y + textH + padV.toFloat(), stickerTextPaint)
                    }
                    StickerType.MENTION -> {
                        stickerTextPaint.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 24f, context.resources.displayMetrics)
                        val textW = stickerTextPaint.measureText(sticker.value)
                        val textH = stickerTextPaint.textSize
                        val padH = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, context.resources.displayMetrics).toInt()
                        val padV = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, context.resources.displayMetrics).toInt()
                        roundRect.set(
                            sticker.offset.x, sticker.offset.y,
                            sticker.offset.x + textW + padH * 2,
                            sticker.offset.y + textH + padV * 2
                        )
                        bgPaint.color = android.graphics.Color.rgb(156, 39, 176)
                        canvas.drawRoundRect(roundRect, 8f * density, 8f * density, bgPaint)
                        canvas.drawText(sticker.value, sticker.offset.x + padH, sticker.offset.y + textH + padV.toFloat(), stickerTextPaint)
                    }
                }
            }

            if (selectedTrack != null) {
                val musicCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.argb(153, 0, 0, 0)
                }
                val cardTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    isFakeBoldText = true
                    textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics)
                }
                val cardArtistPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.rgb(211, 211, 211)
                    textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, context.resources.displayMetrics)
                }
                val cardPad = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics).toInt()
                val coverSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40f, context.resources.displayMetrics).toInt()
                val titleW = cardTitlePaint.measureText(selectedTrack.title)
                val artistW = cardArtistPaint.measureText(selectedTrack.artist)
                val textMaxW = maxOf(titleW, artistW)
                val cardW = coverSize + cardPad * 2 + textMaxW.toInt() + cardPad
                val cardH = coverSize + cardPad * 2
                val cardX = musicOffsetX
                val cardY = musicOffsetY

                roundRect.set(cardX, cardY, cardX + cardW, cardY + cardH)
                canvas.drawRoundRect(roundRect, 12f * density, 12f * density, musicCardPaint)

                try {
                    val coverStream = java.net.URL(selectedTrack.coverUrl).openStream()
                    val coverBitmap = BitmapFactory.decodeStream(coverStream)
                    coverStream.close()
                    if (coverBitmap != null) {
                        canvas.drawBitmap(coverBitmap,
                            Rect(0, 0, coverBitmap.width, coverBitmap.height),
                            RectF(cardX + cardPad, cardY + cardPad, cardX + cardPad + coverSize, cardY + cardPad + coverSize),
                            null)
                        coverBitmap.recycle()
                    }
                } catch (_: Exception) {
                    bgPaint.color = android.graphics.Color.GRAY
                    canvas.drawRoundRect(
                        cardX + cardPad, cardY + cardPad,
                        cardX + cardPad + coverSize, cardY + cardPad + coverSize,
                        6f * density, 6f * density, bgPaint
                    )
                }

                canvas.drawText(selectedTrack.title, cardX + cardPad + coverSize + cardPad, cardY + cardPad + cardTitlePaint.textSize, cardTitlePaint)
                canvas.drawText(selectedTrack.artist, cardX + cardPad + coverSize + cardPad, cardY + cardPad + cardTitlePaint.textSize + cardPad.toFloat() + cardArtistPaint.textSize, cardArtistPaint)
            }

            val file = File(context.cacheDir, "story_export_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                result.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            sourceBitmap?.recycle()
            result?.recycle()
        }
    }
}