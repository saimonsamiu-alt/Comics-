package com.example.ui

import com.example.data.model.SpeechBubbleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechBubbleTest {

    @Test
    fun testSpeechBubbleModelCreationAndDefaults() {
        val bubble = SpeechBubbleItem(
            id = "bubble_1",
            text = "Welcome to ComicCraft!",
            type = "Speech",
            xRatio = 0.25f,
            yRatio = 0.35f,
            scale = 1.0f
        )

        assertEquals("bubble_1", bubble.id)
        assertEquals("Welcome to ComicCraft!", bubble.text)
        assertEquals("Speech", bubble.type)
        assertEquals(0.25f, bubble.xRatio, 0.001f)
        assertEquals(0.35f, bubble.yRatio, 0.001f)
        assertEquals(1.0f, bubble.scale, 0.001f)
    }

    @Test
    fun testSpeechBubbleRepositionAndResize() {
        var bubble = SpeechBubbleItem(
            id = "bubble_2",
            text = "Initial Text",
            type = "Thought",
            xRatio = 0.1f,
            yRatio = 0.1f,
            scale = 1.0f
        )

        // Simulate moving bubble across base image
        val newX = 0.45f
        val newY = 0.65f
        bubble = bubble.copy(xRatio = newX, yRatio = newY)

        assertEquals(0.45f, bubble.xRatio, 0.001f)
        assertEquals(0.65f, bubble.yRatio, 0.001f)

        // Simulate resizing bubble
        val newScale = 1.6f
        bubble = bubble.copy(scale = newScale)
        assertEquals(1.6f, bubble.scale, 0.001f)

        // Simulate user typing text directly into bubble
        val updatedText = "I have a great idea for this scene!"
        bubble = bubble.copy(text = updatedText)
        assertEquals("I have a great idea for this scene!", bubble.text)
    }

    @Test
    fun testDifferentSpeechBubbleTypes() {
        val types = listOf("Speech", "Thought", "Shout", "Whisper")
        types.forEach { type ->
            val bubble = SpeechBubbleItem(id = "b_$type", text = "Dialogue", type = type)
            assertTrue(bubble.type in types)
        }
    }
}
