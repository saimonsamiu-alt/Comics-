package com.example.ui

import androidx.compose.ui.graphics.Color
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import com.example.ui.studio.ComicEditorSnapshot
import com.example.ui.studio.UndoRedoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UndoRedoManagerTest {

    private fun createSampleSnapshot(
        sketchType: String = "dog",
        bubbleCount: Int = 1,
        stickerCount: Int = 0
    ): ComicEditorSnapshot {
        val bubbles = (1..bubbleCount).map { i ->
            SpeechBubbleItem(id = "b_$i", text = "Bubble $i", type = "Speech", xRatio = 0.2f * i, yRatio = 0.2f * i)
        }
        val stickers = (1..stickerCount).map { i ->
            StickerItem(id = "s_$i", emojiOrIcon = "⭐", label = "Star $i", category = "Power")
        }
        return ComicEditorSnapshot(
            sketchType = sketchType,
            customBitmap = null,
            activeSketchEntity = null,
            colorStyle = "Vibrant Cel-Shaded",
            characterColor = Color(0xFFFDCB6E),
            auraType = "Sparkle",
            inkingEdgeContrast = 0.85f,
            isCleanPaperEnabled = true,
            speechBubbles = bubbles,
            stickers = stickers,
            soundEffects = listOf(SoundEffectItem(id = "sfx_1", text = "POW!")),
            animationMode = "Camera Pan & Zoom"
        )
    }

    @Test
    fun testInitialStateEmptyStack() {
        val manager = UndoRedoManager(maxHistorySize = 10)
        assertFalse(manager.canUndo)
        assertFalse(manager.canRedo)
        assertEquals(0, manager.undoCount)
        assertEquals(0, manager.redoCount)
    }

    @Test
    fun testUndoSpeechBubbleAddition() {
        val manager = UndoRedoManager(maxHistorySize = 10)

        val state1 = createSampleSnapshot(bubbleCount = 1)
        manager.recordSnapshot(state1)

        val state2 = createSampleSnapshot(bubbleCount = 2)
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)

        // Undo to revert back to 1 bubble
        val restoredState = manager.undo(state2)
        assertNotNull(restoredState)
        assertEquals(1, restoredState!!.speechBubbles.size)
        assertEquals("Bubble 1", restoredState.speechBubbles[0].text)

        // After undo, canRedo becomes true
        assertTrue(manager.canRedo)
        assertFalse(manager.canUndo)

        // Redo to reapply 2 bubbles
        val redoneState = manager.redo(restoredState)
        assertNotNull(redoneState)
        assertEquals(2, redoneState!!.speechBubbles.size)
        assertTrue(manager.canUndo)
        assertFalse(manager.canRedo)
    }

    @Test
    fun testUndoStickerAddition() {
        val manager = UndoRedoManager(maxHistorySize = 10)

        val stateWithNoStickers = createSampleSnapshot(stickerCount = 0)
        manager.recordSnapshot(stateWithNoStickers)

        val stateWithSticker = createSampleSnapshot(stickerCount = 1)
        val reverted = manager.undo(stateWithSticker)

        assertNotNull(reverted)
        assertEquals(0, reverted!!.stickers.size)

        val reApplied = manager.redo(reverted)
        assertNotNull(reApplied)
        assertEquals(1, reApplied!!.stickers.size)
        assertEquals("⭐", reApplied.stickers[0].emojiOrIcon)
    }

    @Test
    fun testUndoSketchTypeChange() {
        val manager = UndoRedoManager(maxHistorySize = 10)

        val initialSketch = createSampleSnapshot(sketchType = "dog")
        manager.recordSnapshot(initialSketch)

        val modifiedSketch = createSampleSnapshot(sketchType = "dragon")
        val restored = manager.undo(modifiedSketch)

        assertNotNull(restored)
        assertEquals("dog", restored!!.sketchType)

        val redone = manager.redo(restored)
        assertNotNull(redone)
        assertEquals("dragon", redone!!.sketchType)
    }

    @Test
    fun testMaxHistorySizeTrimming() {
        val manager = UndoRedoManager(maxHistorySize = 5)

        for (i in 1..10) {
            manager.recordSnapshot(createSampleSnapshot(sketchType = "sketch_$i"))
        }

        // Must not exceed max history size
        assertEquals(5, manager.undoCount)
    }

    @Test
    fun testBoundarySafety() {
        val manager = UndoRedoManager()
        val dummy = createSampleSnapshot()

        // Calling undo or redo on empty stack returns null without throwing
        assertNull(manager.undo(dummy))
        assertNull(manager.redo(dummy))
    }
}
