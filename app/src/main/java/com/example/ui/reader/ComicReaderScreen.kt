package com.example.ui.reader

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Bookmark
import com.example.data.model.Comic
import com.example.data.model.ComicPanelItem
import com.example.data.model.Comment
import com.example.data.repository.ComicRepository
import com.example.data.sample.SampleComicsData
import com.example.ui.auth.SignInPromptDialog
import com.example.ui.studio.ComicPanelCanvas
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicReaderScreen(
    comicId: String,
    repository: ComicRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = repository.getCurrentUserId()

    var comic by remember {
        mutableStateOf(SampleComicsData.starterComics.find { it.id == comicId } ?: SampleComicsData.starterComics.first())
    }
    var isBookmarked by remember { mutableStateOf(false) }
    var showSignInPrompt by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var isMotionComicActive by remember { mutableStateOf(false) }

    // Comments list
    val comments = remember { mutableStateListOf<Comment>() }
    var commentInput by remember { mutableStateOf("") }

    // Active reading chapter panels
    val panels = remember {
        if (comic.id == "comic_doggo_detective") {
            SampleComicsData.starterPanelsForDoggo
        } else {
            listOf(
                ComicPanelItem(
                    id = "panel_general_1",
                    imageUrl = comic.coverUrl ?: "hero_banner",
                    sketchTitle = comic.title,
                    colorStyle = "Cyberpunk Glow",
                    speechBubbles = listOf(
                        com.example.data.model.SpeechBubbleItem(
                            id = "sb_g1",
                            text = "Episode 1: The Adventure Begins...",
                            type = "Speech",
                            xRatio = 0.5f,
                            yRatio = 0.15f
                        )
                    ),
                    soundEffects = listOf(
                        com.example.data.model.SoundEffectItem(
                            id = "sfx_g1",
                            text = "WHOOSH!",
                            colorHex = "#00CEC9",
                            xRatio = 0.5f,
                            yRatio = 0.7f
                        )
                    )
                )
            )
        }
    }

    // Observe bookmarks if logged in
    LaunchedEffect(currentUserId, comicId) {
        if (currentUserId != null) {
            repository.observeUserBookmarks(currentUserId).collectLatest { bookmarks ->
                isBookmarked = bookmarks.any { it.comicId == comicId }
            }
        }
    }

    // Observe comments
    LaunchedEffect(comicId) {
        repository.observeComments(comicId).collectLatest { commentList ->
            comments.clear()
            comments.addAll(commentList)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = comic.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "by ${comic.authorName} • ★ ${String.format("%.1f", comic.ratingAverage)} (${comic.ratingCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Motion Comic toggle
                    IconButton(
                        onClick = {
                            isMotionComicActive = !isMotionComicActive
                            Toast.makeText(
                                context,
                                if (isMotionComicActive) "Motion Comic Mode: ON" else "Motion Comic Mode: OFF",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = "Toggle Motion Comic",
                            tint = if (isMotionComicActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Bookmark
                    IconButton(
                        onClick = {
                            if (currentUserId == null) {
                                showSignInPrompt = true
                            } else {
                                coroutineScope.launch {
                                    if (isBookmarked) {
                                        repository.removeBookmark(comic.id)
                                        isBookmarked = false
                                        Toast.makeText(context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show()
                                    } else {
                                        repository.saveBookmark(
                                            Bookmark(
                                                userId = currentUserId,
                                                comicId = comic.id,
                                                comicTitle = comic.title,
                                                coverUrl = comic.coverUrl,
                                                lastReadChapter = 1,
                                                progressPercent = 100
                                            )
                                        )
                                        isBookmarked = true
                                        Toast.makeText(context, "Saved to Bookmarks & Cloud Synced!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("bookmark_button")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Rate
                    IconButton(onClick = { showRatingDialog = true }) {
                        Icon(Icons.Default.Star, contentDescription = "Rate", tint = Color(0xFFFDCB6E))
                    }

                    // Comments
                    IconButton(onClick = { showCommentsSheet = true }) {
                        Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = "Comments")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F101A))
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("comic_reader_view")
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Chapter 1: The Origin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isMotionComicActive) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "LIVE MOTION",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = comic.synopsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Render comic panels
            items(panels) { panel ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.05f)
                        .padding(vertical = 8.dp)
                ) {
                    ComicPanelCanvas(
                        modifier = Modifier.fillMaxSize(),
                        sketchType = if (comic.id == "comic_doggo_detective") "dog" else if (comic.id == "comic_phantom_dragon") "dragon" else "hero",
                        colorStyle = panel.colorStyle,
                        speechBubbles = panel.speechBubbles,
                        soundEffects = panel.soundEffects,
                        isAnimating = isMotionComicActive,
                        animationMode = "Camera Pan & Zoom"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "End of Chapter 1",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { showRatingDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Rate Comic")
                        }

                        Button(
                            onClick = { showCommentsSheet = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Discuss (${comments.size})")
                        }
                    }
                }
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        var selectedScore by remember { mutableIntStateOf(5) }
        Dialog(onDismissRequest = { showRatingDialog = false }) {
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
                        text = "Rate ${comic.title}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { selectedScore = star }) {
                                Icon(
                                    imageVector = if (star <= selectedScore) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = Color(0xFFFDCB6E),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (currentUserId == null) {
                                showRatingDialog = false
                                showSignInPrompt = true
                            } else {
                                coroutineScope.launch {
                                    try {
                                        repository.rateComic(
                                            comicId = comic.id,
                                            score = selectedScore,
                                            currentAvg = comic.ratingAverage,
                                            currentCount = comic.ratingCount
                                        )
                                        Toast.makeText(context, "Thanks for rating $selectedScore stars!", Toast.LENGTH_SHORT).show()
                                        showRatingDialog = false
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Rating failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Submit Rating")
                    }
                }
            }
        }
    }

    // Comments Bottom Sheet
    if (showCommentsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Comments & Reader Discussions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Comment input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text("Write a comment or theory...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (currentUserId == null) {
                                showCommentsSheet = false
                                showSignInPrompt = true
                            } else if (commentInput.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        repository.addComment(comic.id, commentInput.trim())
                                        commentInput = ""
                                        Toast.makeText(context, "Comment posted!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = commentInput.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (comments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Be the first to comment on this chapter!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(comments) { comment ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = comment.userName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = comment.content,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Google Sign-In prompt
    if (showSignInPrompt) {
        SignInPromptDialog(
            actionTitle = "Sign In to Bookmark & Rate",
            actionDescription = "Connect your Google account to save your reading progress, bookmark comics, and post comments across devices!",
            onDismiss = { showSignInPrompt = false },
            onAuthSuccess = {
                showSignInPrompt = false
                Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
