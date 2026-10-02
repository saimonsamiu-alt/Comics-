package com.example.ui

import com.example.data.model.SavedComicPanelState
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SavedPanelStateTest {

    @Test
    fun testSavedComicPanelStateModelCreation() {
        val bubble = SpeechBubbleItem(
            id = "b1",
            text = "Ready to save!",
            type = "Speech",
            xRatio = 0.2f,
            yRatio = 0.3f,
            scale = 1.2f
        )
        val sticker = StickerItem(
            id = "s1",
            emojiOrIcon = "⭐",
            label = "Golden Star",
            category = "Power",
            xRatio = 0.6f,
            yRatio = 0.4f,
            scale = 1.0f,
            rotation = 20f
        )
        val sfx = SoundEffectItem(
            id = "sfx1",
            text = "BAM!",
            colorHex = "#FF5722",
            xRatio = 0.5f,
            yRatio = 0.5f
        )

        val panelState = SavedComicPanelState(
            id = "panel_123",
            userId = "user_abc",
            title = "Battle for Comic City",
            baseImage = "builtin:hero",
            sketchType = "hero",
            colorStyle = "Cyberpunk Glow",
            characterColor = "#E84393",
            auraType = "Lightning",
            inkingEdgeContrast = 0.9f,
            isCleanPaperEnabled = true,
            animationMode = "Action Shake",
            speechBubbles = listOf(bubble),
            stickers = listOf(sticker),
            soundEffects = listOf(sfx)
        )

        assertEquals("panel_123", panelState.id)
        assertEquals("user_abc", panelState.userId)
        assertEquals("Battle for Comic City", panelState.title)
        assertEquals("builtin:hero", panelState.baseImage)
        assertEquals(1, panelState.speechBubbles.size)
        assertEquals("Ready to save!", panelState.speechBubbles[0].text)
        assertEquals(1, panelState.stickers.size)
        assertEquals("⭐", panelState.stickers[0].emojiOrIcon)
        assertEquals(20f, panelState.stickers[0].rotation, 0.001f)
        assertEquals(1, panelState.soundEffects.size)
    }

    @Test
    fun testPanelSerializationAndJsonRoundtrip() {
        val bubblesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "b_test")
                put("text", "Let's explore!")
                put("type", "Thought")
                put("xRatio", 0.3)
                put("yRatio", 0.4)
                put("scale", 1.1)
            })
        }

        val stickersArray = JSONArray().apply {
            put(JSONObject().apply {
                put("id", "stk_test")
                put("emojiOrIcon", "👑")
                put("label", "Royal Crown")
                put("category", "Gear")
                put("xRatio", 0.5)
                put("yRatio", 0.15)
                put("scale", 1.3)
                put("rotation", -15.0)
            })
        }

        val container = JSONObject().apply {
            put("bubbles", bubblesArray)
            put("stickers", stickersArray)
        }
        val serializedJson = container.toString()

        assertNotNull(serializedJson)
        assertTrue(serializedJson.contains("Let's explore!"))
        assertTrue(serializedJson.contains("👑"))

        // Parse back
        val parsed = JSONObject(serializedJson)
        val restoredBubbles = parsed.getJSONArray("bubbles")
        val restoredStickers = parsed.getJSONArray("stickers")

        assertEquals(1, restoredBubbles.length())
        assertEquals("Let's explore!", restoredBubbles.getJSONObject(0).getString("text"))
        assertEquals("Thought", restoredBubbles.getJSONObject(0).getString("type"))

        assertEquals(1, restoredStickers.length())
        assertEquals("👑", restoredStickers.getJSONObject(0).getString("emojiOrIcon"))
        assertEquals(-15.0, restoredStickers.getJSONObject(0).getDouble("rotation"), 0.001)
    }
}
