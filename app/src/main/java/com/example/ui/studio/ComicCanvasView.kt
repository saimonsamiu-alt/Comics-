package com.example.ui.studio

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun ComicPanelCanvas(
    modifier: Modifier = Modifier,
    sketchType: String = "dog", // dog, hero, dragon
    colorStyle: String = "Vibrant Cel-Shaded",
    characterColor: Color = Color(0xFFFDCB6E),
    auraType: String = "Sparkle", // None, Flame, Sparkle, Lightning
    speechBubbles: List<SpeechBubbleItem> = emptyList(),
    soundEffects: List<SoundEffectItem> = emptyList(),
    isAnimating: Boolean = false,
    animationMode: String = "Camera Pan & Zoom", // "Camera Pan & Zoom", "Action Shake", "Pulse Life"
    customBitmap: android.graphics.Bitmap? = null,
    inkingEdgeContrast: Float = 0.85f,
    isCleanPaperEnabled: Boolean = true,
    onBubblePositionChanged: ((String, Float, Float) -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "comic_anim")

    // Dynamic animation factors
    val zoomScale by infiniteTransition.animateFloat(
        initialValue = if (isAnimating && animationMode == "Camera Pan & Zoom") 1.0f else 1.0f,
        targetValue = if (isAnimating && animationMode == "Camera Pan & Zoom") 1.15f else if (isAnimating && animationMode == "Pulse Life") 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zoom"
    )

    val shakeOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(80, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val auraPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura"
    )

    val activeShakeX = if (isAnimating && animationMode == "Action Shake") shakeOffset else 0f
    val activeShakeY = if (isAnimating && animationMode == "Action Shake") -shakeOffset * 0.7f else 0f

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
            .background(Color.White)
            .graphicsLayer {
                scaleX = zoomScale
                scaleY = zoomScale
                translationX = activeShakeX
                translationY = activeShakeY
            }
            .testTag("comic_panel_canvas")
    ) {
        // Base Character & Comic Art Drawn on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawComicBackground(colorStyle)
            drawAuraEffects(auraType, auraPulse, size)

            if (customBitmap != null) {
                val imageBitmap = customBitmap.asImageBitmap()
                val targetWidth = size.width * 0.82f
                val targetHeight = (targetWidth / imageBitmap.width.toFloat()) * imageBitmap.height.toFloat()
                val topX = (size.width - targetWidth) / 2f
                val topY = (size.height - targetHeight) / 2f

                // Clean paper background matting if enabled
                if (isCleanPaperEnabled) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.92f),
                        topLeft = Offset(topX - 6f, topY - 6f),
                        size = Size(targetWidth + 12f, targetHeight.coerceAtMost(size.height * 0.85f) + 12f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                }

                val colorFilter = when (colorStyle) {
                    "Cyberpunk Glow" -> androidx.compose.ui.graphics.ColorFilter.tint(
                        Color(0xFF00CEC9).copy(alpha = 0.3f),
                        androidx.compose.ui.graphics.BlendMode.ColorBurn
                    )
                    "Vintage Retro Comic" -> androidx.compose.ui.graphics.ColorFilter.tint(
                        Color(0xFF8D6E63).copy(alpha = 0.2f),
                        androidx.compose.ui.graphics.BlendMode.Darken
                    )
                    "Pastel Fantasy" -> androidx.compose.ui.graphics.ColorFilter.tint(
                        Color(0xFFA29BFE).copy(alpha = 0.25f),
                        androidx.compose.ui.graphics.BlendMode.ColorBurn
                    )
                    else -> null
                }

                drawImage(
                    image = imageBitmap,
                    dstOffset = IntOffset(topX.roundToInt(), topY.roundToInt()),
                    dstSize = IntSize(targetWidth.roundToInt(), targetHeight.coerceAtMost(size.height * 0.85f).roundToInt()),
                    alpha = inkingEdgeContrast.coerceIn(0.4f, 1.0f),
                    colorFilter = colorFilter
                )

                // Comic ink frame border
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.85f),
                    topLeft = Offset(topX, topY),
                    size = Size(targetWidth, targetHeight.coerceAtMost(size.height * 0.85f)),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2.5f)
                )
            } else {
                when (sketchType.lowercase()) {
                    "hero" -> drawAnimeHero(characterColor, colorStyle, size)
                    "dragon" -> drawFantasyDragon(characterColor, colorStyle, size)
                    else -> drawNotebookDoggo(characterColor, colorStyle, size)
                }
            }

            drawComicHalftoneBorder(size)
        }

        // Speech Bubbles Overlay
        speechBubbles.forEach { bubble ->
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            (bubble.xRatio * 700).roundToInt(),
                            (bubble.yRatio * 800).roundToInt()
                        )
                    }
                    .pointerInput(bubble.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val newX = ((bubble.xRatio * 700 + dragAmount.x) / 700f).coerceIn(0.05f, 0.85f)
                            val newY = ((bubble.yRatio * 800 + dragAmount.y) / 800f).coerceIn(0.05f, 0.85f)
                            onBubblePositionChanged?.invoke(bubble.id, newX, newY)
                        }
                    }
            ) {
                ComicSpeechBubbleView(bubble = bubble)
            }
        }

        // Sound Effects (POW, BAM, WHOOSH)
        soundEffects.forEach { sfx ->
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            (sfx.xRatio * 700).roundToInt(),
                            (sfx.yRatio * 800).roundToInt()
                        )
                    }
            ) {
                ComicSoundEffectView(sfx = sfx, isAnimated = isAnimating)
            }
        }
    }
}

