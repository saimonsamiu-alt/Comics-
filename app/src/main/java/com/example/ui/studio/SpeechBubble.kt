package com.example.ui.studio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeechBubbleItem
import kotlin.math.roundToInt

/**
 * A highly interactive SpeechBubble component that:
 * 1. Can be moved (dragged) freely across a base image or canvas
 * 2. Can be resized via pinch gesture or corner drag handle
 * 3. Allows users to tap and type text directly into the bubble
 */
@Composable
fun SpeechBubble(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: String = "Speech", // Speech, Thought, Shout, Whisper
    scale: Float = 1.0f,
    onScaleChanged: ((Float) -> Unit)? = null,
    isSelected: Boolean = false,
    onSelect: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    isEditable: Boolean = true
) {
    var isEditing by remember { mutableStateOf(false) }
    var currentText by remember(text) { mutableStateOf(text) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val bubbleBgColor = when (type) {
        "Shout" -> Color(0xFFFFF275)
        "Thought" -> Color(0xFFF1F5F9)
        "Whisper" -> Color(0xFFFAFAFA)
        else -> Color.White
    }

    val bubbleShape = when (type) {
        "Thought" -> RoundedCornerShape(26.dp)
        "Shout" -> RoundedCornerShape(6.dp)
        "Whisper" -> RoundedCornerShape(14.dp)
        else -> RoundedCornerShape(18.dp)
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (onScaleChanged != null && zoom != 1.0f) {
                        val newScale = (scale * zoom).coerceIn(0.6f, 2.5f)
                        onScaleChanged(newScale)
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSelect?.invoke()
                if (isEditable) {
                    isEditing = true
                }
            }
            .testTag("speech_bubble_component")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Main Bubble Container
            Surface(
                color = bubbleBgColor,
                shape = bubbleShape,
                shadowElevation = if (isSelected) 8.dp else 4.dp,
                modifier = Modifier
                    .widthIn(min = 90.dp, max = 220.dp)
                    .border(
                        width = if (isSelected) 3.dp else 2.5.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                        shape = bubbleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (isEditing) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(IntrinsicSize.Min)
                    ) {
                        BasicTextField(
                            value = currentText,
                            onValueChange = {
                                currentText = it
                                onTextChanged(it)
                            },
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isEditing = false
                                    focusManager.clearFocus()
                                    onTextChanged(currentText)
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .widthIn(min = 80.dp)
                                .padding(2.dp)
                                .testTag("speech_bubble_input_field")
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Done typing checkmark button
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    isEditing = false
                                    focusManager.clearFocus()
                                    onTextChanged(currentText)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done typing",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentText.isBlank()) "Tap to type..." else currentText,
                            color = if (currentText.isBlank()) Color.Gray else Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }

            // Tail indicator depending on bubble type
            SpeechBubbleTail(type = type, color = bubbleBgColor, isSelected = isSelected)
        }

        // Selection Controls: Resize Handle & Delete Button
        if (isSelected) {
            // Delete button at top-right
            if (onDelete != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete bubble",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Resize handle at bottom-right
            if (onScaleChanged != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 10.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .pointerInput(scale) {
                            detectDragGestures { _, dragAmount ->
                                val delta = (dragAmount.x + dragAmount.y) * 0.015f
                                val newScale = (scale + delta).coerceIn(0.6f, 2.5f)
                                onScaleChanged(newScale)
                            }
                        }
                        .testTag("speech_bubble_resize_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Resize bubble",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Draws the comic speech bubble tail or thought bubbles under the main balloon.
 */
@Composable
private fun SpeechBubbleTail(
    type: String,
    color: Color,
    isSelected: Boolean
) {
    when (type) {
        "Thought" -> {
            // Little cloud circles for thought bubbles
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(x = (-14).dp, y = (-2).dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(color, CircleShape)
                        .border(1.5.dp, Color.Black, CircleShape)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(color, CircleShape)
                        .border(1.5.dp, Color.Black, CircleShape)
                )
            }
        }
        "Shout" -> {
            // Spiky tail
            Canvas(modifier = Modifier.size(width = 20.dp, height = 14.dp).offset(x = (-16).dp, y = (-2).dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.3f, size.height)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, color = color)
                drawPath(path, color = Color.Black, style = Stroke(width = 2.5f))
            }
        }
        else -> {
            // Classic Speech Tail pointing downward-left
            Canvas(modifier = Modifier.size(width = 18.dp, height = 12.dp).offset(x = (-16).dp, y = (-2).dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    quadraticBezierTo(
                        size.width * 0.2f, size.height * 0.8f,
                        0f, size.height
                    )
                    quadraticBezierTo(
                        size.width * 0.6f, size.height * 0.5f,
                        size.width, 0f
                    )
                    close()
                }
                drawPath(path, color = color)
                drawPath(path, color = Color.Black, style = Stroke(width = 2.5f))
            }
        }
    }
}

/**
 * Draggable and resizable container holding a SpeechBubble on top of a base canvas/image.
 */
@Composable
fun MovableResizableSpeechBubble(
    bubble: SpeechBubbleItem,
    isSelected: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onSelect: () -> Unit,
    onPositionChanged: (Float, Float) -> Unit,
    onScaleChanged: (Float) -> Unit,
    onTextChanged: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentScale = if (bubble.scale <= 0f) 1.0f else bubble.scale
    val posX = (bubble.xRatio * canvasWidthPx).roundToInt()
    val posY = (bubble.yRatio * canvasHeightPx).roundToInt()

    Box(
        modifier = modifier
            .wrapContentSize()
            .offset { IntOffset(posX, posY) }
            .pointerInput(bubble.id) {
                detectDragGestures(
                    onDragStart = { onSelect() }
                ) { change, dragAmount ->
                    change.consume()
                    val newX = ((bubble.xRatio * canvasWidthPx + dragAmount.x) / canvasWidthPx).coerceIn(0.02f, 0.85f)
                    val newY = ((bubble.yRatio * canvasHeightPx + dragAmount.y) / canvasHeightPx).coerceIn(0.02f, 0.88f)
                    onPositionChanged(newX, newY)
                }
            }
    ) {
        SpeechBubble(
            text = bubble.text,
            type = bubble.type,
            scale = currentScale,
            isSelected = isSelected,
            onSelect = onSelect,
            onTextChanged = onTextChanged,
            onScaleChanged = onScaleChanged,
            onDelete = onDelete
        )
    }
}
