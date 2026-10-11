package com.example.data.sample

import com.example.data.model.Chapter
import com.example.data.model.Comic
import com.example.data.model.ComicPanelItem
import com.example.data.model.FeedPost
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem

object SampleComicsData {
    val sampleComics = listOf(
        Comic(
            id = "c1",
            title = "Cyber Ninja: Neon Blade",
            author = "Alex Turner",
            authorId = "author_1",
            coverUrl = "",
            description = "In the neon-soaked streets of Neo-Tokyo, a rogue ninja uncovers a secret cybernetic conspiracy.",
            genre = "Cyberpunk / Action",
            rating = 4.9,
            likes = 340,
            views = 4200,
            chapters = listOf(
                Chapter(
                    id = "ch1",
                    number = 1,
                    title = "Shadows in the Rain",
                    panels = listOf(
                        ComicPanelItem(id = "p1", caption = "Rain poured incessantly over Sector 4.", soundEffect = "DRIP... DROP..."),
                        ComicPanelItem(id = "p2", caption = "A silhouette leaped across the neon rooftops.", soundEffect = "SWOOSH!"),
                        ComicPanelItem(id = "p3", caption = "The blade ignited in azure plasma.", soundEffect = "ZZZZT!")
                    )
                )
            ),
            isFeatured = true
        ),
        Comic(
            id = "c2",
            title = "Dragon Rider Chronicles",
            author = "Maya Lin",
            authorId = "author_2",
            coverUrl = "",
            description = "A young village apprentice bonds with the last Ember Drake of Mount Sol.",
            genre = "Fantasy / Adventure",
            rating = 4.8,
            likes = 280,
            views = 3100,
            chapters = listOf(
                Chapter(
                    id = "ch1",
                    number = 1,
                    title = "The Spark in the Cave",
                    panels = listOf(
                        ComicPanelItem(id = "p1", caption = "Deep in the obsidian cavern, a warm glow pulsed.", soundEffect = "HUMMM..."),
                        ComicPanelItem(id = "p2", caption = "Two golden slit eyes opened wide.", soundEffect = "GASP!"),
                        ComicPanelItem(id = "p3", caption = "A tiny burst of friendly flame warmed her palm.", soundEffect = "FWOOSH!")
                    )
                )
            ),
            isFeatured = true
        ),
        Comic(
            id = "c3",
            title = "Schoolyard Superheroes",
            author = "Samir Khan",
            authorId = "author_3",
            coverUrl = "",
            description = "Four middle schoolers accidentally gain superpowers from science fair cafeteria pudding.",
            genre = "Comedy / Superhero",
            rating = 4.7,
            likes = 195,
            views = 2400,
            chapters = listOf(
                Chapter(
                    id = "ch1",
                    number = 1,
                    title = "Mystery Lunch Tuesday",
                    panels = listOf(
                        ComicPanelItem(id = "p1", caption = "The pudding was glowing suspiciously radioactive green.", soundEffect = "SQUISH!"),
                        ComicPanelItem(id = "p2", caption = "Leo took a bite and suddenly floated 3 feet in the air.", soundEffect = "POOF!"),
                        ComicPanelItem(id = "p3", caption = "The bell rang, and chaos ensued.", soundEffect = "RIIIIING!")
                    )
                )
            ),
            isFeatured = false
        )
    )

    val sampleFeed = listOf(
        FeedPost(
            id = "f1",
            authorId = "a1",
            authorName = "PaperArtist99",
            caption = "Hand-drawn pencil sketch of our robot knight, scanned with CameraX and inked directly in ComicCraft!",
            likes = 45
        ),
        FeedPost(
            id = "f2",
            authorId = "a2",
            authorName = "WebtoonCreator",
            caption = "Panel 3 from episode 5 finished! Loving the cel-shaded ink style & shout bubbles.",
            likes = 82
        )
    )
}
