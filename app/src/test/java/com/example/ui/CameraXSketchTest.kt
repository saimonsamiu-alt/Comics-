package com.example.ui

import android.graphics.Bitmap
import com.example.ui.studio.enhanceSketchLines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CameraXSketchTest {

    @Test
    fun enhanceSketchLines_sharpensBitmap() {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val enhanced = enhanceSketchLines(bitmap)

        assertNotNull(enhanced)
        assertEquals(100, enhanced.width)
        assertEquals(100, enhanced.height)
    }
}
