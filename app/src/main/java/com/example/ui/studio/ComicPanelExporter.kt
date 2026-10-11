package com.example.ui.studio

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ComicPanelExporter {

    /**
     * Renders the complete, finished comic panel to a high-resolution Bitmap (default 1200x1200px).
     */
    fun renderComicPanel(
        context: Context,
        sketchType: String,
        customBitmap: Bitmap?,
        colorStyle: String,
        characterColor: Color,
        auraType: String,
        inkingEdgeContrast: Float,
        isCleanPaperEnabled: Boolean,
        speechBubbles: List<SpeechBubbleItem>,
        stickers: List<StickerItem>,
        soundEffects: List<SoundEffectItem>,
        layoutType: ComicLayoutType = ComicLayoutType.SINGLE_PANEL,
        width: Int = 1200,
        height: Int = 1200
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Paper Background & Comic Matting
        val bgPaint = Paint().apply {
            color = if (isCleanPaperEnabled) AndroidColor.WHITE else AndroidColor.parseColor("#FFFDF9")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Subtle halftone or dot paper texture if styled
        if (colorStyle == "Vintage Retro Comic") {
            val tintPaint = Paint().apply {
                color = AndroidColor.parseColor("#15E8D8A8")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), tintPaint)
        } else if (colorStyle == "Cyberpunk Glow") {
            val neonBgPaint = Paint().apply {
                color = AndroidColor.parseColor("#180A0520")
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), neonBgPaint)
        }

        // Inner drawing margin
        val margin = 32f
        val drawW = width - (margin * 2)
        val drawH = height - (margin * 2)

        // 2. Aura Effect
        drawAuraEffect(canvas, width, height, auraType)

        // 3. Base Character Drawing (Custom paper capture or vector preset)
        if (customBitmap != null) {
            val scaleX = drawW / customBitmap.width.toFloat()
            val scaleY = drawH / customBitmap.height.toFloat()
            val scale = minOf(scaleX, scaleY)
            val destW = customBitmap.width * scale
            val destH = customBitmap.height * scale
            val left = margin + (drawW - destW) / 2f
            val top = margin + (drawH - destH) / 2f

            val destRect = RectF(left, top, left + destW, top + destH)
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(customBitmap, null, destRect, paint)
        } else {
            drawPresetCharacter(canvas, width, height, sketchType, characterColor.toArgb(), colorStyle)
        }

        // 4. Render Layout Panel Dividers (for multi-panel formats)
        drawLayoutDividers(canvas, width, height, layoutType, margin)

        // 5. Render Speech Bubbles
        speechBubbles.forEach { bubble ->
            drawSpeechBubble(canvas, width, height, bubble)
        }

        // 6. Render Stickers
        stickers.forEach { sticker ->
            drawSticker(canvas, width, height, sticker)
        }

        // 7. Render Sound Effects
        soundEffects.forEach { sfx ->
            drawSoundEffect(canvas, width, height, sfx)
        }

        // 8. Outer Comic Border Frame
        val borderPaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
            isAntiAlias = true
        }
        canvas.drawRect(5f, 5f, width - 5f, height - 5f, borderPaint)

        // Secondary interior comic framing line
        val innerBorderPaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRect(margin - 8f, margin - 8f, width - margin + 8f, height - margin + 8f, innerBorderPaint)

        // Watermark badge at bottom
        val watermarkPaint = Paint().apply {
            color = AndroidColor.parseColor("#88555555")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val layoutTag = layoutType.title
        canvas.drawText("ComicCraft AI Studio • $layoutTag", margin + 12f, height - 12f, watermarkPaint)

        return bitmap
    }

    private fun drawLayoutDividers(
        canvas: Canvas,
        width: Int,
        height: Int,
        layoutType: ComicLayoutType,
        margin: Float
    ) {
        val dividerPaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        val labelBgPaint = Paint().apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val labelBorderPaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val labelTextPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        when (layoutType) {
            ComicLayoutType.THREE_PANEL_STRIP -> {
                val h1 = height / 3f
                val h2 = (height * 2) / 3f
                canvas.drawLine(margin, h1, width - margin, h1, dividerPaint)
                canvas.drawLine(margin, h2, width - margin, h2, dividerPaint)

                listOf(
                    Triple("1 • Setup", margin + 10f, margin + 26f),
                    Triple("2 • Conflict", margin + 10f, h1 + 26f),
                    Triple("3 • Punchline", margin + 10f, h2 + 26f)
                ).forEach { (label, x, y) ->
                    val tw = labelTextPaint.measureText(label)
                    val r = RectF(x - 4f, y - 18f, x + tw + 10f, y + 6f)
                    canvas.drawRoundRect(r, 6f, 6f, labelBgPaint)
                    canvas.drawRoundRect(r, 6f, 6f, labelBorderPaint)
                    canvas.drawText(label, x + 2f, y, labelTextPaint)
                }
            }
            ComicLayoutType.FOUR_PANEL_GRID -> {
                val midX = width / 2f
                val midY = height / 2f
                canvas.drawLine(midX, margin, midX, height - margin, dividerPaint)
                canvas.drawLine(margin, midY, width - margin, midY, dividerPaint)

                listOf(
                    Triple("Panel 1", margin + 10f, margin + 26f),
                    Triple("Panel 2", midX + 10f, margin + 26f),
                    Triple("Panel 3", margin + 10f, midY + 26f),
                    Triple("Panel 4", midX + 10f, midY + 26f)
                ).forEach { (label, x, y) ->
                    val tw = labelTextPaint.measureText(label)
                    val r = RectF(x - 4f, y - 18f, x + tw + 8f, y + 6f)
                    canvas.drawRoundRect(r, 6f, 6f, labelBgPaint)
                    canvas.drawRoundRect(r, 6f, 6f, labelBorderPaint)
                    canvas.drawText(label, x + 2f, y, labelTextPaint)
                }
            }
            ComicLayoutType.TWO_PANEL_SPLIT -> {
                val splitY = height * 0.60f
                canvas.drawLine(margin, splitY, width - margin, splitY, dividerPaint)

                listOf(
                    Triple("Main Action", margin + 10f, margin + 26f),
                    Triple("Reaction", margin + 10f, splitY + 26f)
                ).forEach { (label, x, y) ->
                    val tw = labelTextPaint.measureText(label)
                    val r = RectF(x - 4f, y - 18f, x + tw + 8f, y + 6f)
                    canvas.drawRoundRect(r, 6f, 6f, labelBgPaint)
                    canvas.drawRoundRect(r, 6f, 6f, labelBorderPaint)
                    canvas.drawText(label, x + 2f, y, labelTextPaint)
                }
            }
            ComicLayoutType.CINEMATIC_WIDESCREEN -> {
                val letterboxH = height * 0.12f
                val barPaint = Paint().apply {
                    color = AndroidColor.BLACK
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, width.toFloat(), letterboxH, barPaint)
                canvas.drawRect(0f, height - letterboxH, width.toFloat(), height.toFloat(), barPaint)
            }
            ComicLayoutType.SINGLE_PANEL -> {}
        }
    }

    private fun drawAuraEffect(canvas: Canvas, width: Int, height: Int, auraType: String) {
        val cx = width * 0.5f
        val cy = height * 0.55f
        val auraPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        when (auraType) {
            "Sparkle" -> {
                auraPaint.color = AndroidColor.parseColor("#40FFD700")
                auraPaint.strokeWidth = 18f
                canvas.drawCircle(cx, cy, width * 0.38f, auraPaint)
            }
            "Flame" -> {
                auraPaint.color = AndroidColor.parseColor("#45FF5722")
                auraPaint.strokeWidth = 24f
                canvas.drawCircle(cx, cy, width * 0.40f, auraPaint)
            }
            "Lightning" -> {
                auraPaint.color = AndroidColor.parseColor("#5000E5FF")
                auraPaint.strokeWidth = 16f
                canvas.drawCircle(cx, cy, width * 0.42f, auraPaint)
            }
        }
    }

    private fun drawPresetCharacter(
        canvas: Canvas,
        width: Int,
        height: Int,
        sketchType: String,
        characterColorArgb: Int,
        colorStyle: String
    ) {
        val cx = width * 0.5f
        val cy = height * 0.55f
        val baseRadius = width * 0.22f

        val fillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = characterColorArgb
        }
        val strokePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = AndroidColor.BLACK
            strokeWidth = 8f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        when (sketchType) {
            "hero" -> {
                // Head
                canvas.drawCircle(cx, cy - baseRadius * 0.4f, baseRadius * 0.55f, fillPaint)
                canvas.drawCircle(cx, cy - baseRadius * 0.4f, baseRadius * 0.55f, strokePaint)

                // Mask
                val maskPaint = Paint().apply {
                    color = AndroidColor.BLACK
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val maskRect = RectF(cx - baseRadius * 0.45f, cy - baseRadius * 0.5f, cx + baseRadius * 0.45f, cy - baseRadius * 0.35f)
                canvas.drawRoundRect(maskRect, 16f, 16f, maskPaint)

                // Cape / Torso
                val bodyPath = Path().apply {
                    moveTo(cx - baseRadius * 0.6f, cy + baseRadius * 0.1f)
                    lineTo(cx + baseRadius * 0.6f, cy + baseRadius * 0.1f)
                    lineTo(cx + baseRadius * 0.8f, cy + baseRadius * 1.3f)
                    lineTo(cx - baseRadius * 0.8f, cy + baseRadius * 1.3f)
                    close()
                }
                canvas.drawPath(bodyPath, fillPaint)
                canvas.drawPath(bodyPath, strokePaint)

                // Hero Emblem
                val emblemPaint = Paint().apply {
                    color = AndroidColor.parseColor("#FFEB3B")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(cx, cy + baseRadius * 0.5f, baseRadius * 0.25f, emblemPaint)
                canvas.drawCircle(cx, cy + baseRadius * 0.5f, baseRadius * 0.25f, strokePaint)
            }
            "dragon" -> {
                // Dragon Body & Head
                val dragonHead = Path().apply {
                    moveTo(cx - baseRadius * 0.7f, cy + baseRadius * 0.1f)
                    lineTo(cx + baseRadius * 0.8f, cy - baseRadius * 0.2f)
                    lineTo(cx + baseRadius * 0.4f, cy + baseRadius * 0.7f)
                    close()
                }
                canvas.drawPath(dragonHead, fillPaint)
                canvas.drawPath(dragonHead, strokePaint)

                // Horns
                val hornPaint = Paint().apply {
                    color = AndroidColor.parseColor("#E67E22")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                val hornPath = Path().apply {
                    moveTo(cx - baseRadius * 0.3f, cy - baseRadius * 0.2f)
                    lineTo(cx - baseRadius * 0.6f, cy - baseRadius * 0.8f)
                    lineTo(cx - baseRadius * 0.1f, cy - baseRadius * 0.4f)
                    close()
                }
                canvas.drawPath(hornPath, hornPaint)
                canvas.drawPath(hornPath, strokePaint)
            }
            else -> { // "dog" or general pet
                // Head
                canvas.drawCircle(cx, cy, baseRadius * 0.75f, fillPaint)
                canvas.drawCircle(cx, cy, baseRadius * 0.75f, strokePaint)

                // Ears
                val earPaint = Paint().apply {
                    color = AndroidColor.parseColor("#D35400")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawOval(RectF(cx - baseRadius * 1.05f, cy - baseRadius * 0.7f, cx - baseRadius * 0.45f, cy + baseRadius * 0.2f), earPaint)
                canvas.drawOval(RectF(cx - baseRadius * 1.05f, cy - baseRadius * 0.7f, cx - baseRadius * 0.45f, cy + baseRadius * 0.2f), strokePaint)

                canvas.drawOval(RectF(cx + baseRadius * 0.45f, cy - baseRadius * 0.7f, cx + baseRadius * 1.05f, cy + baseRadius * 0.2f), earPaint)
                canvas.drawOval(RectF(cx + baseRadius * 0.45f, cy - baseRadius * 0.7f, cx + baseRadius * 1.05f, cy + baseRadius * 0.2f), strokePaint)

                // Nose & Eyes
                val blackPaint = Paint().apply {
                    color = AndroidColor.BLACK
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(cx - baseRadius * 0.28f, cy - baseRadius * 0.15f, baseRadius * 0.08f, blackPaint)
                canvas.drawCircle(cx + baseRadius * 0.28f, cy - baseRadius * 0.15f, baseRadius * 0.08f, blackPaint)
                canvas.drawOval(RectF(cx - baseRadius * 0.18f, cy + baseRadius * 0.05f, cx + baseRadius * 0.18f, cy + baseRadius * 0.25f), blackPaint)
            }
        }
    }

    private fun drawSpeechBubble(canvas: Canvas, width: Int, height: Int, bubble: SpeechBubbleItem) {
        val cx = width * bubble.xRatio
        val cy = height * bubble.yRatio
        val scale = bubble.scale.coerceIn(0.5f, 2.5f)

        val textPaint = Paint().apply {
            color = AndroidColor.BLACK
            textSize = 28f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val textWidth = textPaint.measureText(bubble.text)
        val padX = 36f * scale
        val padY = 24f * scale
        val bW = maxOf(140f * scale, textWidth + (padX * 2))
        val bH = 70f * scale

        val rect = RectF(cx - bW / 2, cy - bH / 2, cx + bW / 2, cy + bH / 2)

        val bgPaint = Paint().apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 5f * scale
            isAntiAlias = true
        }

        when (bubble.type) {
            "Thought" -> {
                // Cloud bubble
                canvas.drawRoundRect(rect, 40f * scale, 40f * scale, bgPaint)
                canvas.drawRoundRect(rect, 40f * scale, 40f * scale, borderPaint)
                // Thought dots
                canvas.drawCircle(cx - 20f * scale, cy + bH / 2 + 15f * scale, 10f * scale, bgPaint)
                canvas.drawCircle(cx - 20f * scale, cy + bH / 2 + 15f * scale, 10f * scale, borderPaint)
                canvas.drawCircle(cx - 30f * scale, cy + bH / 2 + 30f * scale, 6f * scale, bgPaint)
                canvas.drawCircle(cx - 30f * scale, cy + bH / 2 + 30f * scale, 6f * scale, borderPaint)
            }
            "Shout" -> {
                // Starburst jagged bubble
                bgPaint.color = AndroidColor.parseColor("#FFFBE6")
                canvas.drawRoundRect(rect, 10f * scale, 10f * scale, bgPaint)
                borderPaint.strokeWidth = 7f * scale
                canvas.drawRoundRect(rect, 10f * scale, 10f * scale, borderPaint)
            }
            "Whisper" -> {
                borderPaint.pathEffect = DashPathEffect(floatArrayOf(12f * scale, 8f * scale), 0f)
                canvas.drawRoundRect(rect, 24f * scale, 24f * scale, bgPaint)
                canvas.drawRoundRect(rect, 24f * scale, 24f * scale, borderPaint)
            }
            else -> {
                // Standard Speech bubble with tail
                canvas.drawRoundRect(rect, 28f * scale, 28f * scale, bgPaint)
                canvas.drawRoundRect(rect, 28f * scale, 28f * scale, borderPaint)

                // Tail
                val tailPath = Path().apply {
                    moveTo(cx - 15f * scale, cy + bH / 2 - 2f)
                    lineTo(cx + 5f * scale, cy + bH / 2 - 2f)
                    lineTo(cx - 25f * scale, cy + bH / 2 + 25f * scale)
                    close()
                }
                canvas.drawPath(tailPath, bgPaint)
                canvas.drawPath(tailPath, borderPaint)
            }
        }

        // Text
        val fontMetrics = textPaint.fontMetrics
        val textY = cy - (fontMetrics.descent + fontMetrics.ascent) / 2
        canvas.drawText(bubble.text, cx, textY, textPaint)
    }

    private fun drawSticker(canvas: Canvas, width: Int, height: Int, sticker: StickerItem) {
        val cx = width * sticker.xRatio
        val cy = height * sticker.yRatio
        val scale = sticker.scale.coerceIn(0.5f, 3.0f)
        val fontSize = 64f * scale

        canvas.save()
        canvas.rotate(sticker.rotation, cx, cy)

        val emojiPaint = Paint().apply {
            textSize = fontSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        // Draw pop-art white halo / sticker border
        val borderPaint = Paint().apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 8f * scale
            textSize = fontSize
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val fontMetrics = emojiPaint.fontMetrics
        val textY = cy - (fontMetrics.descent + fontMetrics.ascent) / 2

        canvas.drawText(sticker.emojiOrIcon, cx, textY, borderPaint)
        canvas.drawText(sticker.emojiOrIcon, cx, textY, emojiPaint)

        canvas.restore()
    }

    private fun drawSoundEffect(canvas: Canvas, width: Int, height: Int, sfx: SoundEffectItem) {
        val cx = width * sfx.xRatio
        val cy = height * sfx.yRatio

        canvas.save()
        canvas.rotate(-10f, cx, cy)

        val strokePaint = Paint().apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 10f
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val fillPaint = Paint().apply {
            color = try {
                AndroidColor.parseColor(sfx.colorHex)
            } catch (_: Exception) {
                AndroidColor.parseColor("#FF5722")
            }
            style = Paint.Style.FILL
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        val fontMetrics = fillPaint.fontMetrics
        val textY = cy - (fontMetrics.descent + fontMetrics.ascent) / 2

        canvas.drawText(sfx.text, cx, textY, strokePaint)
        canvas.drawText(sfx.text, cx, textY, fillPaint)

        canvas.restore()
    }

    /**
     * Saves high-resolution bitmap directly into device gallery using scoped MediaStore.
     * Complies with zero-permission Play policies on modern Android.
     */
    suspend fun saveToGallery(context: Context, bitmap: Bitmap, title: String): Uri? = withContext(Dispatchers.IO) {
        val sanitizedTitle = title.trim().replace("[^a-zA-Z0-9]".toRegex(), "_").ifEmpty { "Comic_Panel" }
        val filename = "${sanitizedTitle}_${System.currentTimeMillis()}.png"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ComicCraft")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null

        try {
            resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }

    /**
     * Exports bitmap to application cache and triggers Android System Share sheet.
     */
    suspend fun shareComicPanel(context: Context, bitmap: Bitmap, title: String) = withContext(Dispatchers.IO) {
        val cacheFolder = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(cacheFolder, "comic_export_${System.currentTimeMillis()}.png")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val authority = "${context.packageName}.provider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "Look at my comic panel created with ComicCraft AI Studio: $title!")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Share Comic Panel via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
