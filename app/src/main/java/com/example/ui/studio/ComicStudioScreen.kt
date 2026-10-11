package com.example.ui.studio

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import com.example.data.repository.ComicRepository
import com.example.ui.auth.AuthManager
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ComicStudioScreen(
    modifier: Modifier = Modifier,
    initialLayout: ComicLayoutType = ComicLayoutType.SINGLE_PANEL,
    onBackToLayouts: () -> Unit,
    repository: ComicRepository = remember { ComicRepository() },
    authManager: AuthManager? = null
) {
    val context = LocalContext.current
    val actualAuthManager = authManager ?: remember { AuthManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // Studio Canvas State
    var selectedLayout by remember { mutableStateOf(initialLayout) }
    var selectedSketch by remember { mutableStateOf("Hero Action") }
    var capturedSketchBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedColorStyle by remember { mutableStateOf("Vibrant Webtoon") }
    var inkingContrast by remember { mutableStateOf(1.2f) }
    var animationMode by remember { mutableStateOf("Camera Pan & Zoom") }

    val speechBubbles = remember { mutableStateListOf<SpeechBubbleItem>().apply { addAll(ChildFriendlyStickers.defaultBubbles) } }
    val stickers = remember { mutableStateListOf<StickerItem>() }
    val soundEffects = remember { mutableStateListOf<SoundEffectItem>() }

    // Active Tool Tab: 0 -> Character & Inks, 1 -> Dialogues & SFX, 2 -> Stickers, 3 -> Color Styles
    var selectedToolTab by remember { mutableIntStateOf(0) }

    // Undo / Redo Stack Manager
    val undoRedoManager = remember { UndoRedoManager() }

    fun takeSnapshot() = ComicEditorSnapshot(
        sketchType = selectedSketch,
        capturedBitmap = capturedSketchBitmap,
        speechBubbles = speechBubbles.toList(),
        stickers = stickers.toList(),
        soundEffects = soundEffects.toList(),
        colorStyle = selectedColorStyle,
        inkingContrast = inkingContrast,
        animationMode = animationMode
    )

    fun restoreSnapshot(snapshot: ComicEditorSnapshot) {
        selectedSketch = snapshot.sketchType
        capturedSketchBitmap = snapshot.capturedBitmap
        speechBubbles.clear()
        speechBubbles.addAll(snapshot.speechBubbles)
        stickers.clear()
        stickers.addAll(snapshot.stickers)
        soundEffects.clear()
        soundEffects.addAll(snapshot.soundEffects)
        selectedColorStyle = snapshot.colorStyle
        inkingContrast = snapshot.inkingContrast
        animationMode = snapshot.animationMode
    }

    // Dialog States
    var showCameraXDialog by remember { mutableStateOf(false) }
    var showSavePanelDialog by remember { mutableStateOf(false) }
    var isSavingPanel by remember { mutableStateOf(false) }
    var showAddBubbleDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Studio Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Comic Studio 🎨",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = selectedLayout.title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Undo / Redo & Snap Photo Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo Button
                IconButton(
                    onClick = {
                        val previous = undoRedoManager.undo(takeSnapshot())
                        if (previous != null) {
                            restoreSnapshot(previous)
                        }
                    },
                    enabled = undoRedoManager.canUndo,
                    modifier = Modifier.testTag("studio_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (undoRedoManager.canUndo) MaterialTheme.colorScheme.onBackground else Color.Gray.copy(alpha = 0.4f)
                    )
                }

                // Redo Button
                IconButton(
                    onClick = {
                        val next = undoRedoManager.redo(takeSnapshot())
                        if (next != null) {
                            restoreSnapshot(next)
                        }
                    },
                    enabled = undoRedoManager.canRedo,
                    modifier = Modifier.testTag("studio_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (undoRedoManager.canRedo) MaterialTheme.colorScheme.onBackground else Color.Gray.copy(alpha = 0.4f)
                    )
                }

                // CameraX Photo Capture Shortcut Button
                Button(
                    onClick = { showCameraXDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("studio_camera_capture_button")
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Snap Sketch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Interactive Canvas Area (Takes dominant upper portion)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            ComicPanelCanvas(
                capturedBitmap = capturedSketchBitmap,
                sketchType = selectedSketch,
                colorStyle = selectedColorStyle,
                speechBubbles = speechBubbles,
                stickers = stickers,
                soundEffects = soundEffects,
                onBubblePositionChanged = { bubbleId, newX, newY ->
                    val index = speechBubbles.indexOfFirst { it.id == bubbleId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        speechBubbles[index] = speechBubbles[index].copy(xRatio = newX, yRatio = newY)
                    }
                },
                onStickerPositionChanged = { stickerId, newX, newY ->
                    val index = stickers.indexOfFirst { it.id == stickerId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        stickers[index] = stickers[index].copy(xRatio = newX, yRatio = newY)
                    }
                },
                onSfxPositionChanged = { sfxId, newX, newY ->
                    val index = soundEffects.indexOfFirst { it.id == sfxId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        soundEffects[index] = soundEffects[index].copy(xRatio = newX, yRatio = newY)
                    }
                },
                onBubbleDeleted = { id ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    speechBubbles.removeAll { it.id == id }
                },
                onStickerDeleted = { id ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    stickers.removeAll { it.id == id }
                },
                onSfxDeleted = { id ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    soundEffects.removeAll { it.id == id }
                }
            )
        }

        // Action Toolbar (Save Panel to Cloud / Share & Export)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { showSavePanelDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.weight(1f).testTag("save_panel_button")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Cloud", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    shareComicPanel(context, capturedSketchBitmap, selectedSketch)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.weight(1f).testTag("share_panel_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Export", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Tool Tabs & Palette Controls
        ScrollableTabRow(
            selectedTabIndex = selectedToolTab,
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedToolTab == 0,
                onClick = { selectedToolTab = 0 },
                text = { Text("Sketches & Camera") }
            )
            Tab(
                selected = selectedToolTab == 1,
                onClick = { selectedToolTab = 1 },
                text = { Text("Dialogue & SFX") }
            )
            Tab(
                selected = selectedToolTab == 2,
                onClick = { selectedToolTab = 2 },
                text = { Text("Stickers (${stickers.size})") }
            )
            Tab(
                selected = selectedToolTab == 3,
                onClick = { selectedToolTab = 3 },
                text = { Text("Styles & Layout") }
            )
        }

        // Active Tool Drawer Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp)
        ) {
            when (selectedToolTab) {
                0 -> {
                    // TAB 0: SKETCHES & CAMERAX
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Base Sketch Source",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { showCameraXDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CameraX Capture", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val templateSketches = listOf("Hero Action", "Cyber City", "Dragon Cave", "School Hallway", "Alien World")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(templateSketches) { sketch ->
                                FilterChip(
                                    selected = selectedSketch == sketch && capturedSketchBitmap == null,
                                    onClick = {
                                        undoRedoManager.recordSnapshot(takeSnapshot())
                                        selectedSketch = sketch
                                        capturedSketchBitmap = null
                                    },
                                    label = { Text(sketch) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (capturedSketchBitmap != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📸 CameraX Photo Active", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Clear",
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            undoRedoManager.recordSnapshot(takeSnapshot())
                                            capturedSketchBitmap = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: DIALOGUE BUBBLES & SFX
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Speech Balloons & Sound FX", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Button(
                                onClick = { showAddBubbleDialog = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Balloon", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick SFX Bar
                        Text("Add Sound Effect:", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ChildFriendlyStickers.sfxPresets) { sfx ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable {
                                            undoRedoManager.recordSnapshot(takeSnapshot())
                                            soundEffects.add(
                                                sfx.copy(
                                                    id = "sfx_${System.currentTimeMillis()}",
                                                    xRatio = 0.35f + (soundEffects.size * 0.05f),
                                                    yRatio = 0.35f
                                                )
                                            )
                                        }
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = sfx.text,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: STICKERS
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text("Tap sticker to place on comic panel:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(ChildFriendlyStickers.presets) { sticker ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clickable {
                                            undoRedoManager.recordSnapshot(takeSnapshot())
                                            stickers.add(
                                                sticker.copy(
                                                    id = "stk_${System.currentTimeMillis()}",
                                                    xRatio = 0.45f,
                                                    yRatio = 0.45f
                                                )
                                            )
                                        }
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                                    shadowElevation = 2.dp
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(sticker.emojiOrIcon, fontSize = 26.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // TAB 3: COLOR STYLES & LAYOUT
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text("Color Art Styling", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        val styles = listOf("Vibrant Webtoon", "Vintage Newsprint", "Neon Noir", "Monochrome Ink")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(styles) { style ->
                                FilterChip(
                                    selected = selectedColorStyle == style,
                                    onClick = {
                                        undoRedoManager.recordSnapshot(takeSnapshot())
                                        selectedColorStyle = style
                                    },
                                    label = { Text(style) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onBackToLayouts,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Switch Layout (${selectedLayout.title})")
                        }
                    }
                }
            }
        }
    }

    // 📸 CameraX Capture Dialog
    if (showCameraXDialog) {
        CameraXCaptureDialog(
            onDismiss = { showCameraXDialog = false },
            onPhotoCaptured = { photo ->
                undoRedoManager.recordSnapshot(takeSnapshot())
                capturedSketchBitmap = photo
                showCameraXDialog = false
                Toast.makeText(context, "Paper sketch captured with CameraX! 🎨", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Save Panel Dialog
    if (showSavePanelDialog) {
        SavePanelDialog(
            initialTitle = "Scene: $selectedSketch",
            isSaving = isSavingPanel,
            onDismiss = { showSavePanelDialog = false },
            onConfirmSave = { title ->
                isSavingPanel = true
                coroutineScope.launch {
                    try {
                        repository.saveComicPanelState(
                            title = title,
                            bitmap = capturedSketchBitmap,
                            sketchType = selectedSketch,
                            colorStyle = selectedColorStyle,
                            animationMode = animationMode,
                            bubbles = speechBubbles,
                            stickers = stickers,
                            soundEffects = soundEffects
                        )
                        Toast.makeText(context, "Panel state saved to cloud!", Toast.LENGTH_SHORT).show()
                        showSavePanelDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isSavingPanel = false
                    }
                }
            }
        )
    }

    // Add Speech Bubble Dialog
    if (showAddBubbleDialog) {
        var bubbleText by remember { mutableStateOf("Great power comes with great comics!") }
        var bubbleType by remember { mutableStateOf("Speech") }

        AlertDialog(
            onDismissRequest = { showAddBubbleDialog = false },
            title = { Text("Add Speech Balloon") },
            text = {
                Column {
                    OutlinedTextField(
                        value = bubbleText,
                        onValueChange = { bubbleText = it },
                        label = { Text("Dialogue Content") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Balloon Style:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Speech", "Shout", "Thought").forEach { type ->
                            FilterChip(
                                selected = bubbleType == type,
                                onClick = { bubbleType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (bubbleText.isNotBlank()) {
                            undoRedoManager.recordSnapshot(takeSnapshot())
                            speechBubbles.add(
                                SpeechBubbleItem(
                                    id = "bubble_${System.currentTimeMillis()}",
                                    text = bubbleText,
                                    type = bubbleType,
                                    xRatio = 0.5f,
                                    yRatio = 0.3f
                                )
                            )
                        }
                        showAddBubbleDialog = false
                    }
                ) {
                    Text("Add Balloon")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddBubbleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SavePanelDialog(
    initialTitle: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirmSave: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Comic Panel State") },
        text = {
            Column {
                Text(
                    text = "Sync your sketch, balloon layers, and styling to your cloud library.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Panel Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isSaving) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Saving to Firebase...", fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSave(title) },
                enabled = !isSaving && title.isNotBlank()
            ) {
                Text("Save to Cloud")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancel")
            }
        }
    )
}

private fun shareComicPanel(context: Context, bitmap: Bitmap?, title: String) {
    try {
        val cacheDir = File(context.cacheDir, "shared_images")
        cacheDir.mkdirs()
        val file = File(cacheDir, "comic_panel_${System.currentTimeMillis()}.png")

        val targetBitmap = bitmap ?: Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888).apply {
            val canvas = android.graphics.Canvas(this)
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.WHITE
            }
            canvas.drawRect(0f, 0f, 1080f, 1080f, paint)
        }

        FileOutputStream(file).use { out ->
            targetBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Created with ComicCraft: $title 🎨📖")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Comic Panel"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
