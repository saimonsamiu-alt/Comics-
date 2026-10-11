package com.example.ui.studio

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem

data class ComicEditorSnapshot(
    val sketchType: String,
    val capturedBitmap: Bitmap?,
    val speechBubbles: List<SpeechBubbleItem>,
    val stickers: List<StickerItem>,
    val soundEffects: List<SoundEffectItem>,
    val colorStyle: String,
    val inkingContrast: Float,
    val animationMode: String
)

class UndoRedoManager(private val maxHistory: Int = 25) {
    private val undoStack = mutableListOf<ComicEditorSnapshot>()
    private val redoStack = mutableListOf<ComicEditorSnapshot>()

    var changeCounter by mutableIntStateOf(0)
        private set

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun recordSnapshot(currentSnapshot: ComicEditorSnapshot) {
        undoStack.add(currentSnapshot)
        if (undoStack.size > maxHistory) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        changeCounter++
    }

    fun undo(currentSnapshot: ComicEditorSnapshot): ComicEditorSnapshot? {
        if (!canUndo) return null
        val previous = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(currentSnapshot)
        changeCounter++
        return previous
    }

    fun redo(currentSnapshot: ComicEditorSnapshot): ComicEditorSnapshot? {
        if (!canRedo) return null
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(currentSnapshot)
        changeCounter++
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        changeCounter = 0
    }
}
