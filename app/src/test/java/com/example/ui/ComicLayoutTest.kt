package com.example.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.ui.studio.ComicEditorSnapshot
import com.example.ui.studio.ComicLayoutType
import com.example.ui.studio.ComicPanelExporter
import com.example.ui.studio.UndoRedoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComicLayoutTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testComicLayoutTypesMetadata() {
        assertEquals(5, ComicLayoutType.entries.size)

        // Single Panel
        val single = ComicLayoutType.SINGLE_PANEL
        assertEquals(1, single.panelCount)
        assertEquals(1.0f, single.aspectRatio, 0.001f)

        // 3-Panel Strip
        val threePanel = ComicLayoutType.THREE_PANEL_STRIP
        assertEquals(3, threePanel.panelCount)
        assertEquals(3, threePanel.panelLabels.size)
        assertTrue(threePanel.panelLabels[0].contains("Setup"))
        assertTrue(threePanel.panelLabels[1].contains("Conflict"))
        assertTrue(threePanel.panelLabels[2].contains("Punchline"))

        // 4-Panel Grid
        val fourGrid = ComicLayoutType.FOUR_PANEL_GRID
        assertEquals(4, fourGrid.panelCount)
        assertEquals(4, fourGrid.panelLabels.size)

        // 2-Panel Split
        val twoSplit = ComicLayoutType.TWO_PANEL_SPLIT
        assertEquals(2, twoSplit.panelCount)

        // Cinematic Widescreen
        val cinema = ComicLayoutType.CINEMATIC_WIDESCREEN
        assertEquals(16f / 9f, cinema.aspectRatio, 0.01f)
    }

    @Test
    fun testLayoutUndoRedoBehavior() {
        val manager = UndoRedoManager(maxHistorySize = 10)

        val initialSnapshot = ComicEditorSnapshot(
            sketchType = "dog",
            customBitmap = null,
            activeSketchEntity = null,
            colorStyle = "Vibrant Cel-Shaded",
            characterColor = Color.Yellow,
            auraType = "None",
            inkingEdgeContrast = 0.85f,
            isCleanPaperEnabled = true,
            speechBubbles = emptyList(),
            stickers = emptyList(),
            soundEffects = emptyList(),
            animationMode = "Camera Pan & Zoom",
            layout = ComicLayoutType.SINGLE_PANEL
        )
        manager.recordSnapshot(initialSnapshot)

        val updatedLayoutSnapshot = initialSnapshot.copy(layout = ComicLayoutType.THREE_PANEL_STRIP)
        assertTrue(manager.canUndo)

        val reverted = manager.undo(updatedLayoutSnapshot)
        assertNotNull(reverted)
        assertEquals(ComicLayoutType.SINGLE_PANEL, reverted!!.layout)

        val redone = manager.redo(reverted)
        assertNotNull(redone)
        assertEquals(ComicLayoutType.THREE_PANEL_STRIP, redone!!.layout)
    }

    @Test
    fun testExportMultiPanelLayouts() {
        listOf(
            ComicLayoutType.SINGLE_PANEL,
            ComicLayoutType.THREE_PANEL_STRIP,
            ComicLayoutType.FOUR_PANEL_GRID,
            ComicLayoutType.TWO_PANEL_SPLIT,
            ComicLayoutType.CINEMATIC_WIDESCREEN
        ).forEach { layout ->
            val rendered = ComicPanelExporter.renderComicPanel(
                context = context,
                sketchType = "dog",
                customBitmap = null,
                colorStyle = "Vibrant Cel-Shaded",
                characterColor = Color.Yellow,
                auraType = "Sparkle",
                inkingEdgeContrast = 0.85f,
                isCleanPaperEnabled = true,
                speechBubbles = emptyList(),
                stickers = emptyList(),
                soundEffects = emptyList(),
                layoutType = layout,
                width = 800,
                height = 800
            )

            assertNotNull(rendered)
            assertEquals(800, rendered.width)
            assertEquals(800, rendered.height)
        }
    }
}
