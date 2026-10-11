package com.example

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.Comic
import com.example.data.repository.ComicRepository
import com.example.ui.auth.AuthManager
import com.example.ui.collab.CollaborationScreen
import com.example.ui.feed.SocialFeedScreen
import com.example.ui.home.ComicHomeScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.reader.ComicReaderScreen
import com.example.ui.studio.CameraXCaptureDialog
import com.example.ui.studio.ComicLayoutSelectionScreen
import com.example.ui.studio.ComicLayoutType
import com.example.ui.studio.ComicStudioScreen
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Explore", Icons.Filled.Book, Icons.Outlined.Book),
    STUDIO("Studio", Icons.Filled.Brush, Icons.Outlined.Brush),
    FEED("Feed", Icons.Filled.AutoAwesome, Icons.Filled.AutoAwesome),
    COLLAB("Collab", Icons.Filled.Group, Icons.Outlined.Group),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ComicCraftApp()
            }
        }
    }
}

@Composable
fun ComicCraftApp() {
    val context = LocalContext.current
    val repository = remember { ComicRepository() }
    val authManager = remember { AuthManager(context) }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var readingComic by remember { mutableStateOf<Comic?>(null) }
    var studioLayout by remember { mutableStateOf<ComicLayoutType?>(null) }
    var showDirectCameraXCapture by remember { mutableStateOf(false) }

    // Direct CameraX Sketch Capture Dialog (can be triggered from home banner, layout picker, or studio)
    if (showDirectCameraXCapture) {
        CameraXCaptureDialog(
            onDismiss = { showDirectCameraXCapture = false },
            onPhotoCaptured = { photo ->
                showDirectCameraXCapture = false
                studioLayout = ComicLayoutType.SINGLE_PANEL
                currentTab = MainTab.STUDIO
                Toast.makeText(context, "Hand-drawn sketch captured with CameraX! 🎨", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (readingComic != null) {
        BackHandler {
            readingComic = null
        }
        ComicReaderScreen(
            comic = readingComic!!,
            onBack = { readingComic = null },
            repository = repository
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentTab = tab },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.HOME -> {
                        ComicHomeScreen(
                            repository = repository,
                            onComicSelected = { readingComic = it },
                            onNavigateToStudio = {
                                studioLayout = null
                                currentTab = MainTab.STUDIO
                            },
                            onDirectCameraCapture = {
                                showDirectCameraXCapture = true
                            }
                        )
                    }
                    MainTab.STUDIO -> {
                        if (studioLayout == null) {
                            ComicLayoutSelectionScreen(
                                onLayoutSelected = { selected ->
                                    studioLayout = selected
                                },
                                onDirectCameraCapture = {
                                    showDirectCameraXCapture = true
                                }
                            )
                        } else {
                            BackHandler {
                                studioLayout = null
                            }
                            ComicStudioScreen(
                                initialLayout = studioLayout!!,
                                onBackToLayouts = { studioLayout = null },
                                repository = repository,
                                authManager = authManager
                            )
                        }
                    }
                    MainTab.FEED -> {
                        SocialFeedScreen(repository = repository)
                    }
                    MainTab.COLLAB -> {
                        CollaborationScreen(repository = repository)
                    }
                    MainTab.PROFILE -> {
                        ProfileScreen(repository = repository, authManager = authManager)
                    }
                }
            }
        }
    }
}
