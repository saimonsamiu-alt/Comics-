package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.model.Bookmark
import com.example.data.model.Comic
import com.example.data.model.Collaboration
import com.example.data.model.FeedPost
import com.example.data.model.SavedComicPanelState
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import com.example.data.sample.SampleComicsData
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ComicRepository {

    private val firestore get() = Firebase.firestore
    private val auth get() = FirebaseAuth.getInstance()

    fun observeComics(): Flow<List<Comic>> = callbackFlow {
        val listener = firestore.collection("comics")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(SampleComicsData.sampleComics)
                    return@addSnapshotListener
                }
                val comics = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comic::class.java)?.copy(id = doc.id)
                }
                trySend(if (comics.isEmpty()) SampleComicsData.sampleComics else comics)
            }
        awaitClose { listener.remove() }
    }

    fun observeFeed(): Flow<List<FeedPost>> = callbackFlow {
        val listener = firestore.collection("feed")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    trySend(SampleComicsData.sampleFeed)
                    return@addSnapshotListener
                }
                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FeedPost::class.java)?.copy(id = doc.id)
                }
                trySend(if (posts.isEmpty()) SampleComicsData.sampleFeed else posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun publishFeedPost(caption: String, panelBitmap: Bitmap?) {
        val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
        val postRef = firestore.collection("feed").document()
        val base64Image = panelBitmap?.let { bitmapToBase64(it) } ?: ""
        val post = FeedPost(
            id = postRef.id,
            authorId = user.uid,
            authorName = user.displayName ?: "Artist",
            panelImageUrl = base64Image,
            caption = caption,
            likes = 0,
            timestamp = Timestamp.now()
        )
        postRef.set(post)
    }

    fun observeBookmarks(): Flow<List<Bookmark>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = firestore.collection("users").document(uid).collection("bookmarks")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val bookmarks = snapshot.documents.mapNotNull { it.toObject(Bookmark::class.java) }
                    trySend(bookmarks)
                }
            awaitClose { listener.remove() }
        }
    }

    suspend fun toggleBookmark(comic: Comic, isCurrentlyBookmarked: Boolean) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        val docRef = firestore.collection("users").document(uid).collection("bookmarks").document(comic.id)
        if (isCurrentlyBookmarked) {
            docRef.delete()
        } else {
            val bookmark = Bookmark(
                comicId = comic.id,
                title = comic.title,
                coverUrl = comic.coverUrl,
                timestamp = Timestamp.now()
            )
            docRef.set(bookmark)
        }
    }

    fun observeCollaborations(): Flow<List<Collaboration>> = callbackFlow {
        val listener = firestore.collection("collaborations")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val collabs = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Collaboration::class.java)?.copy(id = doc.id)
                }
                trySend(collabs)
            }
        awaitClose { listener.remove() }
    }

    suspend fun createCollaboration(title: String, initialScript: String) {
        val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
        val docRef = firestore.collection("collaborations").document()
        val collab = Collaboration(
            id = docRef.id,
            title = title,
            hostUserId = user.uid,
            hostUserName = user.displayName ?: "Creator",
            currentPanelScript = initialScript,
            status = "Open",
            updatedAt = Timestamp.now()
        )
        docRef.set(collab)
    }

    suspend fun updateCollaborationScript(collabId: String, newScript: String) {
        val docRef = firestore.collection("collaborations").document(collabId)
        docRef.update(
            mapOf(
                "currentPanelScript" to newScript,
                "updatedAt" to Timestamp.now()
            )
        )
    }

    suspend fun saveComicPanelState(
        title: String,
        bitmap: Bitmap?,
        sketchType: String,
        colorStyle: String,
        animationMode: String,
        bubbles: List<SpeechBubbleItem>,
        stickers: List<StickerItem>,
        soundEffects: List<SoundEffectItem>
    ): String {
        val user = auth.currentUser ?: throw IllegalStateException("User not authenticated")
        val panelRef = firestore.collection("users").document(user.uid).collection("saved_panels").document()

        val base64Image = bitmap?.let { bitmapToBase64(it) } ?: ""

        val bubblesJson = JSONArray().apply {
            bubbles.forEach { b ->
                put(JSONObject().apply {
                    put("id", b.id)
                    put("text", b.text)
                    put("type", b.type)
                    put("xRatio", b.xRatio.toDouble())
                    put("yRatio", b.yRatio.toDouble())
                    put("scale", b.scale.toDouble())
                })
            }
        }.toString()

        val stickersJson = JSONArray().apply {
            stickers.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("emojiOrIcon", s.emojiOrIcon)
                    put("label", s.label)
                    put("category", s.category)
                    put("xRatio", s.xRatio.toDouble())
                    put("yRatio", s.yRatio.toDouble())
                    put("scale", s.scale.toDouble())
                    put("rotation", s.rotation.toDouble())
                })
            }
        }.toString()

        val sfxJson = JSONArray().apply {
            soundEffects.forEach { s ->
                put(JSONObject().apply {
                    put("id", s.id)
                    put("text", s.text)
                    put("colorHex", s.colorHex)
                    put("xRatio", s.xRatio.toDouble())
                    put("yRatio", s.yRatio.toDouble())
                    put("scale", s.scale.toDouble())
                    put("rotation", s.rotation.toDouble())
                })
            }
        }.toString()

        val savedState = SavedComicPanelState(
            id = panelRef.id,
            userId = user.uid,
            title = title,
            baseImageBase64 = base64Image,
            sketchType = sketchType,
            colorStyle = colorStyle,
            animationMode = animationMode,
            bubblesJson = bubblesJson,
            stickersJson = stickersJson,
            soundEffectsJson = sfxJson,
            createdAt = Timestamp.now()
        )

        panelRef.set(savedState)
        return panelRef.id
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
