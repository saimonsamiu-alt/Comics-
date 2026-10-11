package com.example.ui.studio

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import kotlin.math.roundToInt

@Composable
fun ComicPanelCanvas(
    modifier: Modifier = Modifier,
    capturedBitmap: Bitmap?,
    sketchType: String,
    colorStyle: String,
    speechBubbles: List<SpeechBubbleItem>,
    stickers: List<StickerItem>,
    soundEffects: List<SoundEffectItem>,
    onBubblePositionChanged: (bubbleId: String, newXRatio: Float, newYRatio: Float) -> Unit,
    onStickerPositionChanged: (stickerId: String, newXRatio: Float, newYRatio: Float) -> Unit,
    onSfxPositionChanged: (sfxId: String, newXRatio: Float, newYRatio: Float) -> Unit,
    onBubbleDeleted: (bubbleId: String) -> Unit,
    onStickerDeleted: (stickerId: String) -> Unit,
    onSfxDeleted: (sfxId: String) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
            .background(Color.White)
            .testTag("comic_panel_canvas")
    ) {
        val canvasWidthPx = constraints.maxWidth.toFloat()
        val canvasHeightPx = constraints.maxHeight.toFloat()

        // 1. BASE BACKGROUND: Captured CameraX sketch or template art
        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Hand-Drawn Sketch (CameraX)",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Default stylized sketchbook drawing placeholder
            DefaultSketchArtBackground(sketchType = sketchType, colorStyle = colorStyle)
        }

        // 2. SOUND EFFECTS (SFX)
        soundEffects.forEach { sfx ->
            val offsetX = (sfx.xRatio * canvasWidthPx).roundToInt()
            val offsetY = (sfx.yRatio * canvasHeightPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .pointerInput(sfx.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val nextX = ((offsetX + dragAmount.x) / canvasWidthPx).coerceIn(0.05f, 0.95f)
                            val nextY = ((offsetY + dragAmount.y) / canvasHeightPx).coerceIn(0.05f, 0.95f)
                            onSfxPositionChanged(sfx.id, nextX, nextY)
                        }
                    }
                    .rotate(sfx.rotation)
                    .scale(sfx.scale)
            ) {
                Text(
                    text = sfx.text,
                    color = try { Color(android.graphics.Color.parseColor(sfx.colorHex)) } catch (_: Exception) { Color.Red },
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // 3. STICKERS
        stickers.forEach { sticker ->
            val offsetX = (sticker.xRatio * canvasWidthPx).roundToInt()
            val offsetY = (sticker.yRatio * canvasHeightPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .pointerInput(sticker.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val nextX = ((offsetX + dragAmount.x) / canvasWidthPx).coerceIn(0.05f, 0.95f)
                            val nextY = ((offsetY + dragAmount.y) / canvasHeightPx).coerceIn(0.05f, 0.95f)
                            onStickerPositionChanged(sticker.id, nextX, nextY)
                        }
                    }
                    .rotate(sticker.rotation)
                    .scale(sticker.scale)
            ) {
                Text(
                    text = sticker.emojiOrIcon,
                    fontSize = 38.sp
                )
            }
        }

        // 4. SPEECH BUBBLES
        speechBubbles.forEach { bubble ->
            val offsetX = (bubble.xRatio * canvasWidthPx).roundToInt()
            val offsetY = (bubble.yRatio * canvasHeightPx).roundToInt()

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .pointerInput(bubble.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val nextX = ((offsetX + dragAmount.x) / canvasWidthPx).coerceIn(0.05f, 0.95f)
                            val nextY = ((offsetY + dragAmount.y) / canvasHeightPx).coerceIn(0.05f, 0.95f)
                            onBubblePositionChanged(bubble.id, nextX, nextY)
                        }
                    }
                    .scale(bubble.scale)
            ) {
                Surface(
                    color = Color.White,
                    shape = when (bubble.type) {
                        "Shout" -> RoundedCornerShape(4.dp)
                        "Thought" -> CircleShape
                        else -> RoundedCornerShape(16.dp)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (bubble.type == "Shout") 3.dp else 2.dp,
                        color = Color.Black
                    ),
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = bubble.text,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = if (bubble.type == "Shout") FontWeight.Black else FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultSketchArtBackground(sketchType: String, colorStyle: String) {
    val bgTint = when (colorStyle) {
        "Vibrant Webtoon" -> Color(0xFFFFF9C4)
        "Vintage Newsprint" -> Color(0xFFEFEBE9)
        "Neon Noir" -> Color(0xFF263238)
        else -> Color(0xFFFFFFFF)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgTint),
        contentAlignment = Alignment.Center
    ) {
        // Grid notebook paper lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 40.dp.toPx()
            var y = step
            while (y < size.height) {
                drawLine(
                    color = Color(0xFFB0BEC5).copy(alpha = 0.35f),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += step
            }
        }

        Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✏️ Hand-Drawn Sketch: $sketchType\nTap 'Snap Sketch (CameraX)' to photograph your drawing",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
