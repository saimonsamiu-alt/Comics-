package com.example.ui

import com.example.data.model.StickerItem
import com.example.ui.studio.ChildFriendlyStickers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StickerTest {

    @Test
    fun testStickerItemModelCreationAndProperties() {
        val sticker = StickerItem(
            id = "stk_1",
            emojiOrIcon = "⭐",
            label = "Golden Star",
            category = "Power",
            xRatio = 0.5f,
            yRatio = 0.3f,
            scale = 1.2f,
            rotation = 15f
        )

        assertEquals("stk_1", sticker.id)
        assertEquals("⭐", sticker.emojiOrIcon)
        assertEquals("Golden Star", sticker.label)
        assertEquals("Power", sticker.category)
        assertEquals(0.5f, sticker.xRatio, 0.001f)
        assertEquals(0.3f, sticker.yRatio, 0.001f)
        assertEquals(1.2f, sticker.scale, 0.001f)
        assertEquals(15f, sticker.rotation, 0.001f)
    }

    @Test
    fun testStickerTransformationMoveResizeRotate() {
        var sticker = StickerItem(
            id = "stk_2",
            emojiOrIcon = "👑",
            label = "Royal Crown",
            category = "Gear"
        )

        // Move onto hero's head in the sketch
        sticker = sticker.copy(xRatio = 0.45f, yRatio = 0.15f)
        assertEquals(0.45f, sticker.xRatio, 0.001f)
        assertEquals(0.15f, sticker.yRatio, 0.001f)

        // Scale up to fit character
        sticker = sticker.copy(scale = 1.5f)
        assertEquals(1.5f, sticker.scale, 0.001f)

        // Rotate slightly for playful angle
        sticker = sticker.copy(rotation = -12f)
        assertEquals(-12f, sticker.rotation, 0.001f)
    }

    @Test
    fun testChildFriendlyPresetsCategoriesExist() {
        val presets = ChildFriendlyStickers.presets
        assertTrue(presets.isNotEmpty())

        val categories = presets.map { it.category }.toSet()
        assertTrue(categories.contains("Power"))
        assertTrue(categories.contains("Animal"))
        assertTrue(categories.contains("Gear"))
        assertTrue(categories.contains("Fun"))

        // Ensure each preset has non-blank emoji and label
        presets.forEach { preset ->
            assertTrue(preset.emoji.isNotBlank())
            assertTrue(preset.label.isNotBlank())
        }
    }
}
