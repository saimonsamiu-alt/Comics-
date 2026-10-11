package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.dao.ComicDao
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ComicEntity
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.SketchEntity
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComicDatabaseTest {

    private lateinit var database: ComicCraftDatabase
    private lateinit var comicDao: ComicDao
    private lateinit var commentDao: CommentDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ComicCraftDatabase::class.java
        ).allowMainThreadQueries().build()

        comicDao = database.comicDao()
        commentDao = database.commentDao()
        userDao = database.userDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndReadComic() = runBlocking {
        val comic = ComicEntity(
            id = "test_comic_1",
            title = "Pencil Doggo Mystery",
            synopsis = "A hand-sketched detective comic",
            authorId = "user_123",
            authorName = "Artist Alice",
            genre = "Comedy",
            coverUrl = "sketch_dog",
            ratingAverage = 4.9,
            ratingCount = 10,
            viewCount = 150,
            chapterCount = 2,
            isPublished = true,
            isBookmarked = true,
            lastReadChapter = 1,
            progressPercent = 50
        )

        comicDao.insertComic(comic)
        val list = comicDao.getAllCachedComics().first()
        assertEquals(1, list.size)
        assertEquals("Pencil Doggo Mystery", list[0].title)
        assertTrue(list[0].isBookmarked)
    }

    @Test
    fun updateBookmarkState() = runBlocking {
        val comic = ComicEntity(
            id = "test_comic_2",
            title = "Cyber City",
            synopsis = "Neon comic",
            authorId = "user_456",
            authorName = "Bob",
            genre = "Sci-Fi",
            coverUrl = null,
            ratingAverage = 4.5,
            ratingCount = 5,
            viewCount = 60,
            chapterCount = 1,
            isPublished = true,
            isBookmarked = false
        )

        comicDao.insertComic(comic)
        comicDao.updateBookmarkState("test_comic_2", true, 2, 75)

        val updated = comicDao.getComicById("test_comic_2").first()
        assertNotNull(updated)
        assertTrue(updated!!.isBookmarked)
        assertEquals(2, updated.lastReadChapter)
        assertEquals(75, updated.progressPercent)
    }

    @Test
    fun insertAndQueryComments() = runBlocking {
        val comment1 = CommentEntity(
            id = "cmt_1",
            comicId = "comic_abc",
            userId = "user_1",
            userName = "Reader One",
            userAvatar = null,
            content = "Great drawing!",
            createdAtEpoch = 1000L
        )
        val comment2 = CommentEntity(
            id = "cmt_2",
            comicId = "comic_abc",
            userId = "user_2",
            userName = "Reader Two",
            userAvatar = null,
            content = "Amazing dialogue bubbles!",
            createdAtEpoch = 2000L
        )

        commentDao.insertComments(listOf(comment1, comment2))
        val comments = commentDao.getCommentsForComic("comic_abc").first()
        assertEquals(2, comments.size)
        assertEquals("cmt_2", comments[0].id) // Ordered DESC
    }

    @Test
    fun insertAndQueryActiveUser() = runBlocking {
        val user = UserEntity(
            userId = "signed_in_user_1",
            displayName = "Saimon",
            email = "saimonsamiu@gmail.com",
            avatarUrl = null,
            isOfflineAvailable = true,
            lastLoginEpoch = System.currentTimeMillis()
        )

        userDao.insertUser(user)
        val cachedUser = userDao.getActiveUser().first()
        assertNotNull(cachedUser)
        assertEquals("Saimon", cachedUser!!.displayName)
        assertEquals("saimonsamiu@gmail.com", cachedUser.email)
    }

    @Test
    fun insertAndQueryPaperSketches() = runBlocking {
        val sketchDao = database.sketchDao()
        val sketch = com.example.data.local.entity.SketchEntity(
            id = "sketch_camera_1",
            title = "My Pencil Dragon",
            imagePath = "/data/user/0/com.example/files/sketches/sketch_1.jpg",
            captureSource = "CAMERA",
            detectedSubject = "Fantasy Creature",
            inkingContrast = 0.9f,
            colorStyle = "Cyberpunk Glow",
            characterColorHex = "#6C5CE7",
            auraType = "Flame",
            status = "READY_FOR_AI",
            createdAtEpoch = System.currentTimeMillis()
        )

        sketchDao.insertSketch(sketch)
        val sketches = sketchDao.getAllSketches().first()
        assertEquals(1, sketches.size)
        assertEquals("My Pencil Dragon", sketches[0].title)
        assertEquals("READY_FOR_AI", sketches[0].status)
        assertEquals("CAMERA", sketches[0].captureSource)

        // Update status to AI_ENHANCED
        val updatedSketch = sketches[0].copy(status = "AI_ENHANCED")
        sketchDao.updateSketch(updatedSketch)
        val fetchedAfterUpdate = sketchDao.getSketchById("sketch_camera_1").first()
        assertNotNull(fetchedAfterUpdate)
        assertEquals("AI_ENHANCED", fetchedAfterUpdate!!.status)
    }
}