private fun DrawScope.drawComicBackground(style: String) {
    val bgBrush = when (style) {
        "Cyberpunk Glow" -> Brush.radialGradient(
            colors = listOf(Color(0xFF2D1B69), Color(0xFF110726), Color(0xFF070414)),
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.width
        )
        "Vintage Retro Comic" -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFF9E6), Color(0xFFF5E6CA))
        )
        "Pastel Fantasy" -> Brush.linearGradient(
            colors = listOf(Color(0xFFE0C3FC), Color(0xFF8EC5FC))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFDF8), Color(0xFFEFF5FF), Color(0xFFFFF0F5))
        )
    }
    drawRect(brush = bgBrush, size = size)

    // Draw comic dynamic speedlines or notebook grid if retro
    if (style == "Vintage Retro Comic") {
        // Draw light notebook grid lines
        val lineSpacing = 36f
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = Color(0x336C5CE7),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.2f
            )
            y += lineSpacing
        }
    } else {
        // Dynamic radial action rays
        val center = Offset(size.width * 0.5f, size.height * 0.5f)
        val rayColor = Color(0x15000000)
        for (i in 0 until 18) {
            val angle = (i * 20.0) * (Math.PI / 180.0)
            val end = Offset(
                center.x + (size.width * 1.2f * cos(angle)).toFloat(),
                center.y + (size.height * 1.2f * sin(angle)).toFloat()
            )
            drawLine(
                color = rayColor,
                start = center,
                end = end,
                strokeWidth = 4f
            )
        }
    }
}

private fun DrawScope.drawAuraEffects(type: String, pulse: Float, size: Size) {
    val center = Offset(size.width * 0.5f, size.height * 0.55f)
    when (type) {
        "Flame" -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99FF5722), Color(0x44FFC107), Color.Transparent),
                    center = center,
                    radius = (size.width * 0.42f) * pulse
                ),
                radius = (size.width * 0.42f) * pulse,
                center = center
            )
        }
        "Lightning" -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x8800CEC9), Color(0x336C5CE7), Color.Transparent),
                    center = center,
                    radius = (size.width * 0.45f) * pulse
                ),
                radius = (size.width * 0.45f) * pulse,
                center = center
            )
        }
        "Sparkle" -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x88FDCB6E), Color(0x22FD79A8), Color.Transparent),
                    center = center,
                    radius = (size.width * 0.4f) * pulse
                ),
                radius = (size.width * 0.4f) * pulse,
                center = center
            )
        }
    }
}

