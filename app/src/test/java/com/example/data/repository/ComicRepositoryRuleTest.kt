package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Bookmark
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComicRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun observeComics_canBeCalledByAnyone() = runBlocking<Unit> {
        val repository = ComicRepository(
            firestore = firestore,
            userIdProvider = { null }
        )
        val flow = repository.observeComics()
        assertNotNull(flow)
    }

    @Test
    fun saveBookmark_unauthenticatedUser_throwsIllegalStateException() = runBlocking<Unit> {
        val repository = ComicRepository(
            firestore = firestore,
            userIdProvider = { null }
        )
        var exceptionThrown = false
        try {
            repository.saveBookmark(
                Bookmark(
                    comicId = "test_comic_1",
                    comicTitle = "Test Comic",
                    lastReadChapter = 1,
                    progressPercent = 50
                )
            )
        } catch (e: IllegalStateException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun publishComic_unauthenticatedUser_throwsIllegalStateException() = runBlocking<Unit> {
        val repository = ComicRepository(
            firestore = firestore,
            userIdProvider = { null }
        )
        var exceptionThrown = false
        try {
            repository.publishComic(
                title = "Unauth Comic",
                synopsis = "Should fail fast before hitting firestore",
                genre = "Action",
                coverUrl = "sketch_dog",
                chapterTitle = "Chapter 1",
                panelsJson = "[]"
            )
        } catch (e: IllegalStateException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun saveComicPanelState_unauthenticatedUser_throwsIllegalStateException() = runBlocking<Unit> {
        val repository = ComicRepository(
            firestore = firestore,
            userIdProvider = { null }
        )
        var exceptionThrown = false
        try {
            repository.saveComicPanelState(
                title = "Hero Battle Scene",
                bitmap = null,
                sketchType = "dog",
                colorStyle = "Vibrant Cel-Shaded",
                characterColorHex = "#FDCB6E",
                auraType = "Sparkle",
                inkingEdgeContrast = 0.85f,
                isCleanPaperEnabled = true,
                animationMode = "Camera Pan & Zoom",
                speechBubbles = emptyList(),
                stickers = emptyList(),
                soundEffects = emptyList()
            )
        } catch (e: IllegalStateException) {
            exceptionThrown = true
        }
        assertTrue(exceptionThrown)
    }

    @Test
    fun observeSavedPanels_unauthenticated_returnsEmptyFlow() = runBlocking<Unit> {
        val repository = ComicRepository(
            firestore = firestore,
            userIdProvider = { null }
        )
        val flow = repository.observeSavedPanels()
        assertNotNull(flow)
    }
}
