package com.example.ui.studio

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.example.data.local.entity.SketchEntity
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem

/**
 * Immutable snapshot of the comic canvas state for undo/redo operations.
 */
data class ComicEditorSnapshot(
    val sketchType: String,
    val customBitmap: Bitmap?,
    val activeSketchEntity: SketchEntity?,
    val colorStyle: String,
    val characterColor: Color,
    val auraType: String,
    val inkingEdgeContrast: Float,
    val isCleanPaperEnabled: Boolean,
    val speechBubbles: List<SpeechBubbleItem>,
    val stickers: List<StickerItem>,
    val soundEffects: List<SoundEffectItem>,
    val animationMode: String
)

/**
 * Robust Undo/Redo stack manager with size limiting to prevent memory issues.
 */
class UndoRedoManager(private val maxHistorySize: Int = 30) {
    private val undoStack = mutableListOf<ComicEditorSnapshot>()
    private val redoStack = mutableListOf<ComicEditorSnapshot>()

    // Observable counter to trigger Compose recomposition when stacks change
    var revision by mutableIntStateOf(0)
        private set

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    val undoCount: Int
        get() = undoStack.size

    val redoCount: Int
        get() = redoStack.size

    /**
     * Records the current state snapshot before applying a modification.
     */
    fun recordSnapshot(snapshot: ComicEditorSnapshot) {
        // Deep clone lists to ensure state immutability
        val clonedSnapshot = snapshot.copy(
            speechBubbles = snapshot.speechBubbles.toList(),
            stickers = snapshot.stickers.toList(),
            soundEffects = snapshot.soundEffects.toList()
        )

        // Don't duplicate if identical to last snapshot
        if (undoStack.isNotEmpty() && isIdentical(undoStack.last(), clonedSnapshot)) {
            return
        }

        undoStack.add(clonedSnapshot)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
        revision++
    }

    /**
     * Undoes the last action, restoring previous snapshot and pushing current to redo.
     */
    fun undo(currentState: ComicEditorSnapshot): ComicEditorSnapshot? {
        if (undoStack.isEmpty()) return null
        val previous = undoStack.removeAt(undoStack.lastIndex)
        val currentCloned = currentState.copy(
            speechBubbles = currentState.speechBubbles.toList(),
            stickers = currentState.stickers.toList(),
            soundEffects = currentState.soundEffects.toList()
        )
        redoStack.add(currentCloned)
        revision++
        return previous
    }

    /**
     * Redoes the last undone action, restoring next snapshot and pushing current to undo.
     */
    fun redo(currentState: ComicEditorSnapshot): ComicEditorSnapshot? {
        if (redoStack.isEmpty()) return null
        val next = redoStack.removeAt(redoStack.lastIndex)
        val currentCloned = currentState.copy(
            speechBubbles = currentState.speechBubbles.toList(),
            stickers = currentState.stickers.toList(),
            soundEffects = currentState.soundEffects.toList()
        )
        undoStack.add(currentCloned)
        revision++
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
        revision++
    }

    private fun isIdentical(a: ComicEditorSnapshot, b: ComicEditorSnapshot): Boolean {
        return a.sketchType == b.sketchType &&
                a.colorStyle == b.colorStyle &&
                a.characterColor == b.characterColor &&
                a.auraType == b.auraType &&
                a.inkingEdgeContrast == b.inkingEdgeContrast &&
                a.isCleanPaperEnabled == b.isCleanPaperEnabled &&
                a.animationMode == b.animationMode &&
                a.speechBubbles == b.speechBubbles &&
                a.stickers == b.stickers &&
                a.soundEffects == b.soundEffects &&
                a.customBitmap == b.customBitmap &&
                a.activeSketchEntity?.id == b.activeSketchEntity?.id
    }
}