// 1. Drawing the Pencil Notebook Doggo transformed into anime comic hero
private fun DrawScope.drawNotebookDoggo(bodyColor: Color, style: String, size: Size) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.58f

    val outlineColor = if (style == "Cyberpunk Glow") Color(0xFF00CEC9) else Color(0xFF1A1A1A)
    val strokeW = 6f

    // Dog Body
    drawOval(
        color = bodyColor,
        topLeft = Offset(cx - size.width * 0.22f, cy - size.height * 0.15f),
        size = Size(size.width * 0.44f, size.height * 0.36f)
    )
    drawOval(
        color = outlineColor,
        topLeft = Offset(cx - size.width * 0.22f, cy - size.height * 0.15f),
        size = Size(size.width * 0.44f, size.height * 0.36f),
        style = Stroke(width = strokeW)
    )

    // Dog Head
    val headRadius = size.width * 0.24f
    drawCircle(
        color = bodyColor,
        radius = headRadius,
        center = Offset(cx, cy - size.height * 0.22f)
    )
    drawCircle(
        color = outlineColor,
        radius = headRadius,
        center = Offset(cx, cy - size.height * 0.22f),
        style = Stroke(width = strokeW)
    )

    // Floppy Comic Ears
    val leftEar = Path().apply {
        moveTo(cx - headRadius * 0.7f, cy - size.height * 0.32f)
        cubicTo(
            cx - headRadius * 1.5f, cy - size.height * 0.35f,
            cx - headRadius * 1.4f, cy - size.height * 0.12f,
            cx - headRadius * 0.8f, cy - size.height * 0.16f
        )
        close()
    }
    drawPath(path = leftEar, color = bodyColor)
    drawPath(path = leftEar, color = outlineColor, style = Stroke(width = strokeW))

    val rightEar = Path().apply {
        moveTo(cx + headRadius * 0.7f, cy - size.height * 0.32f)
        cubicTo(
            cx + headRadius * 1.5f, cy - size.height * 0.35f,
            cx + headRadius * 1.4f, cy - size.height * 0.12f,
            cx + headRadius * 0.8f, cy - size.height * 0.16f
        )
        close()
    }
    drawPath(path = rightEar, color = bodyColor)
    drawPath(path = rightEar, color = outlineColor, style = Stroke(width = strokeW))

    // Detective Comic Hat
    val hatPath = Path().apply {
        moveTo(cx - headRadius * 0.9f, cy - size.height * 0.34f)
        lineTo(cx + headRadius * 0.9f, cy - size.height * 0.34f)
        lineTo(cx + headRadius * 0.5f, cy - size.height * 0.44f)
        lineTo(cx - headRadius * 0.5f, cy - size.height * 0.44f)
        close()
    }
    drawPath(path = hatPath, color = Color(0xFF6C5CE7))
    drawPath(path = hatPath, color = outlineColor, style = Stroke(width = strokeW))

    // Big Anime Eyes
    val eyeY = cy - size.height * 0.24f
    val eyeRadius = size.width * 0.045f
    drawCircle(color = Color.Black, radius = eyeRadius, center = Offset(cx - size.width * 0.08f, eyeY))
    drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(cx - size.width * 0.09f, eyeY - 4f))

    drawCircle(color = Color.Black, radius = eyeRadius, center = Offset(cx + size.width * 0.08f, eyeY))
    drawCircle(color = Color.White, radius = eyeRadius * 0.4f, center = Offset(cx + size.width * 0.07f, eyeY - 4f))

    // Snout and Nose
    drawOval(
        color = Color(0xFF2D3436),
        topLeft = Offset(cx - 20f, cy - size.height * 0.18f),
        size = Size(40f, 26f)
    )

    // Comic Detective Coat Collar
    drawRoundRect(
        color = Color(0xFFFF5722),
        topLeft = Offset(cx - size.width * 0.16f, cy - size.height * 0.06f),
        size = Size(size.width * 0.32f, 26f),
        cornerRadius = CornerRadius(12f, 12f)
    )
}

// 2. Anime Hero Character
private fun DrawScope.drawAnimeHero(hairColor: Color, style: String, size: Size) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.55f
    val outline = if (style == "Cyberpunk Glow") Color(0xFFFD79A8) else Color(0xFF14141E)
    val strokeW = 6f

    // Hero Cape
    val cape = Path().apply {
        moveTo(cx - 60f, cy - 80f)
        lineTo(cx - size.width * 0.4f, cy + size.height * 0.35f)
        quadraticTo(cx, cy + size.height * 0.38f, cx + size.width * 0.4f, cy + size.height * 0.35f)
        lineTo(cx + 60f, cy - 80f)
        close()
    }
    drawPath(path = cape, color = Color(0xFFFF5722))
    drawPath(path = cape, color = outline, style = Stroke(width = strokeW))

    // Torso / Suit
    drawRoundRect(
        color = Color(0xFF1E272E),
        topLeft = Offset(cx - size.width * 0.18f, cy - 70f),
        size = Size(size.width * 0.36f, size.height * 0.35f),
        cornerRadius = CornerRadius(20f, 20f)
    )

    // Hero Head
    drawCircle(
        color = Color(0xFFFFDFC4),
        radius = size.width * 0.19f,
        center = Offset(cx, cy - size.height * 0.24f)
    )
    drawCircle(
        color = outline,
        radius = size.width * 0.19f,
        center = Offset(cx, cy - size.height * 0.24f),
        style = Stroke(width = strokeW)
    )

    // Spiky Anime Hair
    val hair = Path().apply {
        moveTo(cx - size.width * 0.22f, cy - size.height * 0.28f)
        lineTo(cx - size.width * 0.32f, cy - size.height * 0.38f)
        lineTo(cx - size.width * 0.14f, cy - size.height * 0.44f)
        lineTo(cx, cy - size.height * 0.52f)
        lineTo(cx + size.width * 0.14f, cy - size.height * 0.44f)
        lineTo(cx + size.width * 0.32f, cy - size.height * 0.38f)
        lineTo(cx + size.width * 0.22f, cy - size.height * 0.28f)
        close()
    }
    drawPath(path = hair, color = hairColor)
    drawPath(path = hair, color = outline, style = Stroke(width = strokeW))

    // Sharp Hero Eyes
    val eyeY = cy - size.height * 0.23f
    drawOval(color = Color.Black, topLeft = Offset(cx - 70f, eyeY), size = Size(35f, 18f))
    drawCircle(color = Color.Cyan, radius = 6f, center = Offset(cx - 52f, eyeY + 9f))

    drawOval(color = Color.Black, topLeft = Offset(cx + 35f, eyeY), size = Size(35f, 18f))
    drawCircle(color = Color.Cyan, radius = 6f, center = Offset(cx + 52f, eyeY + 9f))
}

