package com.example.ui.studio

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ViewQuilt
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ComicLayoutType(
    val title: String,
    val subtitle: String,
    val panelsCount: Int,
    val icon: ImageVector,
    val badge: String
) {
    SINGLE_PANEL(
        title = "Single Epic Panel",
        subtitle = "Full-bleed splash art, cover showcase & high-impact punchlines",
        panelsCount = 1,
        icon = Icons.Default.ViewStream,
        badge = "Splash / Cover"
    ),
    STRIP_3_PANEL(
        title = "3-Panel Story Strip",
        subtitle = "Setup • Conflict • Punchline webtoon strip sequence",
        panelsCount = 3,
        icon = Icons.Default.ViewColumn,
        badge = "Webtoon Classic"
    ),
    GRID_4_PANEL(
        title = "4-Panel Comic Grid",
        subtitle = "2x2 classic Yonkoma / Sunday newspaper gag strip",
        panelsCount = 4,
        icon = Icons.AutoMirrored.Filled.ViewQuilt,
        badge = "4-Koma / Manga"
    ),
    SPLIT_2_PANEL(
        title = "2-Panel Action Split",
        subtitle = "Dominant action panel + reaction showdown",
        panelsCount = 2,
        icon = Icons.Default.ViewCarousel,
        badge = "Versus / Duel"
    ),
    WIDESCREEN_CINEMATIC(
        title = "Cinematic Widescreen",
        subtitle = "16:9 anime movie panoramic aspect ratio",
        panelsCount = 1,
        icon = Icons.Default.Dashboard,
        badge = "16:9 Anime"
    )
}

@Composable
fun ComicLayoutSelectionScreen(
    onLayoutSelected: (ComicLayoutType) -> Unit,
    onDirectCameraCapture: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("comic_layout_selection_screen")
    ) {
        // Hero Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Start Creating",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Pick a comic layout or photograph your sketch",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Direct CameraX Callout Banner
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDirectCameraCapture() }
                .testTag("layout_screen_camera_banner")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture Sketch",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Snap Paper Sketch (CameraX)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Photograph your notebook drawing with live viewfinder",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "CHOOSE COMIC FORMAT",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Layout Cards List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ComicLayoutType.values()) { layout ->
                LayoutCard(
                    layout = layout,
                    onClick = { onLayoutSelected(layout) }
                )
            }
        }
    }
}

@Composable
private fun LayoutCard(
    layout: ComicLayoutType,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("layout_card_${layout.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visual Mini Layout Preview Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                when (layout) {
                    ComicLayoutType.SINGLE_PANEL -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        )
                    }
                    ComicLayoutType.STRIP_3_PANEL -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            repeat(3) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                    ComicLayoutType.GRID_4_PANEL -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxSize().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.weight(1f).fillMaxSize().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp)))
                            }
                            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxSize().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp)))
                                Box(modifier = Modifier.weight(1f).fillMaxSize().background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(2.dp)))
                            }
                        }
                    }
                    ComicLayoutType.SPLIT_2_PANEL -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Box(modifier = Modifier.weight(1.4f).fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                            Box(modifier = Modifier.weight(0.9f).fillMaxWidth().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(2.dp)).border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                        }
                    }
                    ComicLayoutType.WIDESCREEN_CINEMATIC -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(Color(0xFF212121), RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color(0xFFFFC107), RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = layout.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = layout.badge,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = layout.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
