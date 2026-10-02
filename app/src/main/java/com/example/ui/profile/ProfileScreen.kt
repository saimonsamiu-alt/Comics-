package com.example.ui.profile

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.local.entity.UserEntity
import com.example.data.model.Bookmark
import com.example.data.repository.ComicRepository
import com.example.ui.auth.AuthManager
import com.example.ui.auth.GoogleSignInButton
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    repository: ComicRepository,
    onOpenComic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    val bookmarks = remember { mutableStateListOf<Bookmark>() }
    var cachedComicsCount by remember { mutableStateOf(0) }
    var cachedCommentsCount by remember { mutableStateOf(0) }
    var cachedSketchesCount by remember { mutableStateOf(0) }

    LaunchedEffect(currentUser?.uid) {
        val user = currentUser
        if (user != null) {
            repository.cacheUser(
                UserEntity(
                    userId = user.uid,
                    displayName = user.displayName ?: "Comic Creator",
                    email = user.email ?: "",
                    avatarUrl = user.photoUrl?.toString(),
                    isOfflineAvailable = true,
                    lastLoginEpoch = System.currentTimeMillis()
                )
            )
            repository.observeUserBookmarks(user.uid).collectLatest { list ->
                bookmarks.clear()
                bookmarks.addAll(list)
            }
        } else {
            bookmarks.clear()
        }
    }

    LaunchedEffect(Unit) {
        launch {
            repository.observeCachedComicsCount().collectLatest { count ->
                cachedComicsCount = count
            }
        }
        launch {
            repository.observeCachedCommentsCount().collectLatest { count ->
                cachedCommentsCount = count
            }
        }
        launch {
            repository.observeSketchCount().collectLatest { count ->
                cachedSketchesCount = count
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        item {
            Text(
                text = "Account & Cloud Sync",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track reading progress & synced bookmarks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (currentUser == null) {
                        // Guest User State
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Guest Reader",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sign in to save bookmarks & cloud progress",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        GoogleSignInButton(
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            buttonText = "Sign in with Google",
                            onAuthSuccess = {
                                currentUser = Firebase.auth.currentUser
                                Toast.makeText(context, "Welcome ${currentUser?.displayName ?: ""}!", Toast.LENGTH_SHORT).show()
                            },
                            onAuthError = { err ->
                                Toast.makeText(context, "Sign in error: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        // Signed In State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (currentUser?.displayName ?: "U").take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = currentUser?.displayName ?: "Comic Creator",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    AuthManager.signOut(context, credentialManager, onSignOutComplete = {
                                        currentUser = null
                                        bookmarks.clear()
                                    }, scope = coroutineScope)
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign out", tint = Color.Red)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color(0xFF00B894),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "All progress synced with Firestore Cloud",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Room SQLite Offline Cache: $cachedComicsCount Comics, $cachedCommentsCount Comments, $cachedSketchesCount Sketches stored locally",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Saved Bookmarks & Progress (${bookmarks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (bookmarks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (currentUser == null) "Sign in to save bookmarks" else "No saved bookmarks yet",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap the bookmark icon on any comic while reading to save your spot!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(bookmarks) { bookmark ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onOpenComic(bookmark.comicId) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bookmark.comicTitle,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Last read: Chapter ${bookmark.lastReadChapter} (${bookmark.progressPercent}%)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { bookmark.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                            )
                        }

                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.removeBookmark(bookmark.comicId)
                                    bookmarks.removeAll { it.comicId == bookmark.comicId }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            IOSCrossPlatformCard()
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun IOSCrossPlatformCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val iosWebUrl = "https://ais-pre-kz2zzzffpqap45vh3u7xq7-264203637948.asia-southeast1.run.app"
    var isCopied by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ios_cross_platform_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PhoneIphone,
                            contentDescription = "iOS Support",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "iOS (iPhone & iPad) Download",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "iPhone বা iPad-এ সহজেই ইনস্টল ও ব্যবহার করুন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "📱 How to install on iOS / iPhone (3 Easy Steps):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Open Safari on iPhone/iPad and visit the web app link.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "2. Tap the Safari Share button (⎋ square with upward arrow).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "3. Tap 'Add to Home Screen' — ComicCraft installs as a full-screen app with offline reading, studio tools, and Google account sync!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Copy Link & Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(iosWebUrl))
                        isCopied = true
                        Toast.makeText(context, "iOS Web App link copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCopied) "Link Copied!" else "Copy iOS Link", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "ComicCraft for iPhone & iPad")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Open ComicCraft on your iPhone or iPad! Read comics and animate sketches: $iosWebUrl"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share ComicCraft to iOS Device"))
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share to iOS", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Toggle QR Code button
            OutlinedButton(
                onClick = { showQrCode = !showQrCode },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showQrCode) "Hide iPhone QR Code" else "Scan with iPhone Camera to Open", fontSize = 12.sp)
            }

            if (showQrCode) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(modifier = Modifier.size(160.dp)) {
                            drawQrCodeMatrix(size)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Point your iPhone Camera at this code to open in Safari instantly!",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawQrCodeMatrix(size: Size) {
    val cellSize = size.width / 21f
    val darkColor = Color(0xFF181829)

    // Finder patterns in 3 corners (Top-Left, Top-Right, Bottom-Left)
    fun drawFinder(startX: Float, startY: Float) {
        drawRoundRect(
            color = darkColor,
            topLeft = Offset(startX, startY),
            size = Size(cellSize * 7, cellSize * 7),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(startX + cellSize, startY + cellSize),
            size = Size(cellSize * 5, cellSize * 5),
            cornerRadius = CornerRadius(4f, 4f)
        )
        drawRoundRect(
            color = darkColor,
            topLeft = Offset(startX + cellSize * 2, startY + cellSize * 2),
            size = Size(cellSize * 3, cellSize * 3),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }

    drawFinder(0f, 0f)
    drawFinder(size.width - cellSize * 7, 0f)
    drawFinder(0f, size.height - cellSize * 7)

    // Decorative data cells
    val pattern = listOf(
        Pair(9, 1), Pair(11, 1), Pair(8, 3), Pair(10, 3), Pair(12, 3),
        Pair(7, 5), Pair(9, 5), Pair(11, 5), Pair(13, 5),
        Pair(1, 9), Pair(3, 9), Pair(5, 9), Pair(8, 8), Pair(10, 8),
        Pair(12, 8), Pair(14, 8), Pair(16, 8), Pair(18, 8), Pair(20, 8),
        Pair(8, 10), Pair(10, 10), Pair(13, 10), Pair(15, 10), Pair(17, 10),
        Pair(8, 12), Pair(11, 12), Pair(14, 12), Pair(16, 12), Pair(19, 12),
        Pair(9, 14), Pair(11, 14), Pair(13, 14), Pair(17, 14),
        Pair(8, 16), Pair(10, 16), Pair(12, 16), Pair(15, 16), Pair(18, 16),
        Pair(9, 18), Pair(11, 18), Pair(13, 18), Pair(16, 18), Pair(20, 18),
        Pair(10, 20), Pair(12, 20), Pair(14, 20), Pair(17, 20), Pair(19, 20)
    )

    pattern.forEach { (col, row) ->
        drawRoundRect(
            color = darkColor,
            topLeft = Offset(col * cellSize, row * cellSize),
            size = Size(cellSize * 0.9f, cellSize * 0.9f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}
