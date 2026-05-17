package com.social.flare.features.feed.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.MusicNote
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

data class DrawnStroke(val points: List<Offset>, val color: Color = Color(0xFFFF5722), val strokeWidth: Float = 12f)
data class StoryText(val id: Int, val text: String, var offset: Offset = Offset(200f, 500f))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryScreen(
    selectedImageUri: Uri?,
    activeUserAvatarUrl: String?,
    onCancel: () -> Unit,
    onShareToStory: (Uri) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isDrawingMode by remember { mutableStateOf(false) }
    var showTextInput by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    var strokes by remember { mutableStateOf(listOf<DrawnStroke>()) }
    var currentPoints by remember { mutableStateOf(listOf<Offset>()) }
    var textElements by remember { mutableStateOf(listOf<StoryText>()) }
    var textIdCounter by remember { mutableStateOf(0) }

    val picture = remember { Picture() }
    var viewWidth by remember { mutableIntStateOf(0) }
    var viewHeight by remember { mutableIntStateOf(0) }

    val handleShare: () -> Unit = {
        if (!isSaving) {
            isSaving = true
            coroutineScope.launch {
                val newUri = createBitmapFromPicture(context, picture, viewWidth, viewHeight)
                if (newUri != null) {
                    onShareToStory(newUri)
                } else {
                    selectedImageUri?.let { onShareToStory(it) }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = this.size.width.toInt()
                    val height = this.size.height.toInt()
                    viewWidth = width
                    viewHeight = height

                    onDrawWithContent {
                        val pictureCanvas = androidx.compose.ui.graphics.Canvas(picture.beginRecording(width, height))

                        draw(this, this.layoutDirection, pictureCanvas, this.size) {
                            this@onDrawWithContent.drawContent()
                        }
                        picture.endRecording()

                        drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
                    }
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(selectedImageUri)
                    .allowHardware(false)
                    .build(),
                contentDescription = "Selected Story Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 90.dp, bottom = 100.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isDrawingMode) {
                        if (isDrawingMode) {
                            detectDragGestures(
                                onDragStart = { offset -> currentPoints = listOf(offset) },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPoints = currentPoints + change.position
                                },
                                onDragEnd = {
                                    if (currentPoints.isNotEmpty()) {
                                        strokes = strokes + DrawnStroke(currentPoints)
                                        currentPoints = emptyList()
                                    }
                                }
                            )
                        }
                    }
            ) {
                strokes.forEach { stroke ->
                    val path = Path().apply {
                        if (stroke.points.isNotEmpty()) {
                            moveTo(stroke.points.first().x, stroke.points.first().y)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                        }
                    }
                    drawPath(path = path, color = stroke.color, style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                if (currentPoints.isNotEmpty()) {
                    val path = Path().apply {
                        moveTo(currentPoints.first().x, currentPoints.first().y)
                        for (i in 1 until currentPoints.size) {
                            lineTo(currentPoints[i].x, currentPoints[i].y)
                        }
                    }
                    drawPath(path = path, color = Color(0xFFFF5722), style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            textElements.forEach { storyText ->
                DraggableTextItem(
                    text = storyText.text,
                    initialOffset = storyText.offset,
                    onOffsetChange = { newOffset ->
                        textElements = textElements.map { if (it.id == storyText.id) it.copy(offset = newOffset) else it }
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxWidth().height(140.dp).align(Alignment.TopCenter).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent))))
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).align(Alignment.BottomCenter).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))

        IconButton(onClick = onCancel, modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 16.dp).size(48.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White, modifier = Modifier.size(32.dp))
        }

        if (textElements.isEmpty() && !isDrawingMode && !showTextInput) {
            Box(modifier = Modifier.align(Alignment.Center).clickable { showTextInput = true }.padding(32.dp)) {
                Text("Tap to add text...", color = Color.White.copy(alpha = 0.8f), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ToolIcon(icon = Icons.Default.TextFields, description = "Text", onClick = { showTextInput = true })
            ToolIcon(icon = Icons.Default.EmojiEmotions, description = "Stickers", onClick = {})
            ToolIcon(icon = Icons.Default.MusicNote, description = "Music", onClick = {})
            ToolIcon(icon = Icons.Default.Edit, description = "Draw", isActive = isDrawingMode, onClick = { isDrawingMode = !isDrawingMode })
        }

        if (!showTextInput) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable(onClick = handleShare) // Llamamos al handler
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.Gray)) {
                        if (activeUserAvatarUrl != null) AsyncImage(model = activeUserAvatarUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSaving) "Guardando..." else "Your Story", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable(onClick = handleShare),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
        if (showTextInput) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
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
                            if (inputText.isNotBlank()) textElements = textElements + StoryText(id = textIdCounter++, text = inputText)
                            inputText = ""
                            showTextInput = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
                    ) { Text("Listo", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}
suspend fun createBitmapFromPicture(context: Context, picture: Picture, width: Int, height: Int): Uri? {
    if (width == 0 || height == 0) return null

    return withContext(Dispatchers.IO) {
        try {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.BLACK.toInt())
            canvas.drawPicture(picture)

            val file = File(context.cacheDir, "story_export_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


@Composable
fun DraggableTextItem(text: String, initialOffset: Offset, onOffsetChange: (Offset) -> Unit) {
    var offset by remember { mutableStateOf(initialOffset) }
    Text(
        text = text, color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offset += dragAmount
                    onOffsetChange(offset)
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