// 3. Fantasy Creature / Dragon
private fun DrawScope.drawFantasyDragon(dragonColor: Color, style: String, size: Size) {
    val cx = size.width * 0.5f
    val cy = size.height * 0.55f
    val outline = Color(0xFF1A1A24)
    val strokeW = 6f

    // Dragon Wings
    val leftWing = Path().apply {
        moveTo(cx - 40f, cy - 20f)
        cubicTo(
            cx - size.width * 0.48f, cy - size.height * 0.25f,
            cx - size.width * 0.45f, cy + size.height * 0.15f,
            cx - 30f, cy + 60f
        )
        close()
    }
    drawPath(leftWing, Color(0xFF6C5CE7))
    drawPath(leftWing, outline, style = Stroke(strokeW))

    val rightWing = Path().apply {
        moveTo(cx + 40f, cy - 20f)
        cubicTo(
            cx + size.width * 0.48f, cy - size.height * 0.25f,
            cx + size.width * 0.45f, cy + size.height * 0.15f,
            cx + 30f, cy + 60f
        )
        close()
    }
    drawPath(rightWing, Color(0xFF6C5CE7))
    drawPath(rightWing, outline, style = Stroke(strokeW))

    // Dragon Body & Head
    drawOval(
        color = dragonColor,
        topLeft = Offset(cx - size.width * 0.2f, cy - size.height * 0.18f),
        size = Size(size.width * 0.4f, size.height * 0.42f)
    )
    drawOval(
        color = outline,
        topLeft = Offset(cx - size.width * 0.2f, cy - size.height * 0.18f),
        size = Size(size.width * 0.4f, size.height * 0.42f),
        style = Stroke(strokeW)
    )

    // Dragon Horns
    drawPath(
        Path().apply {
            moveTo(cx - 35f, cy - size.height * 0.18f)
            lineTo(cx - 70f, cy - size.height * 0.35f)
            lineTo(cx - 15f, cy - size.height * 0.22f)
            close()
        },
        Color(0xFFFFD700)
    )
    drawPath(
        Path().apply {
            moveTo(cx + 35f, cy - size.height * 0.18f)
            lineTo(cx + 70f, cy - size.height * 0.35f)
            lineTo(cx + 15f, cy - size.height * 0.22f)
            close()
        },
        Color(0xFFFFD700)
    )

    // Dragon Glowing Eyes
    drawCircle(Color(0xFFFF0055), radius = 12f, center = Offset(cx - 35f, cy - size.height * 0.08f))
    drawCircle(Color(0xFFFF0055), radius = 12f, center = Offset(cx + 35f, cy - size.height * 0.08f))
}

private fun DrawScope.drawComicHalftoneBorder(size: Size) {
    drawRect(
        color = Color.Black,
        size = size,
        style = Stroke(width = 6f)
    )
}

@Composable
fun ComicSpeechBubbleView(bubble: SpeechBubbleItem) {
    val bubbleColor = when (bubble.type) {
        "Shout" -> Color(0xFFFFF275)
        "Thought" -> Color(0xFFF0F3FF)
        "Whisper" -> Color(0xFFFAFAFA)
        else -> Color.White
    }

    val bubbleShape = when (bubble.type) {
        "Thought" -> RoundedCornerShape(28.dp)
        "Shout" -> RoundedCornerShape(4.dp)
        else -> RoundedCornerShape(16.dp)
    }

    Surface(
        color = bubbleColor,
        shape = bubbleShape,
        shadowElevation = 6.dp,
        modifier = Modifier
            .border(2.5.dp, Color.Black, bubbleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = bubble.text,
            color = Color.Black,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}

@Composable
fun ComicSoundEffectView(sfx: SoundEffectItem, isAnimated: Boolean) {
    val sfxColor = try {
        Color(android.graphics.Color.parseColor(sfx.colorHex))
    } catch (e: Exception) {
        Color(0xFFFF5722)
    }

    val infinite = rememberInfiniteTransition(label = "sfx_bounce")
    val pulseScale by infinite.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sfx_pulse"
    )

    val scaleValue = if (isAnimated) pulseScale else 1.0f

    Box(
        modifier = Modifier
            .rotate(-12f)
            .scale(scaleValue)
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .background(sfxColor, RoundedCornerShape(8.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = sfx.text,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif
        )
    }
}
