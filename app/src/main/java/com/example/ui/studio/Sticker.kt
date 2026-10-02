package com.example.ui.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StickerItem
import kotlin.math.roundToInt

/**
 * Pre-made child-friendly sticker preset definition.
 */
data class StickerPreset(
    val emoji: String,
    val label: String,
    val category: String // "Power", "Animal", "Gear", "Fun"
)

object ChildFriendlyStickers {
    val presets = listOf(
        // Superpowers & Energy FX
        StickerPreset("⭐", "Golden Star", "Power"),
        StickerPreset("⚡", "Thunder Spark", "Power"),
        StickerPreset("🔥", "Energy Flame", "Power"),
        StickerPreset("💥", "Action Blast", "Power"),
        StickerPreset("✨", "Magic Sparkles", "Power"),
        StickerPreset("🌪️", "Wind Tornado", "Power"),
        StickerPreset("💫", "Dizzy Stars", "Power"),
        StickerPreset("☄️", "Cosmic Meteor", "Power"),

        // Cute Animals & Pals
        StickerPreset("🐱", "Kitty Cat", "Animal"),
        StickerPreset("🐶", "Happy Doggo", "Animal"),
        StickerPreset("🐰", "Speedy Bunny", "Animal"),
        StickerPreset("🦊", "Clever Fox", "Animal"),
        StickerPreset("🐼", "Kung Fu Panda", "Animal"),
        StickerPreset("🦄", "Magic Unicorn", "Animal"),
        StickerPreset("🦁", "Brave Lion", "Animal"),
        StickerPreset("🐸", "Ninja Frog", "Animal"),

        // Hero Gear & Props
        StickerPreset("👑", "Royal Crown", "Gear"),
        StickerPreset("🕶️", "Cool Shades", "Gear"),
        StickerPreset("🛡️", "Hero Shield", "Gear"),
        StickerPreset("⚔️", "Adventure Sword", "Gear"),
        StickerPreset("🚀", "Rocket Booster", "Gear"),
        StickerPreset("🏆", "Victory Cup", "Gear"),
        StickerPreset("🎩", "Magician Hat", "Gear"),
        StickerPreset("🥊", "Power Glove", "Gear"),

        // Fun Cartoon Expressions & Badges
        StickerPreset("😎", "Super Cool", "Fun"),
        StickerPreset("🤩", "Star Eyes", "Fun"),
        StickerPreset("😜", "Playful Wink", "Fun"),
        StickerPreset("🦸", "Cape Hero", "Fun"),
        StickerPreset("❤️", "Hero Heart", "Fun"),
        StickerPreset("🎨", "Color Palette", "Fun"),
        StickerPreset("🍕", "Hero Snack", "Fun"),
        StickerPreset("🎉", "Celebration", "Fun")
    )
}

/**
 * A playful Sticker component designed for enhancing comic panels and hand-drawn sketches.
 */
@Composable
fun Sticker(
    sticker: StickerItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onScaleChanged: ((Float) -> Unit)? = null,
    onRotateChanged: ((Float) -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val scale = if (sticker.scale <= 0f) 1.0f else sticker.scale

    Box(
        modifier = modifier
            .scale(scale)
            .rotate(sticker.rotation)
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, rotation ->
                    if (onScaleChanged != null && zoom != 1.0f) {
                        val newScale = (scale * zoom).coerceIn(0.5f, 3.0f)
                        onScaleChanged(newScale)
                    }
                    if (onRotateChanged != null && rotation != 0f) {
                        val newRot = (sticker.rotation + rotation) % 360f
                        onRotateChanged(newRot)
                    }
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onSelect()
            }
            .testTag("sticker_component_${sticker.id}")
    ) {
        // Comic sticker badge appearance
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = if (isSelected) 8.dp else 4.dp,
            modifier = Modifier
                .border(
                    width = if (isSelected) 2.5.dp else 2.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = sticker.emojiOrIcon,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(2.dp)
            )
        }

        // Selection Controls: Delete & Resize & Rotate handles
        if (isSelected) {
            // Delete button at top-right
            if (onDelete != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete Sticker",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Resize handle at bottom-right
            if (onScaleChanged != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 8.dp, y = 8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .pointerInput(scale) {
                            detectDragGestures { _, dragAmount ->
                                val delta = (dragAmount.x + dragAmount.y) * 0.02f
                                val newScale = (scale + delta).coerceIn(0.5f, 3.0f)
                                onScaleChanged(newScale)
                            }
                        }
                        .testTag("sticker_resize_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Resize Sticker",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Rotate handle at top-left
            if (onRotateChanged != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable {
                            val nextRot = (sticker.rotation + 45f) % 360f
                            onRotateChanged(nextRot)
                        }
                        .testTag("sticker_rotate_handle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rotate Sticker",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Movable and resizable wrapper for placing a Sticker on top of a comic canvas / sketch.
 */
@Composable
fun MovableResizableSticker(
    sticker: StickerItem,
    isSelected: Boolean,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
    onSelect: () -> Unit,
    onPositionChanged: (Float, Float) -> Unit,
    onScaleChanged: (Float) -> Unit,
    onRotateChanged: (Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posX = (sticker.xRatio * canvasWidthPx).roundToInt()
    val posY = (sticker.yRatio * canvasHeightPx).roundToInt()

    Box(
        modifier = modifier
            .wrapContentSize()
            .offset { IntOffset(posX, posY) }
            .pointerInput(sticker.id) {
                detectDragGestures(
                    onDragStart = { onSelect() }
                ) { change, dragAmount ->
                    change.consume()
                    val newX = ((sticker.xRatio * canvasWidthPx + dragAmount.x) / canvasWidthPx).coerceIn(0.02f, 0.90f)
                    val newY = ((sticker.yRatio * canvasHeightPx + dragAmount.y) / canvasHeightPx).coerceIn(0.02f, 0.90f)
                    onPositionChanged(newX, newY)
                }
            }
    ) {
        Sticker(
            sticker = sticker,
            isSelected = isSelected,
            onSelect = onSelect,
            onScaleChanged = onScaleChanged,
            onRotateChanged = onRotateChanged,
            onDelete = onDelete
        )
    }
}

/**
 * Child-friendly sticker drawer/tray that lets kids pick emojis and icons to add to their drawing.
 */
@Composable
fun StickerPickerTray(
    onAddSticker: (emoji: String, label: String, category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("Power") }
    val categories = listOf("Power" to "⚡ Powers", "Animal" to "🐾 Animals", "Gear" to "👑 Gear", "Fun" to "🎉 Fun")

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Sticker Fun Pack",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Tap a sticker to add onto your paper sketch!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { (catKey, catLabel) ->
                    FilterChip(
                        selected = selectedCategory == catKey,
                        onClick = { selectedCategory = catKey },
                        label = { Text(catLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stickers Grid
            val filteredStickers = ChildFriendlyStickers.presets.filter { it.category == selectedCategory }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filteredStickers) { preset ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                onAddSticker(preset.emoji, preset.label, preset.category)
                            }
                            .testTag("sticker_preset_${preset.label.replace(" ", "_")}")
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = preset.emoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
