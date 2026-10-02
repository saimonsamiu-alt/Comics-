package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import com.example.ui.studio.ComicPanelExporter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComicPanelExporterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testRenderComicPanelToBitmapHighResolution() {
        val bubble = SpeechBubbleItem(
            id = "b1",
            text = "Victory is ours!",
            type = "Shout",
            xRatio = 0.5f,
            yRatio = 0.25f,
            scale = 1.1f
        )
        val sticker = StickerItem(
            id = "s1",
            emojiOrIcon = "⭐",
            label = "Power Star",
            category = "Power",
            xRatio = 0.7f,
            yRatio = 0.3f,
            scale = 1.2f,
            rotation = 15f
        )
        val sfx = SoundEffectItem(
            id = "sfx1",
            text = "KAPOW!",
            colorHex = "#FF5722",
            xRatio = 0.3f,
            yRatio = 0.7f
        )

        val renderedBitmap = ComicPanelExporter.renderComicPanel(
            context = context,
            sketchType = "hero",
            customBitmap = null,
            colorStyle = "Vibrant Cel-Shaded",
            characterColor = Color(0xFFFDCB6E),
            auraType = "Sparkle",
            inkingEdgeContrast = 0.9f,
            isCleanPaperEnabled = true,
            speechBubbles = listOf(bubble),
            stickers = listOf(sticker),
            soundEffects = listOf(sfx),
            width = 1200,
            height = 1200
        )

        assertNotNull(renderedBitmap)
        assertEquals(1200, renderedBitmap.width)
        assertEquals(1200, renderedBitmap.height)
        assertEquals(Bitmap.Config.ARGB_8888, renderedBitmap.config)
    }

    @Test
    fun testRenderComicPanelWithCustomBitmap() {
        val dummyDrawing = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)

        val renderedBitmap = ComicPanelExporter.renderComicPanel(
            context = context,
            sketchType = "custom",
            customBitmap = dummyDrawing,
            colorStyle = "Cyberpunk Glow",
            characterColor = Color(0xFFE84393),
            auraType = "Lightning",
            inkingEdgeContrast = 0.8f,
            isCleanPaperEnabled = true,
            speechBubbles = emptyList(),
            stickers = emptyList(),
            soundEffects = emptyList(),
            width = 800,
            height = 800
        )

        assertNotNull(renderedBitmap)
        assertEquals(800, renderedBitmap.width)
        assertEquals(800, renderedBitmap.height)
    }

    @Test
    fun testSaveToGalleryAndMediaStore() = runBlocking {
        val testBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val uri = ComicPanelExporter.saveToGallery(context, testBitmap, "Hero Scene")

        assertNotNull(uri)
        assertTrue(uri.toString().contains("content://media"))
    }
}
