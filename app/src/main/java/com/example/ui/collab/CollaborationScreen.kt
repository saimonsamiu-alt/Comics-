package com.example.ui.collab

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Collaboration
import com.example.data.repository.ComicRepository
import com.example.ui.auth.SignInPromptDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CollaborationScreen(
    repository: ComicRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = repository.getCurrentUserId()

    val collabs = remember { mutableStateListOf<Collaboration>() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSignInPrompt by remember { mutableStateOf(false) }
    var activeEditingCollab by remember { mutableStateOf<Collaboration?>(null) }

    LaunchedEffect(Unit) {
        repository.observeCollaborations().collectLatest { list ->
            collabs.clear()
            collabs.addAll(list)
        }
    }

    val displayCollabs = remember(collabs.toList()) {
        if (collabs.isEmpty()) {
            listOf(
                Collaboration(
                    id = "collab_demo_1",
                    title = "Episode 2: The Neon Heist (Shared Storyboard)",
                    ownerId = "creator_1",
                    ownerName = "Saimon Samiu",
                    collaboratorIds = "all",
                    status = "active",
                    currentPanelScript = "Panel 1: Rain falls over neon billboards. Buster the dog barks in morse code.\nPanel 2: Dialogue [Buster]: 'The treat heist begins at midnight!'\nPanel 3: Sound effect: *KABOOM!* smoke cloud clears to reveal the shadow villain."
                )
            )
        } else {
            collabs
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("collaboration_screen")
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Real-Time Co-Creation",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Multi-device syncing for writers & comic artists",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        if (currentUserId == null) {
                            showSignInPrompt = true
                        } else {
                            showCreateDialog = true
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Room")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Sync Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cloud Sync & Storage Active",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (currentUserId != null) "Connected: Firestore Enterprise Real-Time Database" else "Guest Mode: Sign in to sync across your phone, tablet and laptop",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Active Collaboration Rooms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        items(displayCollabs) { collab ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = collab.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = Color(0xFF00B894).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "LIVE SYNC",
                                color = Color(0xFF00B894),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Lead Author: ${collab.ownerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = collab.currentPanelScript,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Realtime Co-authors", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(
                            onClick = {
                                if (currentUserId == null) {
                                    showSignInPrompt = true
                                } else {
                                    activeEditingCollab = collab
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Script", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Create Room Dialog
    if (showCreateDialog) {
        var roomTitle by remember { mutableStateOf("") }
        var initialScript by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Create Collaboration Room",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = roomTitle,
                        onValueChange = { roomTitle = it },
                        label = { Text("Comic Episode / Script Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = initialScript,
                        onValueChange = { initialScript = it },
                        label = { Text("Initial Panel Beats & Dialogue Script") },
                        placeholder = { Text("Panel 1: [Buster] 'Look at that!'\nPanel 2: Sound effect: *POW!*") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (roomTitle.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        repository.createCollaboration(roomTitle, initialScript)
                                        Toast.makeText(context, "Room created & synced!", Toast.LENGTH_SHORT).show()
                                        showCreateDialog = false
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = roomTitle.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create Live Room")
                    }
                }
            }
        }
    }

    // Live Script Editor Dialog
    activeEditingCollab?.let { collab ->
        var editedScript by remember(collab.currentPanelScript) { mutableStateOf(collab.currentPanelScript) }

        Dialog(onDismissRequest = { activeEditingCollab = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Live Storyboard Editor",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Changes will sync in real-time across all collaborators' devices",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editedScript,
                        onValueChange = { editedScript = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 6
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    repository.updateCollaborationScript(collab.id, editedScript)
                                    Toast.makeText(context, "Script updated in cloud!", Toast.LENGTH_SHORT).show()
                                    activeEditingCollab = null
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync to All Devices")
                    }
                }
            }
        }
    }

    if (showSignInPrompt) {
        SignInPromptDialog(
            actionTitle = "Sign In to Co-Create",
            actionDescription = "Connect your Google account to collaborate in real-time and sync comic scripts across devices!",
            onDismiss = { showSignInPrompt = false },
            onAuthSuccess = {
                showSignInPrompt = false
                showCreateDialog = true
            }
        )
    }
}
