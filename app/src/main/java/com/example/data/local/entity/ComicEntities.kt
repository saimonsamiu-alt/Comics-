package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Comic
import com.example.data.model.Comment

@Entity(
    tableName = "comics",
    indices = [Index(value = ["authorId"]), Index(value = ["genre"])]
)
data class ComicEntity(
    @PrimaryKey val id: String,
    val title: String,
    val synopsis: String,
    val authorId: String,
    val authorName: String,
    val genre: String,
    val coverUrl: String?,
    val ratingAverage: Double,
    val ratingCount: Int,
    val viewCount: Int,
    val chapterCount: Int,
    val isPublished: Boolean,
    val isBookmarked: Boolean = false,
    val lastReadChapter: Int = 1,
    val progressPercent: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Comic {
        return Comic(
            id = id,
            title = title,
            synopsis = synopsis,
            authorId = authorId,
            authorName = authorName,
            genre = genre,
            coverUrl = coverUrl,
            ratingAverage = ratingAverage,
            ratingCount = ratingCount,
            viewCount = viewCount,
            chapterCount = chapterCount,
            isPublished = isPublished
        )
    }
}

fun Comic.toEntity(
    isBookmarked: Boolean = false,
    lastReadChapter: Int = 1,
    progressPercent: Int = 0
): ComicEntity {
    return ComicEntity(
        id = id,
        title = title,
        synopsis = synopsis,
        authorId = authorId,
        authorName = authorName,
        genre = genre,
        coverUrl = coverUrl,
        ratingAverage = ratingAverage,
        ratingCount = ratingCount,
        viewCount = viewCount,
        chapterCount = chapterCount,
        isPublished = isPublished,
        isBookmarked = isBookmarked,
        lastReadChapter = lastReadChapter,
        progressPercent = progressPercent
    )
}

@Entity(
    tableName = "comments",
    indices = [Index(value = ["comicId"])]
)
data class CommentEntity(
    @PrimaryKey val id: String,
    val comicId: String,
    val userId: String,
    val userName: String,
    val userAvatar: String?,
    val content: String,
    val createdAtEpoch: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Comment {
        return Comment(
            id = id,
            comicId = comicId,
            userId = userId,
            userName = userName,
            userAvatar = userAvatar,
            content = content
        )
    }
}

fun Comment.toEntity(): CommentEntity {
    return CommentEntity(
        id = id,
        comicId = comicId,
        userId = userId,
        userName = userName,
        userAvatar = userAvatar,
        content = content,
        createdAtEpoch = createdAt?.toDate()?.time ?: System.currentTimeMillis()
    )
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val email: String,
    val avatarUrl: String?,
    val isOfflineAvailable: Boolean = true,
    val lastLoginEpoch: Long = System.currentTimeMillis()
)

@Entity(tableName = "sketches")
data class SketchEntity(
    @PrimaryKey val id: String,
    val title: String,
    val imagePath: String,
    val captureSource: String, // "CAMERA", "GALLERY", "PRESET"
    val detectedSubject: String = "Pencil Character", // "Dog", "Anime Hero", "Fantasy Creature"
    val inkingContrast: Float = 0.85f,
    val colorStyle: String = "Vibrant Cel-Shaded",
    val characterColorHex: String = "#FDCB6E",
    val auraType: String = "Sparkle",
    val status: String = "READY_FOR_AI",
    val createdAtEpoch: Long = System.currentTimeMillis()
)
