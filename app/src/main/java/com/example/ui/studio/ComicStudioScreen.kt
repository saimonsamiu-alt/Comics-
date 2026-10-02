package com.example.ui.studio

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.SketchEntity
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import com.example.data.repository.ComicRepository
import com.example.ui.auth.SignInPromptDialog
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicStudioScreen(
    repository: ComicRepository,
    onNavigateToFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = repository.getCurrentUserId()

    // Studio Active State
    var selectedSketch by remember { mutableStateOf("dog") } // dog, hero, dragon, or custom
    var colorStyle by remember { mutableStateOf("Vibrant Cel-Shaded") }
    var characterColor by remember { mutableStateOf(Color(0xFFFDCB6E)) }
    var auraType by remember { mutableStateOf("Sparkle") }
    var isCleanPaperEnabled by remember { mutableStateOf(true) }
    var inkingEdgeContrast by remember { mutableStateOf(0.85f) }

    // Offline-first Room Database Sketches State
    val roomSketches by repository.observeSketches().collectAsStateWithLifecycle(initialValue = emptyList())
    var activeCustomBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeSketchEntity by remember { mutableStateOf<SketchEntity?>(null) }
    var showCameraXViewfinder by remember { mutableStateOf(false) }
    var showSnapDialog by remember { mutableStateOf(false) }
    var pendingCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var pendingCaptureSource by remember { mutableStateOf("CAMERAX") }
    var pendingSketchTitle by remember { mutableStateOf("") }
    var pendingDetectedSubject by remember { mutableStateOf("Pencil Character") }

    // Camera & Gallery Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            pendingCapturedBitmap = bitmap
            pendingCaptureSource = "CAMERA"
            pendingSketchTitle = "Paper Sketch #${(System.currentTimeMillis() % 10000)}"
            pendingDetectedSubject = "Pencil Character"
            showSnapDialog = true
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCameraXViewfinder = true
        } else {
            Toast.makeText(context, "Camera permission needed to snap paper sketches", Toast.LENGTH_SHORT).show()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                if (bitmap != null) {
                    pendingCapturedBitmap = bitmap
                    pendingCaptureSource = "GALLERY"
                    pendingSketchTitle = "Paper Sketch #${(System.currentTimeMillis() % 10000)}"
                    pendingDetectedSubject = "Pencil Character"
                    showSnapDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val triggerCameraSnap = {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            showCameraXViewfinder = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val triggerGalleryPick = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    // Bubbles & Sound FX
    var selectedBubbleId by remember { mutableStateOf<String?>(null) }
    val speechBubbles = remember {
        mutableStateListOf(
            SpeechBubbleItem(
                id = "sb_init",
                text = "Drawn on notebook, now ready for adventure!",
                type = "Speech",
                xRatio = 0.12f,
                yRatio = 0.08f,
                scale = 1.0f
            )
        )
    }

    val soundEffects = remember {
        mutableStateListOf(
            SoundEffectItem(
                id = "sfx_init",
                text = "POW!",
                colorHex = "#FF5722",
                xRatio = 0.55f,
                yRatio = 0.72f
            )
        )
    }

    var selectedStickerId by remember { mutableStateOf<String?>(null) }
    val stickers = remember {
        mutableStateListOf(
            StickerItem(
                id = "sticker_star",
                emojiOrIcon = "⭐",
                label = "Star",
                category = "Power",
                xRatio = 0.72f,
                yRatio = 0.18f,
                scale = 1.1f
            )
        )
    }

    // Animation Controls
    var isAnimating by remember { mutableStateOf(false) }
    var animationMode by remember { mutableStateOf("Camera Pan & Zoom") }
    var showShareExportDialog by remember { mutableStateOf(false) }
    var showSavePanelDialog by remember { mutableStateOf(false) }
    var isSavingPanel by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var showSignInPrompt by remember { mutableStateOf(false) }

    // Active Editor Tool Tab: 0 -> Character & Inks, 1 -> Dialogues & SFX, 2 -> Animation Motion
    var selectedToolTab by remember { mutableIntStateOf(0) }

    // Undo / Redo Stack Manager
    val undoRedoManager = remember { UndoRedoManager() }

    fun takeSnapshot(): ComicEditorSnapshot {
        return ComicEditorSnapshot(
            sketchType = selectedSketch,
            customBitmap = activeCustomBitmap,
            activeSketchEntity = activeSketchEntity,
            colorStyle = colorStyle,
            characterColor = characterColor,
            auraType = auraType,
            inkingEdgeContrast = inkingEdgeContrast,
            isCleanPaperEnabled = isCleanPaperEnabled,
            speechBubbles = speechBubbles.toList(),
            stickers = stickers.toList(),
            soundEffects = soundEffects.toList(),
            animationMode = animationMode
        )
    }

    fun restoreSnapshot(snapshot: ComicEditorSnapshot) {
        selectedSketch = snapshot.sketchType
        activeCustomBitmap = snapshot.customBitmap
        activeSketchEntity = snapshot.activeSketchEntity
        colorStyle = snapshot.colorStyle
        characterColor = snapshot.characterColor
        auraType = snapshot.auraType
        inkingEdgeContrast = snapshot.inkingEdgeContrast
        isCleanPaperEnabled = snapshot.isCleanPaperEnabled
        animationMode = snapshot.animationMode

        speechBubbles.clear()
        speechBubbles.addAll(snapshot.speechBubbles)

        stickers.clear()
        stickers.addAll(snapshot.stickers)

        soundEffects.clear()
        soundEffects.addAll(snapshot.soundEffects)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("comic_studio_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = "Comic Studio & Sketch AI",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Transform notebook drawings into animated comics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo Button
                IconButton(
                    onClick = {
                        val prev = undoRedoManager.undo(takeSnapshot())
                        if (prev != null) {
                            restoreSnapshot(prev)
                            Toast.makeText(context, "Undo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = undoRedoManager.canUndo,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (undoRedoManager.canUndo) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .testTag("undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo previous action",
                        tint = if (undoRedoManager.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Redo Button
                IconButton(
                    onClick = {
                        val next = undoRedoManager.redo(takeSnapshot())
                        if (next != null) {
                            restoreSnapshot(next)
                            Toast.makeText(context, "Redo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = undoRedoManager.canRedo,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (undoRedoManager.canRedo) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            CircleShape
                        )
                        .testTag("redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo next action",
                        tint = if (undoRedoManager.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Play / Pause Animation Button
                IconButton(
                    onClick = { isAnimating = !isAnimating },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isAnimating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("animation_play_button")
                ) {
                    Icon(
                        imageVector = if (isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Toggle Animation",
                        tint = if (isAnimating) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Live Canvas Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.1f)
        ) {
            ComicPanelCanvas(
                modifier = Modifier.fillMaxSize(),
                sketchType = selectedSketch,
                colorStyle = colorStyle,
                characterColor = characterColor,
                auraType = auraType,
                speechBubbles = speechBubbles,
                soundEffects = soundEffects,
                isAnimating = isAnimating,
                animationMode = animationMode,
                customBitmap = activeCustomBitmap,
                inkingEdgeContrast = inkingEdgeContrast,
                isCleanPaperEnabled = isCleanPaperEnabled,
                selectedBubbleId = selectedBubbleId,
                onSelectBubble = { selectedBubbleId = it },
                onBubblePositionChanged = { bubbleId, newX, newY ->
                    val index = speechBubbles.indexOfFirst { it.id == bubbleId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        speechBubbles[index] = speechBubbles[index].copy(xRatio = newX, yRatio = newY)
                    }
                },
                onBubbleScaleChanged = { bubbleId, newScale ->
                    val index = speechBubbles.indexOfFirst { it.id == bubbleId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        speechBubbles[index] = speechBubbles[index].copy(scale = newScale)
                    }
                },
                onBubbleTextChanged = { bubbleId, newText ->
                    val index = speechBubbles.indexOfFirst { it.id == bubbleId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        speechBubbles[index] = speechBubbles[index].copy(text = newText)
                    }
                },
                onBubbleDeleted = { bubbleId ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    speechBubbles.removeAll { it.id == bubbleId }
                    if (selectedBubbleId == bubbleId) {
                        selectedBubbleId = null
                    }
                },
                stickers = stickers,
                selectedStickerId = selectedStickerId,
                onSelectSticker = { selectedStickerId = it },
                onStickerPositionChanged = { stickerId, newX, newY ->
                    val index = stickers.indexOfFirst { it.id == stickerId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        stickers[index] = stickers[index].copy(xRatio = newX, yRatio = newY)
                    }
                },
                onStickerScaleChanged = { stickerId, newScale ->
                    val index = stickers.indexOfFirst { it.id == stickerId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        stickers[index] = stickers[index].copy(scale = newScale)
                    }
                },
                onStickerRotateChanged = { stickerId, newRot ->
                    val index = stickers.indexOfFirst { it.id == stickerId }
                    if (index >= 0) {
                        undoRedoManager.recordSnapshot(takeSnapshot())
                        stickers[index] = stickers[index].copy(rotation = newRot)
                    }
                },
                onStickerDeleted = { stickerId ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    stickers.removeAll { it.id == stickerId }
                    if (selectedStickerId == stickerId) {
                        selectedStickerId = null
                    }
                }
            )

            // Animation Mode Badge
            if (isAnimating) {
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MOTION: $animationMode",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Paper Sketch Status Banner (Room DB)
        if (activeSketchEntity != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth().testTag("active_sketch_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (activeSketchEntity!!.captureSource == "CAMERA") Icons.Default.CameraAlt else Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = activeSketchEntity!!.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Text(
                            text = "Room DB: ${activeSketchEntity!!.status} • Subject: ${activeSketchEntity!!.detectedSubject}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val updated = activeSketchEntity!!.copy(
                                        status = "AI_ENHANCED",
                                        inkingContrast = inkingEdgeContrast,
                                        colorStyle = colorStyle,
                                        characterColorHex = String.format("#%06X", (0xFFFFFF and characterColor.toArgb())),
                                        auraType = auraType
                                    )
                                    repository.updateSketch(updated)
                                    activeSketchEntity = updated
                                    Toast.makeText(context, "AI enhancements saved to local Room DB!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("save_ai_enhanced_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save AI", fontSize = 12.sp)
                        }

                        IconButton(
                            onClick = {
                                activeCustomBitmap = null
                                activeSketchEntity = null
                                selectedSketch = "dog"
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Close Custom Sketch")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Action Toolbar (Export & Share / Cloud Save / Motion Reel / Publish)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showShareExportDialog = true },
                    modifier = Modifier.weight(1f).testTag("share_panel_image_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export & Share", maxLines = 1)
                }

                Button(
                    onClick = { showSavePanelDialog = true },
                    modifier = Modifier.weight(1f).testTag("save_panel_cloud_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cloud Save", maxLines = 1)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f).testTag("export_animation_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Motion Reel", maxLines = 1)
                }

                Button(
                    onClick = {
                        if (currentUserId == null) {
                            showSignInPrompt = true
                        } else {
                            showPublishDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f).testTag("publish_comic_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Publish Comic", maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Tabs
        PrimaryTabRow(selectedTabIndex = selectedToolTab) {
            Tab(
                selected = selectedToolTab == 0,
                onClick = { selectedToolTab = 0 },
                text = { Text("Inks & Art") }
            )
            Tab(
                selected = selectedToolTab == 1,
                onClick = { selectedToolTab = 1 },
                text = { Text("Dialogues") }
            )
            Tab(
                selected = selectedToolTab == 2,
                onClick = { selectedToolTab = 2 },
                text = { Text("Stickers") }
            )
            Tab(
                selected = selectedToolTab == 3,
                onClick = { selectedToolTab = 3 },
                text = { Text("Motion") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedToolTab) {
            0 -> CharacterInksToolPanel(
                selectedSketch = selectedSketch,
                onSelectSketch = {
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    selectedSketch = it
                    activeCustomBitmap = null
                    activeSketchEntity = null
                },
                activeSketchEntity = activeSketchEntity,
                roomSketches = roomSketches,
                onSnapCamera = triggerCameraSnap,
                onPickGallery = triggerGalleryPick,
                onSelectRoomSketch = { sketch ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    val file = File(sketch.imagePath)
                    if (file.exists()) {
                        val bmp = BitmapFactory.decodeFile(file.absolutePath)
                        activeCustomBitmap = bmp
                        activeSketchEntity = sketch
                        selectedSketch = "custom"
                        inkingEdgeContrast = sketch.inkingContrast
                        colorStyle = sketch.colorStyle
                        auraType = sketch.auraType
                    } else {
                        Toast.makeText(context, "Sketch image file not found on disk", Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteRoomSketch = { sketchId ->
                    coroutineScope.launch {
                        repository.deleteSketch(sketchId)
                        if (activeSketchEntity?.id == sketchId) {
                            activeCustomBitmap = null
                            activeSketchEntity = null
                            selectedSketch = "dog"
                        }
                        Toast.makeText(context, "Sketch deleted from Room DB", Toast.LENGTH_SHORT).show()
                    }
                },
                colorStyle = colorStyle,
                onSelectColorStyle = {
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    colorStyle = it
                },
                characterColor = characterColor,
                onSelectCharacterColor = {
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    characterColor = it
                },
                auraType = auraType,
                onSelectAura = {
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    auraType = it
                },
                inkingEdgeContrast = inkingEdgeContrast,
                onInkingEdgeChanged = { inkingEdgeContrast = it },
                isCleanPaperEnabled = isCleanPaperEnabled,
                onToggleCleanPaper = {
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    isCleanPaperEnabled = it
                }
            )
            1 -> DialoguesAndSfxPanel(
                speechBubbles = speechBubbles,
                onAddBubble = { text, type ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    val newId = UUID.randomUUID().toString()
                    speechBubbles.add(
                        SpeechBubbleItem(
                            id = newId,
                            text = text,
                            type = type,
                            xRatio = 0.35f,
                            yRatio = 0.15f,
                            scale = 1.0f
                        )
                    )
                    selectedBubbleId = newId
                },
                onRemoveBubble = { id ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    speechBubbles.removeAll { it.id == id }
                    if (selectedBubbleId == id) {
                        selectedBubbleId = null
                    }
                },
                soundEffects = soundEffects,
                onAddSfx = { text, hex ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    soundEffects.add(
                        SoundEffectItem(
                            id = UUID.randomUUID().toString(),
                            text = text,
                            colorHex = hex,
                            xRatio = 0.5f,
                            yRatio = 0.5f
                        )
                    )
                },
                onRemoveSfx = { id ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    soundEffects.removeAll { it.id == id }
                }
            )
            2 -> StickersToolPanel(
                stickers = stickers,
                onAddSticker = { emoji, label, category ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    val newId = UUID.randomUUID().toString()
                    stickers.add(
                        StickerItem(
                            id = newId,
                            emojiOrIcon = emoji,
                            label = label,
                            category = category,
                            xRatio = 0.5f,
                            yRatio = 0.4f,
                            scale = 1.1f
                        )
                    )
                    selectedStickerId = newId
                },
                onRemoveSticker = { id ->
                    undoRedoManager.recordSnapshot(takeSnapshot())
                    stickers.removeAll { it.id == id }
                    if (selectedStickerId == id) {
                        selectedStickerId = null
                    }
                }
            )
            3 -> MotionCameraPanel(
                animationMode = animationMode,
                onSelectAnimationMode = {
                    animationMode = it
                    isAnimating = true
                },
                isAnimating = isAnimating,
                onTogglePlay = { isAnimating = !isAnimating }
            )
        }
    }

    // Export Animation Dialog
    if (showExportDialog) {
        ExportAnimationDialog(
            animationMode = animationMode,
            sketchType = selectedSketch,
            onDismiss = { showExportDialog = false },
            onExportSuccess = {
                showExportDialog = false
                Toast.makeText(context, "Animation exported! Saved to Motion Comics reel.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Save Comic Panel State to Firestore Dialog
    if (showSavePanelDialog) {
        val defaultTitle = activeSketchEntity?.title ?: "The Tale of ${selectedSketch.replaceFirstChar { it.uppercase() }}"
        SavePanelDialog(
            initialTitle = defaultTitle,
            sketchType = selectedSketch,
            colorStyle = colorStyle,
            speechBubblesCount = speechBubbles.size,
            stickersCount = stickers.size,
            isSaving = isSavingPanel,
            onDismiss = { showSavePanelDialog = false },
            onConfirmSave = { title ->
                if (currentUserId == null) {
                    showSavePanelDialog = false
                    showSignInPrompt = true
                } else {
                    isSavingPanel = true
                    coroutineScope.launch {
                        try {
                            val panelId = repository.saveComicPanelState(
                                title = title,
                                bitmap = activeCustomBitmap,
                                sketchType = selectedSketch,
                                colorStyle = colorStyle,
                                characterColorHex = String.format("#%06X", (0xFFFFFF and characterColor.toArgb())),
                                auraType = auraType,
                                inkingEdgeContrast = inkingEdgeContrast,
                                isCleanPaperEnabled = isCleanPaperEnabled,
                                animationMode = animationMode,
                                speechBubbles = speechBubbles.toList(),
                                stickers = stickers.toList(),
                                soundEffects = soundEffects.toList()
                            )
                            Toast.makeText(context, "Comic panel saved to Firestore!", Toast.LENGTH_SHORT).show()
                            showSavePanelDialog = false
                        } catch (e: Exception) {
                            Toast.makeText(context, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isSavingPanel = false
                        }
                    }
                }
            }
        )
    }

    // Share / Export Finished Panel Image Dialog
    if (showShareExportDialog) {
        val defaultTitle = activeSketchEntity?.title ?: "Comic Panel: ${selectedSketch.replaceFirstChar { it.uppercase() }}"
        ShareExportImageDialog(
            initialTitle = defaultTitle,
            sketchType = selectedSketch,
            speechBubblesCount = speechBubbles.size,
            stickersCount = stickers.size,
            onDismiss = { showShareExportDialog = false },
            onSaveToGallery = { title ->
                coroutineScope.launch {
                    try {
                        val renderedBitmap = ComicPanelExporter.renderComicPanel(
                            context = context,
                            sketchType = selectedSketch,
                            customBitmap = activeCustomBitmap,
                            colorStyle = colorStyle,
                            characterColor = characterColor,
                            auraType = auraType,
                            inkingEdgeContrast = inkingEdgeContrast,
                            isCleanPaperEnabled = isCleanPaperEnabled,
                            speechBubbles = speechBubbles.toList(),
                            stickers = stickers.toList(),
                            soundEffects = soundEffects.toList(),
                            width = 1200,
                            height = 1200
                        )
                        val uri = ComicPanelExporter.saveToGallery(context, renderedBitmap, title)
                        if (uri != null) {
                            Toast.makeText(context, "Comic panel saved to Gallery (Pictures/ComicCraft)!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Exported image saved to gallery!", Toast.LENGTH_SHORT).show()
                        }
                        showShareExportDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onShare = { title ->
                coroutineScope.launch {
                    try {
                        val renderedBitmap = ComicPanelExporter.renderComicPanel(
                            context = context,
                            sketchType = selectedSketch,
                            customBitmap = activeCustomBitmap,
                            colorStyle = colorStyle,
                            characterColor = characterColor,
                            auraType = auraType,
                            inkingEdgeContrast = inkingEdgeContrast,
                            isCleanPaperEnabled = isCleanPaperEnabled,
                            speechBubbles = speechBubbles.toList(),
                            stickers = stickers.toList(),
                            soundEffects = soundEffects.toList(),
                            width = 1200,
                            height = 1200
                        )
                        ComicPanelExporter.shareComicPanel(context, renderedBitmap, title)
                        showShareExportDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Publish / Share Dialog
    if (showPublishDialog) {
        val defaultSketchName = activeSketchEntity?.title ?: selectedSketch
        val customImagePath = activeSketchEntity?.imagePath ?: "sketch_$selectedSketch"
        PublishComicDialog(
            sketchType = defaultSketchName,
            colorStyle = colorStyle,
            speechBubbles = speechBubbles,
            onDismiss = { showPublishDialog = false },
            onPublish = { title, genre, synopsis, isFeedPost ->
                coroutineScope.launch {
                    try {
                        if (isFeedPost) {
                            repository.shareFeedPost(
                                title = title,
                                description = synopsis,
                                imageUrl = customImagePath,
                                characterDesign = "Style: $colorStyle, Character: $defaultSketchName (${activeSketchEntity?.captureSource ?: "Preset"})"
                            )
                            Toast.makeText(context, "Shared to Community Feed!", Toast.LENGTH_SHORT).show()
                            onNavigateToFeed()
                        } else {
                            repository.publishComic(
                                title = title,
                                synopsis = synopsis,
                                genre = genre,
                                coverUrl = customImagePath,
                                chapterTitle = "Episode 1: The Sketch Awakens",
                                panelsJson = "[]"
                            )
                            Toast.makeText(context, "Comic series published successfully!", Toast.LENGTH_LONG).show()
                        }
                        showPublishDialog = false
                    } catch (e: Exception) {
                        Toast.makeText(context, "Publish failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Google Sign-In prompt
    if (showSignInPrompt) {
        SignInPromptDialog(
            actionTitle = "Sign In to Publish Comics",
            actionDescription = "Connect your Google account to publish your hand-drawn creations, save bookmarks, and sync across all your devices!",
            onDismiss = { showSignInPrompt = false },
            onAuthSuccess = {
                showSignInPrompt = false
                showPublishDialog = true
            }
        )
    }

    // Snap Paper Sketch Confirmation Dialog (Room DB save)
    if (showSnapDialog && pendingCapturedBitmap != null) {
        SnapSketchConfirmationDialog(
            bitmap = pendingCapturedBitmap!!,
            captureSource = pendingCaptureSource,
            initialTitle = pendingSketchTitle,
            onDismiss = {
                showSnapDialog = false
                pendingCapturedBitmap = null
            },
            onConfirmSave = { title, detectedSubject ->
                coroutineScope.launch {
                    try {
                        val dir = File(context.filesDir, "sketches").apply { if (!exists()) mkdirs() }
                        val file = File(dir, "sketch_${UUID.randomUUID()}.jpg")
                        FileOutputStream(file).use { out ->
                            pendingCapturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 92, out)
                        }

                        val savedSketch = repository.saveCapturedSketch(
                            title = title.ifBlank { "Paper Sketch" },
                            imagePath = file.absolutePath,
                            captureSource = pendingCaptureSource,
                            detectedSubject = detectedSubject,
                            inkingContrast = inkingEdgeContrast,
                            colorStyle = colorStyle,
                            characterColorHex = String.format("#%06X", (0xFFFFFF and characterColor.toArgb())),
                            auraType = auraType
                        )

                        activeSketchEntity = savedSketch
                        activeCustomBitmap = pendingCapturedBitmap
                        selectedSketch = "custom"
                        showSnapDialog = false
                        pendingCapturedBitmap = null
                        Toast.makeText(context, "Paper sketch saved to Room database!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to save sketch: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // CameraX Direct Viewfinder & Capture
    if (showCameraXViewfinder) {
        CameraXCaptureDialog(
            onDismiss = { showCameraXViewfinder = false },
            onPhotoCaptured = { bitmap ->
                pendingCapturedBitmap = bitmap
                pendingCaptureSource = "CAMERAX"
                pendingSketchTitle = "Paper Sketch #${(System.currentTimeMillis() % 10000)}"
                pendingDetectedSubject = "Pencil Character"
                showCameraXViewfinder = false
                showSnapDialog = true
            }
        )
    }
}

@Composable
private fun CharacterInksToolPanel(
    selectedSketch: String,
    onSelectSketch: (String) -> Unit,
    activeSketchEntity: SketchEntity?,
    roomSketches: List<SketchEntity>,
    onSnapCamera: () -> Unit,
    onPickGallery: () -> Unit,
    onSelectRoomSketch: (SketchEntity) -> Unit,
    onDeleteRoomSketch: (String) -> Unit,
    colorStyle: String,
    onSelectColorStyle: (String) -> Unit,
    characterColor: Color,
    onSelectCharacterColor: (Color) -> Unit,
    auraType: String,
    onSelectAura: (String) -> Unit,
    inkingEdgeContrast: Float,
    onInkingEdgeChanged: (Float) -> Unit,
    isCleanPaperEnabled: Boolean,
    onToggleCleanPaper: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Camera & Upload Section
            Text(
                text = "1. Snap Paper Sketch / Notebook (Camera Upload)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Photograph paper drawings to store in local Room DB for AI comic transformation",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSnapCamera,
                    modifier = Modifier.weight(1f).testTag("snap_camera_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Snap Sketch")
                }

                OutlinedButton(
                    onClick = onPickGallery,
                    modifier = Modifier.weight(1f).testTag("pick_photo_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pick Photo")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Room DB Stored Sketches
            Text(
                text = "Captured Paper Sketches (Room DB: ${roomSketches.size})",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (roomSketches.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No paper sketches in Room DB yet. Tap 'Snap Sketch' above to photograph your notebook artwork!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    roomSketches.forEach { sketch ->
                        val isSelected = activeSketchEntity?.id == sketch.id
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { onSelectRoomSketch(sketch) }
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                // Thumbnail
                                val file = File(sketch.imagePath)
                                val bitmap = remember(sketch.imagePath) {
                                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.DarkGray.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = sketch.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
                                    }

                                    // Source badge
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp)
                                    ) {
                                        Text(
                                            text = sketch.captureSource,
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = sketch.title,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (sketch.status == "AI_ENHANCED") "✨ AI Done" else "✏️ Ready",
                                        fontSize = 10.sp,
                                        color = if (sketch.status == "AI_ENHANCED") Color(0xFF2E7D32) else Color(0xFFE65100),
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    IconButton(
                                        onClick = { onDeleteRoomSketch(sketch.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete sketch",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Or Built-in Starter Presets",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "dog" to "Pencil Doggo (Khata)",
                    "hero" to "Anime Fighter (Manga)",
                    "dragon" to "Fantasy Winged Beast"
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = selectedSketch == id && activeSketchEntity == null,
                        onClick = { onSelectSketch(id) },
                        label = { Text(label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "2. Comic Art Style & Color Grading",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Vibrant Cel-Shaded",
                    "Cyberpunk Glow",
                    "Vintage Retro Comic",
                    "Pastel Fantasy"
                ).forEach { style ->
                    FilterChip(
                        selected = colorStyle == style,
                        onClick = { onSelectColorStyle(style) },
                        label = { Text(style) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "3. Character Color Palette",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    Color(0xFFFDCB6E) to "Golden",
                    Color(0xFFFF5722) to "Orange Flame",
                    Color(0xFF6C5CE7) to "Indigo Blade",
                    Color(0xFF00CEC9) to "Electric Cyan",
                    Color(0xFFE84393) to "Neon Magenta",
                    Color(0xFF2D3436) to "Dark Obsidian"
                ).forEach { (color, _) ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (characterColor == color) 3.5.dp else 1.dp,
                                color = if (characterColor == color) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { onSelectCharacterColor(color) }
                    ) {
                        if (characterColor == color) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "4. Energy Aura / FX",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("None", "Sparkle", "Flame", "Lightning").forEach { aura ->
                    FilterChip(
                        selected = auraType == aura,
                        onClick = { onSelectAura(aura) },
                        label = { Text(aura) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "5. Clean Paper & Inking Contrast (AI Prep)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clean Paper Texture Matting",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Whitens notebook grid & shadows behind drawing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isCleanPaperEnabled,
                    onCheckedChange = onToggleCleanPaper
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ink Vectorizer Edge Contrast",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(inkingEdgeContrast * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Slider(
                value = inkingEdgeContrast,
                onValueChange = onInkingEdgeChanged,
                valueRange = 0.2f..1.0f
            )
        }
    }
}

@Composable
private fun DialoguesAndSfxPanel(
    speechBubbles: List<SpeechBubbleItem>,
    onAddBubble: (String, String) -> Unit,
    onRemoveBubble: (String) -> Unit,
    soundEffects: List<SoundEffectItem>,
    onAddSfx: (String, String) -> Unit,
    onRemoveSfx: (String) -> Unit
) {
    var newBubbleText by remember { mutableStateOf("") }
    var newBubbleType by remember { mutableStateOf("Speech") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "💡 Interactive Bubble: Tap any speech bubble on the artwork to type text into it! Drag to move anywhere, or pinch / drag the corner handle to resize.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Text(
                text = "Add Dialogue Balloon",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newBubbleText,
                onValueChange = { newBubbleText = it },
                label = { Text("Character dialogue / thought") },
                placeholder = { Text("e.g. Woof! Who stole my golden bone?") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Speech", "Thought", "Shout", "Whisper").forEach { type ->
                    FilterChip(
                        selected = newBubbleType == type,
                        onClick = { newBubbleType = type },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (newBubbleText.isNotBlank()) {
                        onAddBubble(newBubbleText, newBubbleType)
                        newBubbleText = ""
                    }
                },
                enabled = newBubbleText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Bubble to Panel (Drag to Reposition)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Bubbles list
            if (speechBubbles.isNotEmpty()) {
                Text(
                    text = "Active Dialogue Bubbles (${speechBubbles.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                speechBubbles.forEach { bubble ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${bubble.type}] ${bubble.text}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onRemoveBubble(bubble.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete bubble", tint = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Comic Action Sound Effects (SFX)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "POW!" to "#FF5722",
                    "BAM!" to "#FDCB6E",
                    "WHOOSH!" to "#00CEC9",
                    "KABOOM!" to "#E84393",
                    "SNIFF!" to "#6C5CE7",
                    "GULP!" to "#00B894"
                ).forEach { (sfxText, colorHex) ->
                    Button(
                        onClick = { onAddSfx(sfxText, colorHex) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(android.graphics.Color.parseColor(colorHex))
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(sfxText, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StickersToolPanel(
    stickers: List<StickerItem>,
    onAddSticker: (String, String, String) -> Unit,
    onRemoveSticker: (String) -> Unit
) {
    Column {
        StickerPickerTray(onAddSticker = onAddSticker)

        if (stickers.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Active Stickers on Panel (${stickers.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    stickers.forEach { sticker ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = sticker.emojiOrIcon, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${sticker.label} (${(sticker.scale * 100).toInt()}%)",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                            }
                            IconButton(onClick = { onRemoveSticker(sticker.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Sticker", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MotionCameraPanel(
    animationMode: String,
    onSelectAnimationMode: (String) -> Unit,
    isAnimating: Boolean,
    onTogglePlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Motion Comic Animation Modes",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Turns static drawings into dynamic animated scenes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            listOf(
                "Camera Pan & Zoom" to "Slow cinematic camera glide into the hero with depth",
                "Action Shake" to "Impact screen shake and comic hit vibration effect",
                "Pulse Life" to "Breathing character motion with floating energy dialogue"
            ).forEach { (mode, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectAnimationMode(mode) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (animationMode == mode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (animationMode == mode) MaterialTheme.colorScheme.primary else Color.Gray,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = mode, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onTogglePlay,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(if (isAnimating) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isAnimating) "Pause Live Motion" else "Play Animation")
            }
        }
    }
}

@Composable
fun ExportAnimationDialog(
    animationMode: String,
    sketchType: String,
    onDismiss: () -> Unit,
    onExportSuccess: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Export Motion Comic",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Exporting animation loop using mode '$animationMode' at 60 FPS (Looping GIF / MP4 Motion Comic)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    ComicPanelCanvas(
                        modifier = Modifier.fillMaxSize(),
                        sketchType = sketchType,
                        colorStyle = "Vibrant Cel-Shaded",
                        isAnimating = true,
                        animationMode = animationMode
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onExportSuccess,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save / Export Motion Comic")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun PublishComicDialog(
    sketchType: String,
    colorStyle: String,
    speechBubbles: List<SpeechBubbleItem>,
    onDismiss: () -> Unit,
    onPublish: (title: String, genre: String, synopsis: String, isFeedPost: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("The Tale of ${sketchType.replaceFirstChar { it.uppercase() }}") }
    var synopsis by remember { mutableStateOf("A vibrant comic created from notebook sketch with ComicCraft studio.") }
    var genre by remember { mutableStateOf("Action & Comedy") }
    var publishType by remember { mutableStateOf("feed") } // "feed" or "comic"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Publish Creation",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = publishType == "feed",
                        onClick = { publishType = "feed" },
                        label = { Text("Community Social Feed") }
                    )
                    FilterChip(
                        selected = publishType == "comic",
                        onClick = { publishType = "comic" },
                        label = { Text("Publish Full Comic") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Comic / Character Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = synopsis,
                    onValueChange = { synopsis = it },
                    label = { Text("Description & Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onPublish(title, genre, synopsis, publishType == "feed")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (publishType == "feed") "Share to Feed" else "Publish Series")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun SnapSketchConfirmationDialog(
    bitmap: Bitmap,
    captureSource: String,
    initialTitle: String,
    onDismiss: () -> Unit,
    onConfirmSave: (title: String, detectedSubject: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var detectedSubject by remember { mutableStateOf("Pencil Character") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (captureSource == "CAMERA") Icons.Default.CameraAlt else Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (captureSource == "CAMERA") "Snap Captured!" else "Photo Loaded",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Preview captured photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.05f))
                        .border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Paper Drawing",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Sketch Title / Caption") },
                    placeholder = { Text("e.g. Notebook Hero Lineart") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Detected Artwork Subject",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Pencil Character", "Anime Fighter", "Fantasy Beast", "Comic Scene").forEach { subject ->
                        FilterChip(
                            selected = detectedSubject == subject,
                            onClick = { detectedSubject = subject },
                            label = { Text(subject, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stored offline in Room DB ('sketches') with offline caching & AI enhancement prep.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirmSave(title, detectedSubject) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Room DB & Open in Studio")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Retake / Cancel")
                }
            }
        }
    }
}

@Composable
fun SavePanelDialog(
    initialTitle: String,
    sketchType: String,
    colorStyle: String,
    speechBubblesCount: Int,
    stickersCount: Int,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirmSave: (title: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }

    Dialog(onDismissRequest = { if (!isSaving) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Save Panel to Cloud",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Saves your current comic panel state—including base image, lineart settings, speech bubbles, and stickers—to Firebase Firestore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Panel Scene Title") },
                    placeholder = { Text("e.g. Hero's Awakening") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Panel summary chip list
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Included in Firestore payload:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Base Sketch: $sketchType ($colorStyle)\n• Speech Bubbles: $speechBubblesCount dialogue balloons\n• Stickers: $stickersCount child-friendly props",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onConfirmSave(title) },
                    enabled = !isSaving && title.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving to Firestore...")
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm & Save to Firestore")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun ShareExportImageDialog(
    initialTitle: String,
    sketchType: String,
    speechBubblesCount: Int,
    stickersCount: Int,
    onDismiss: () -> Unit,
    onSaveToGallery: (title: String) -> Unit,
    onShare: (title: String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var isProcessing by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isProcessing) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Export Finished Panel",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Export your finished comic panel as a high-quality 1200x1200px PNG image with all speech bubbles, stickers, and lineart styling.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Artwork / Panel Title") },
                    placeholder = { Text("e.g. Hero's Final Stand") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quality Badge Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export Format: 1200x1200 HD PNG (Lossless)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Base Sketch: $sketchType\n• Speech Bubbles: $speechBubblesCount dialogue elements\n• Stickers: $stickersCount pop-art props\n• Location: Pictures/ComicCraft",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Rendering high-res panel...", fontSize = 13.sp)
                    }
                } else {
                    // Save to Gallery Button
                    Button(
                        onClick = {
                            isProcessing = true
                            onSaveToGallery(title)
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_save_gallery_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Device Gallery")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // System Share Button
                    OutlinedButton(
                        onClick = {
                            isProcessing = true
                            onShare(title)
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_share_sheet_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share via App (Instagram / Discord)")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    enabled = !isProcessing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}



