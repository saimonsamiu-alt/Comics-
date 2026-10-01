package com.example

import android.os.Bundle
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.repository.ComicRepository
import com.example.ui.auth.AuthManager
import com.example.ui.collab.CollaborationScreen
import com.example.ui.feed.SocialFeedScreen
import com.example.ui.home.ComicHomeScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.reader.ComicReaderScreen
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
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { ComicRepository(context) }
    val credentialManager = remember { CredentialManager.create(context) }

    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    var activeReadingComicId by remember { mutableStateOf<String?>(null) }

    // Attempt silent auto-sign-in on app start
    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    if (activeReadingComicId != null) {
        BackHandler {
            activeReadingComicId = null
        }
        ComicReaderScreen(
            comicId = activeReadingComicId!!,
            repository = repository,
            onBack = { activeReadingComicId = null }
        )
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.navigationBarsPadding().testTag("bottom_nav_bar"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    MainTab.entries.forEach { tab ->
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
                    MainTab.HOME -> ComicHomeScreen(
                        repository = repository,
                        onComicSelected = { id -> activeReadingComicId = id },
                        onOpenStudio = { currentTab = MainTab.STUDIO }
                    )
                    MainTab.STUDIO -> ComicStudioScreen(
                        repository = repository,
                        onNavigateToFeed = { currentTab = MainTab.FEED }
                    )
                    MainTab.FEED -> SocialFeedScreen(
                        repository = repository,
                        onRemixPost = { _ ->
                            currentTab = MainTab.STUDIO
                        },
                        onOpenStudio = { currentTab = MainTab.STUDIO }
                    )
                    MainTab.COLLAB -> CollaborationScreen(
                        repository = repository
                    )
                    MainTab.PROFILE -> ProfileScreen(
                        repository = repository,
                        onOpenComic = { id -> activeReadingComicId = id }
                    )
                }
            }
        }
    }
}
