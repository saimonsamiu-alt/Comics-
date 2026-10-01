package com.example.data.sample

import com.example.data.model.Chapter
import com.example.data.model.Comic
import com.example.data.model.ComicPanelItem
import com.example.data.model.FeedPost
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem

object SampleComicsData {

    val starterComics = listOf(
        Comic(
            id = "comic_doggo_detective",
            title = "Doggo Detective: Notebook Case",
            synopsis = "Drawn directly in a student's notebook! Buster the hand-sketched hound solves the mystery of the missing golden bone in a whimsical hand-inked comic world.",
            authorId = "author_saimon",
            authorName = "Saimon Samiu",
            genre = "Comedy & Mystery",
            coverUrl = "sketch_dog",
            ratingAverage = 4.9,
            ratingCount = 84,
            viewCount = 1240,
            chapterCount = 2,
            isPublished = true
        ),
        Comic(
            id = "comic_cyber_dhaka",
            title = "Cyber Blade: Neon Sector 7",
            synopsis = "In the futuristic skyline of Cyber City, a rebel street artist discovers a prototype cybernetic stylus that can materialize anything sketched into electric reality.",
            authorId = "author_nabil",
            authorName = "Nabil Comics",
            genre = "Cyberpunk Action",
            coverUrl = "hero_banner",
            ratingAverage = 4.8,
            ratingCount = 152,
            viewCount = 3890,
            chapterCount = 3,
            isPublished = true
        ),
        Comic(
            id = "comic_phantom_dragon",
            title = "The Paper Dragon Chronicles",
            synopsis = "An ancient fantasy beast sealed inside an artist's sketchbook awakens when water drops touch the paper, sparking an epic journey across realms.",
            authorId = "author_lina",
            authorName = "Lina Art",
            genre = "Fantasy & Adventure",
            coverUrl = "sketch_dragon",
            ratingAverage = 4.7,
            ratingCount = 63,
            viewCount = 920,
            chapterCount = 1,
            isPublished = true
        )
    )

    val starterPanelsForDoggo = listOf(
        ComicPanelItem(
            id = "panel_1",
            imageUrl = "sketch_dog",
            sketchTitle = "Pencil Sketch: Detective Buster on Notebook",
            colorStyle = "Anime Vibrant Cel-Shaded",
            speechBubbles = listOf(
                SpeechBubbleItem(
                    id = "sb_1",
                    text = "Aha! Footprints leading behind the school desk...",
                    type = "Thought",
                    xRatio = 0.5f,
                    yRatio = 0.15f
                )
            ),
            soundEffects = listOf(
                SoundEffectItem(
                    id = "sfx_1",
                    text = "SNIFF!",
                    colorHex = "#FF5722",
                    xRatio = 0.25f,
                    yRatio = 0.65f
                )
            )
        ),
        ComicPanelItem(
            id = "panel_2",
            imageUrl = "sketch_dog_action",
            sketchTitle = "Buster Jumps Into Action!",
            colorStyle = "Cyberpunk Glow",
            speechBubbles = listOf(
                SpeechBubbleItem(
                    id = "sb_2",
                    text = "Stop right there, treat thief!",
                    type = "Shout",
                    xRatio = 0.5f,
                    yRatio = 0.2f
                )
            ),
            soundEffects = listOf(
                SoundEffectItem(
                    id = "sfx_2",
                    text = "POW!!",
                    colorHex = "#FDCB6E",
                    xRatio = 0.75f,
                    yRatio = 0.55f
                )
            )
        ),
        ComicPanelItem(
            id = "panel_3",
            imageUrl = "sketch_dog_victory",
            sketchTitle = "Case Solved!",
            colorStyle = "Vintage Retro Comic",
            speechBubbles = listOf(
                SpeechBubbleItem(
                    id = "sb_3",
                    text = "Another notebook mystery closed by Buster!",
                    type = "Speech",
                    xRatio = 0.5f,
                    yRatio = 0.25f
                )
            ),
            soundEffects = listOf(
                SoundEffectItem(
                    id = "sfx_3",
                    text = "WAG WAG!",
                    colorHex = "#00CEC9",
                    xRatio = 0.3f,
                    yRatio = 0.75f
                )
            )
        )
    )

    val starterFeedPosts = listOf(
        FeedPost(
            id = "post_1",
            authorId = "author_saimon",
            authorName = "Saimon (Creator)",
            title = "Notebook Doggo Sketch -> Manga Hero!",
            description = "Drew this pencil puppy on my class notebook, then used ComicCraft to vectorize the ink lines and add anime cel shading + dynamic speech bubble!",
            imageUrl = "sketch_dog",
            characterDesign = "Breed: Beagle Detective, Outfit: Detective Trenchcoat, Aura: Golden Amber",
            likesCount = 42,
            remixCount = 12
        ),
        FeedPost(
            id = "post_2",
            authorId = "author_tariq",
            authorName = "Tariq K.",
            title = "Mecha Cyber Guardian WIP",
            description = "Hand-sketched with 2B pencil and blue ballpoint. Enhanced line thickness, added neon cyber highlights!",
            imageUrl = "hero_banner",
            characterDesign = "Armor: Neon Blue Titanium, Weapon: Energy Halberd",
            likesCount = 88,
            remixCount = 29
        ),
        FeedPost(
            id = "post_3",
            authorId = "author_maya",
            authorName = "Maya Studio",
            title = "Mystic Forest Spirit",
            description = "Ballpoint sketch transformed into watercolor pastel fantasy panel. Animated with breathing motion!",
            imageUrl = "sketch_dragon",
            characterDesign = "Type: Forest Dragonling, Elements: Leaf Wings & Emerald Horns",
            likesCount = 65,
            remixCount = 18
        )
    )
}
