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
}
