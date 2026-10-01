package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Comic(
    val id: String = "",
    val title: String = "",
    val synopsis: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val genre: String = "Action",
    val coverUrl: String? = null,
    val ratingAverage: Double = 5.0,
    val ratingCount: Int = 0,
    val viewCount: Int = 0,
    val chapterCount: Int = 1,
    val isPublished: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "synopsis" to synopsis,
            "authorId" to authorId,
            "authorName" to authorName,
            "genre" to genre,
            "coverUrl" to coverUrl,
            "ratingAverage" to ratingAverage,
            "ratingCount" to ratingCount,
            "viewCount" to viewCount,
            "chapterCount" to chapterCount,
            "isPublished" to isPublished
        ).filterValues { it != null }
    }
}

data class Chapter(
    val id: String = "",
    val comicId: String = "",
    val title: String = "",
    val chapterNumber: Int = 1,
    val panelsJson: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "comicId" to comicId,
            "title" to title,
            "chapterNumber" to chapterNumber,
            "panelsJson" to panelsJson
        ).filterValues { it != null }
    }
}

data class Comment(
    val id: String = "",
    val comicId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvatar: String? = null,
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "comicId" to comicId,
            "userId" to userId,
            "userName" to userName,
            "userAvatar" to userAvatar,
            "content" to content
        ).filterValues { it != null }
    }
}

data class Rating(
    val id: String = "",
    val comicId: String = "",
    val userId: String = "",
    val score: Int = 5,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "comicId" to comicId,
            "userId" to userId,
            "score" to score
        ).filterValues { it != null }
    }
}

data class Bookmark(
    val id: String = "",
    val userId: String = "",
    val comicId: String = "",
    val comicTitle: String = "",
    val coverUrl: String? = null,
    val lastReadChapter: Int = 1,
    val progressPercent: Int = 0,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "userId" to userId,
            "comicId" to comicId,
            "comicTitle" to comicTitle,
            "coverUrl" to coverUrl,
            "lastReadChapter" to lastReadChapter,
            "progressPercent" to progressPercent
        ).filterValues { it != null }
    }
}

data class FeedPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val characterDesign: String = "",
    val likesCount: Int = 0,
    val remixCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "authorId" to authorId,
            "authorName" to authorName,
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "characterDesign" to characterDesign,
            "likesCount" to likesCount,
            "remixCount" to remixCount
        ).filterValues { it != null }
    }
}

data class Collaboration(
    val id: String = "",
    val title: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val collaboratorIds: String = "",
    val status: String = "active",
    val currentPanelScript: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "collaboratorIds" to collaboratorIds,
            "status" to status,
            "currentPanelScript" to currentPanelScript
        ).filterValues { it != null }
    }
}

// Visual Comic Panel Editor Models
data class ComicPanelItem(
    val id: String = "",
    val imageUrl: String = "",
    val sketchTitle: String = "Hand-drawn Character",
    val colorStyle: String = "Vibrant Cel-Shaded",
    val speechBubbles: List<SpeechBubbleItem> = emptyList(),
    val soundEffects: List<SoundEffectItem> = emptyList()
)

data class SpeechBubbleItem(
    val id: String = "",
    val text: String = "Hello!",
    val type: String = "Speech", // Speech, Thought, Shout, Whisper
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.2f
)

data class SoundEffectItem(
    val id: String = "",
    val text: String = "POW!",
    val colorHex: String = "#FF5722",
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f
)
