package com.example.ui.studio

/**
 * Represents comic page layouts supported by ComicCraft Studio.
 */
enum class ComicLayoutType(
    val id: String,
    val title: String,
    val subtitle: String,
    val panelCount: Int,
    val tag: String,
    val aspectRatio: Float,
    val description: String,
    val panelLabels: List<String>
) {
    SINGLE_PANEL(
        id = "single_panel",
        title = "Single Epic Panel",
        subtitle = "Splash art & dramatic covers",
        panelCount = 1,
        tag = "Classic Splash",
        aspectRatio = 1.0f,
        description = "One expansive hero panel for high-impact character artwork, epic climaxes, or comic book covers.",
        panelLabels = listOf("Main Scene")
    ),
    THREE_PANEL_STRIP(
        id = "three_panel_strip",
        title = "3-Panel Story Strip",
        subtitle = "Setup • Conflict • Punchline",
        panelCount = 3,
        tag = "Webtoon & Daily Strip",
        aspectRatio = 0.82f,
        description = "3 stacked vertical frames. Perfect for short stories, comedy gags, or webtoon-style reading.",
        panelLabels = listOf("Panel 1: Setup", "Panel 2: Conflict", "Panel 3: Punchline")
    ),
    FOUR_PANEL_GRID(
        id = "four_panel_grid",
        title = "4-Panel Comic Grid",
        subtitle = "2x2 classic Yonkoma / Newspaper",
        panelCount = 4,
        tag = "Manga & Newspaper",
        aspectRatio = 1.0f,
        description = "Classic 2x2 quad box layout for multi-scene narrative progression, manga storytelling, and episodic pacing.",
        panelLabels = listOf("Scene 1", "Scene 2", "Scene 3", "Scene 4")
    ),
    TWO_PANEL_SPLIT(
        id = "two_panel_split",
        title = "2-Panel Action Split",
        subtitle = "Duel & Reaction sequence",
        panelCount = 2,
        tag = "Heroic Drama",
        aspectRatio = 0.9f,
        description = "Split format featuring a dominant widescreen action panel and an expressive close-up reaction panel.",
        panelLabels = listOf("Top: Action", "Bottom: Reaction")
    ),
    CINEMATIC_WIDESCREEN(
        id = "cinematic_widescreen",
        title = "Cinematic Widescreen",
        subtitle = "16:9 movie style panoramic",
        panelCount = 1,
        tag = "Cinema Shot",
        aspectRatio = 16f / 9f,
        description = "Ultra-wide landscape panel formatted like anime cinema with letterboxed framing for motion pans.",
        panelLabels = listOf("Cinematic Shot")
    )
}
