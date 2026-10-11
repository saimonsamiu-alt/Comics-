package com.example.data.model

import com.google.firebase.Timestamp

data class Comic(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val authorName: String = "",
    val authorId: String = "",
    val coverUrl: String? = null,
    val description: String = "",
    val synopsis: String = "",
    val genre: String = "Action",
    val rating: Double = 4.8,
    val ratingAverage: Double = 4.8,
    val likes: Int = 120,
    val ratingCount: Int = 120,
    val views: Int = 1500,
    val viewCount: Int = 1500,
    val chapterCount: Int = 1,
    val isPublished: Boolean = true,
    val chapters: List<Chapter> = emptyList(),
    val isFeatured: Boolean = false,
    val createdAt: Timestamp? = null
)

data class Chapter(
    val id: String = "",
    val number: Int = 1,
    val title: String = "Chapter 1",
    val panels: List<ComicPanelItem> = emptyList()
)

data class ComicPanelItem(
    val id: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val soundEffect: String = ""
)

data class Comment(
    val id: String = "",
    val comicId: String = "",
    val userId: String = "",
    val userName: String = "Comic Fan",
    val userAvatar: String? = null,
    val content: String = "",
    val timestamp: Timestamp? = null,
    val createdAt: Timestamp? = timestamp
)

data class Bookmark(
    val comicId: String = "",
    val title: String = "",
    val coverUrl: String = "",
    val timestamp: Timestamp? = null
)

data class Collaboration(
    val id: String = "",
    val title: String = "",
    val hostUserId: String = "",
    val hostUserName: String = "",
    val partnerId: String = "",
    val currentPanelScript: String = "",
    val status: String = "Open",
    val updatedAt: Timestamp? = null
)

data class FeedPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "Artist",
    val authorAvatar: String = "",
    val panelImageUrl: String = "",
    val caption: String = "",
    val likes: Int = 0,
    val timestamp: Timestamp? = null
)

data class Rating(
    val score: Double = 5.0,
    val count: Int = 1
)

data class SpeechBubbleItem(
    val id: String = "",
    val text: String = "Hello World!",
    val type: String = "Speech", // Speech, Thought, Shout, Whisper
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val scale: Float = 1.0f,
    val tailDirection: String = "BOTTOM_LEFT"
)

data class StickerItem(
    val id: String = "",
    val emojiOrIcon: String = "⭐",
    val label: String = "Star",
    val category: String = "Power", // Power, Animal, Gear, Fun
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f
)

data class SoundEffectItem(
    val id: String = "",
    val text: String = "BOOM!",
    val colorHex: String = "#FF5722",
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val scale: Float = 1.2f,
    val rotation: Float = -10.0f
)

data class SavedComicPanelState(
    val id: String = "",
    val userId: String = "",
    val title: String = "My Comic Scene",
    val baseImageBase64: String = "",
    val sketchType: String = "Action Hero",
    val colorStyle: String = "Vibrant Webtoon",
    val animationMode: String = "Camera Pan & Zoom",
    val bubblesJson: String = "[]",
    val stickersJson: String = "[]",
    val soundEffectsJson: String = "[]",
    val inkingContrast: Float = 1.2f,
    val createdAt: Timestamp? = null
)
