package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ComicEntity
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.SketchEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {

    @Query("SELECT * FROM comics ORDER BY cachedAt DESC")
    fun getAllCachedComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE id = :comicId LIMIT 1")
    fun getComicById(comicId: String): Flow<ComicEntity?>

    @Query("SELECT * FROM comics WHERE isBookmarked = 1 ORDER BY cachedAt DESC")
    fun getBookmarkedComics(): Flow<List<ComicEntity>>

    @Query("SELECT * FROM comics WHERE genre LIKE '%' || :genre || '%' ORDER BY cachedAt DESC")
    fun getComicsByGenre(genre: String): Flow<List<ComicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComics(comics: List<ComicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComic(comic: ComicEntity)

    @Update
    suspend fun updateComic(comic: ComicEntity)

    @Query("UPDATE comics SET isBookmarked = :bookmarked, lastReadChapter = :chapter, progressPercent = :percent WHERE id = :comicId")
    suspend fun updateBookmarkState(comicId: String, bookmarked: Boolean, chapter: Int, percent: Int)

    @Query("DELETE FROM comics WHERE id = :comicId")
    suspend fun deleteComicById(comicId: String)

    @Query("DELETE FROM comics")
    suspend fun clearAllComics()

    @Query("SELECT COUNT(*) FROM comics")
    fun getCachedComicsCount(): Flow<Int>
}

@Dao
interface CommentDao {

    @Query("SELECT * FROM comments WHERE comicId = :comicId ORDER BY createdAtEpoch DESC")
    fun getCommentsForComic(comicId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComments(comments: List<CommentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteCommentById(commentId: String)

    @Query("DELETE FROM comments WHERE comicId = :comicId")
    suspend fun deleteCommentsForComic(comicId: String)

    @Query("SELECT COUNT(*) FROM comments")
    fun getCachedCommentsCount(): Flow<Int>
}

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY lastLoginEpoch DESC LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users")
    suspend fun clearUsers()
}

@Dao
interface SketchDao {

    @Query("SELECT * FROM sketches ORDER BY createdAtEpoch DESC")
    fun getAllSketches(): Flow<List<SketchEntity>>

    @Query("SELECT * FROM sketches WHERE id = :id LIMIT 1")
    fun getSketchById(id: String): Flow<SketchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSketch(sketch: SketchEntity)

    @Update
    suspend fun updateSketch(sketch: SketchEntity)

    @Query("DELETE FROM sketches WHERE id = :id")
    suspend fun deleteSketchById(id: String)

    @Query("SELECT COUNT(*) FROM sketches")
    fun getSketchCount(): Flow<Int>
}
