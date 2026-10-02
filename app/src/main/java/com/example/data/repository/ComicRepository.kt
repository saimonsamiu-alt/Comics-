package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.ComicCraftDatabase
import com.example.data.local.dao.ComicDao
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.SketchDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ComicEntity
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.SketchEntity
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.toEntity
import com.example.data.model.Bookmark
import com.example.data.model.Chapter
import com.example.data.model.Collaboration
import com.example.data.model.Comic
import com.example.data.model.Comment
import com.example.data.model.FeedPost
import com.example.data.model.Rating
import com.example.data.model.SavedComicPanelState
import com.example.data.model.SoundEffectItem
import com.example.data.model.SpeechBubbleItem
import com.example.data.model.StickerItem
import com.example.data.sample.SampleComicsData
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class ComicRepository(
    private val firestore: FirebaseFirestore,
    private val comicDao: ComicDao? = null,
    private val commentDao: CommentDao? = null,
    private val userDao: UserDao? = null,
    private val sketchDao: SketchDao? = null,
    private val userIdProvider: () -> String? = { Firebase.auth.currentUser?.uid },
    private val userNameProvider: () -> String = { Firebase.auth.currentUser?.displayName ?: "Comic Creator" }
) {
    constructor(context: Context) : this(
        firestore = FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id)),
        comicDao = ComicCraftDatabase.getDatabase(context).comicDao(),
        commentDao = ComicCraftDatabase.getDatabase(context).commentDao(),
        userDao = ComicCraftDatabase.getDatabase(context).userDao(),
        sketchDao = ComicCraftDatabase.getDatabase(context).sketchDao(),
        userIdProvider = { Firebase.auth.currentUser?.uid },
        userNameProvider = { Firebase.auth.currentUser?.displayName ?: "Comic Creator" }
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return userIdProvider()
            ?: throw IllegalStateException("User must be signed in with Google before accessing authenticated features.")
    }

    fun getCurrentUserId(): String? = userIdProvider()
    fun getCurrentUserName(): String = userNameProvider()

    // --- Offline-first Seed ---
    suspend fun seedStarterComicsIfEmpty() = withContext(Dispatchers.IO) {
        comicDao?.let { dao ->
            val count = dao.getCachedComicsCount().firstOrNull() ?: 0
            if (count == 0) {
                dao.insertComics(SampleComicsData.starterComics.map { it.toEntity() })
            }
        }
    }

    // --- Read/Observe Comics (Offline-first with Room + Firestore cloud) ---
    fun observeComics(): Flow<List<Comic>> = flow {
        // Emit locally cached Room comics first for instant, offline-available load
        if (comicDao != null) {
            val cachedEntities = comicDao.getAllCachedComics().firstOrNull() ?: emptyList()
            if (cachedEntities.isNotEmpty()) {
                emit(cachedEntities.map { it.toDomainModel() })
            } else {
                seedStarterComicsIfEmpty()
                emit(SampleComicsData.starterComics)
            }
        }

        val path = "comics"
        val cloudFlow = firestore.collection(path)
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                val comicsList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comic::class.java)?.copy(id = doc.id)
                }
                // Cache into Room database seamlessly
                comicDao?.insertComics(comicsList.map { it.toEntity() })
                comicsList
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                // If network is offline, maintain Room cached stream
                if (comicDao != null) {
                    emitAll(comicDao.getAllCachedComics().map { list -> list.map { it.toDomainModel() } })
                } else {
                    throw error
                }
            }

        emitAll(cloudFlow)
    }.flowOn(Dispatchers.IO)

    suspend fun getComicById(comicId: String): Comic? {
        // Try local Room cache first
        val cached = comicDao?.getComicById(comicId)?.firstOrNull()
        if (cached != null) return cached.toDomainModel()

        return try {
            val doc = firestore.collection("comics").document(comicId).get().await()
            val comic = doc.toObject(Comic::class.java)?.copy(id = doc.id)
            comic?.let { comicDao?.insertComic(it.toEntity()) }
            comic
        } catch (e: Exception) {
            null
        }
    }

    fun observeChapters(comicId: String): Flow<List<Chapter>> = flow {
        val path = "comics/$comicId/chapters"
        emitAll(
            firestore.collection("comics").document(comicId).collection("chapters")
                .orderBy("chapterNumber", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Chapter::class.java)?.copy(id = doc.id)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    // --- Offline-first Comments with Room caching ---
    fun observeComments(comicId: String): Flow<List<Comment>> = flow {
        // Emit Room cached comments first
        if (commentDao != null) {
            val cached = commentDao.getCommentsForComic(comicId).firstOrNull() ?: emptyList()
            if (cached.isNotEmpty()) {
                emit(cached.map { it.toDomainModel() })
            }
        }

        val path = "comics/$comicId/comments"
        val cloudFlow = firestore.collection("comics").document(comicId).collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { snapshot ->
                val commentList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                // Save to Room local cache
                commentDao?.insertComments(commentList.map { it.toEntity() })
                commentList
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                if (commentDao != null) {
                    emitAll(commentDao.getCommentsForComic(comicId).map { list -> list.map { it.toDomainModel() } })
                } else {
                    throw error
                }
            }

        emitAll(cloudFlow)
    }.flowOn(Dispatchers.IO)

    // --- Bookmarks & Progress (User scoped) ---
    fun observeUserBookmarks(userId: String): Flow<List<Bookmark>> = flow {
        val path = "users/$userId/bookmarks"
        emitAll(
            firestore.collection("users").document(userId).collection("bookmarks")
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Bookmark::class.java)?.copy(id = doc.id)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun saveBookmark(bookmark: Bookmark) {
        val uid = requireUserId()
        val path = "users/$uid/bookmarks/${bookmark.comicId}"
        // Immediate local Room cache update
        comicDao?.updateBookmarkState(
            comicId = bookmark.comicId,
            bookmarked = true,
            chapter = bookmark.lastReadChapter,
            percent = bookmark.progressPercent
        )
        try {
            val payload = bookmark.toMap() + mapOf(
                "userId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid).collection("bookmarks")
                .document(bookmark.comicId)
                .set(payload)
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    suspend fun removeBookmark(comicId: String) {
        val uid = requireUserId()
        val path = "users/$uid/bookmarks/$comicId"
        comicDao?.updateBookmarkState(comicId, false, 1, 0)
        try {
            firestore.collection("users").document(uid).collection("bookmarks")
                .document(comicId)
                .delete()
                .await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }

    // --- User Profile Caching in Room ---
    suspend fun cacheUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao?.insertUser(user)
    }

    fun observeCachedUser(): Flow<UserEntity?> = flow {
        if (userDao != null) {
            emitAll(userDao.getActiveUser())
        } else {
            emit(null)
        }
    }.flowOn(Dispatchers.IO)

    fun observeCachedComicsCount(): Flow<Int> = flow {
        if (comicDao != null) {
            emitAll(comicDao.getCachedComicsCount())
        } else {
            emit(0)
        }
    }.flowOn(Dispatchers.IO)

    fun observeCachedCommentsCount(): Flow<Int> = flow {
        if (commentDao != null) {
            emitAll(commentDao.getCachedCommentsCount())
        } else {
            emit(0)
        }
    }.flowOn(Dispatchers.IO)

    // --- Camera Captured Sketches in Room (Offline First) ---
    suspend fun saveCapturedSketch(
        title: String,
        imagePath: String,
        captureSource: String = "CAMERA",
        detectedSubject: String = "Pencil Character",
        inkingContrast: Float = 0.85f,
        colorStyle: String = "Vibrant Cel-Shaded",
        characterColorHex: String = "#FDCB6E",
        auraType: String = "Sparkle"
    ): SketchEntity = withContext(Dispatchers.IO) {
        val sketch = SketchEntity(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            imagePath = imagePath,
            captureSource = captureSource,
            detectedSubject = detectedSubject,
            inkingContrast = inkingContrast,
            colorStyle = colorStyle,
            characterColorHex = characterColorHex,
            auraType = auraType,
            status = "READY_FOR_AI",
            createdAtEpoch = System.currentTimeMillis()
        )
        sketchDao?.insertSketch(sketch)
        sketch
    }

    fun observeSketches(): Flow<List<SketchEntity>> = flow {
        if (sketchDao != null) {
            emitAll(sketchDao.getAllSketches())
        } else {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteSketch(id: String) = withContext(Dispatchers.IO) {
        sketchDao?.deleteSketchById(id)
    }

    suspend fun updateSketch(sketch: SketchEntity) = withContext(Dispatchers.IO) {
        sketchDao?.updateSketch(sketch)
    }

    fun observeSketchCount(): Flow<Int> = flow {
        if (sketchDao != null) {
            emitAll(sketchDao.getSketchCount())
        } else {
            emit(0)
        }
    }.flowOn(Dispatchers.IO)

    // --- Publish / Upload Comic ---
    suspend fun publishComic(
        title: String,
        synopsis: String,
        genre: String,
        coverUrl: String,
        chapterTitle: String,
        panelsJson: String
    ): String {
        val uid = requireUserId()
        val userName = getCurrentUserName()
        val comicDocRef = firestore.collection("comics").document()
        val comicId = comicDocRef.id

        val comicPayload = mapOf(
            "title" to title,
            "synopsis" to synopsis,
            "authorId" to uid,
            "authorName" to userName,
            "genre" to genre,
            "coverUrl" to coverUrl,
            "ratingAverage" to 5.0,
            "ratingCount" to 1,
            "viewCount" to 1,
            "chapterCount" to 1,
            "isPublished" to true,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val chapterDocRef = comicDocRef.collection("chapters").document()
        val chapterPayload = mapOf(
            "comicId" to comicId,
            "title" to chapterTitle,
            "chapterNumber" to 1,
            "panelsJson" to panelsJson,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            firestore.runBatch { batch ->
                batch.set(comicDocRef, comicPayload)
                batch.set(chapterDocRef, chapterPayload)
            }.await()

            // Save to local Room cache
            comicDao?.insertComic(
                Comic(
                    id = comicId,
                    title = title,
                    synopsis = synopsis,
                    authorId = uid,
                    authorName = userName,
                    genre = genre,
                    coverUrl = coverUrl
                ).toEntity()
            )
            return comicId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "comics/$comicId")
            throw e
        }
    }

    // --- Add Comment ---
    suspend fun addComment(comicId: String, content: String) {
        val uid = requireUserId()
        val userName = getCurrentUserName()
        val path = "comics/$comicId/comments"
        val commentDoc = firestore.collection("comics").document(comicId).collection("comments").document()
        val payload = mapOf(
            "comicId" to comicId,
            "userId" to uid,
            "userName" to userName,
            "content" to content,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            commentDoc.set(payload).await()
            // Local Room cache insertion
            commentDao?.insertComment(
                Comment(
                    id = commentDoc.id,
                    comicId = comicId,
                    userId = uid,
                    userName = userName,
                    content = content
                ).toEntity()
            )
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    // --- Rate Comic ---
    suspend fun rateComic(comicId: String, score: Int, currentAvg: Double, currentCount: Int) {
        val uid = requireUserId()
        val path = "comics/$comicId/ratings/$uid"
        val ratingRef = firestore.collection("comics").document(comicId).collection("ratings").document(uid)
        val comicRef = firestore.collection("comics").document(comicId)

        val newCount = currentCount + 1
        val newAvg = ((currentAvg * currentCount) + score) / newCount.toDouble()

        try {
            firestore.runBatch { batch ->
                batch.set(
                    ratingRef,
                    mapOf(
                        "comicId" to comicId,
                        "userId" to uid,
                        "score" to score,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                batch.update(
                    comicRef,
                    mapOf(
                        "ratingAverage" to newAvg,
                        "ratingCount" to newCount,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    // --- Social Feed ---
    fun observeFeedPosts(): Flow<List<FeedPost>> = flow {
        val path = "feed_posts"
        emitAll(
            firestore.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(40)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(FeedPost::class.java)?.copy(id = doc.id)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun shareFeedPost(title: String, description: String, imageUrl: String, characterDesign: String): String {
        val uid = requireUserId()
        val userName = getCurrentUserName()
        val postRef = firestore.collection("feed_posts").document()
        val payload = mapOf(
            "authorId" to uid,
            "authorName" to userName,
            "title" to title,
            "description" to description,
            "imageUrl" to imageUrl,
            "characterDesign" to characterDesign,
            "likesCount" to 0,
            "remixCount" to 0,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            postRef.set(payload).await()
            return postRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "feed_posts")
            throw e
        }
    }

    suspend fun likeFeedPost(postId: String, currentLikes: Int) {
        val postRef = firestore.collection("feed_posts").document(postId)
        try {
            postRef.update(
                mapOf(
                    "likesCount" to currentLikes + 1,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "feed_posts/$postId")
            throw e
        }
    }

    // --- Collaborations ---
    fun observeCollaborations(): Flow<List<Collaboration>> = flow {
        val path = "collaborations"
        emitAll(
            firestore.collection(path)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(20)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Collaboration::class.java)?.copy(id = doc.id)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun createCollaboration(title: String, script: String): String {
        val uid = requireUserId()
        val userName = getCurrentUserName()
        val collabRef = firestore.collection("collaborations").document()
        val payload = mapOf(
            "title" to title,
            "ownerId" to uid,
            "ownerName" to userName,
            "collaboratorIds" to uid,
            "status" to "active",
            "currentPanelScript" to script,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            collabRef.set(payload).await()
            return collabRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "collaborations")
            throw e
        }
    }

    suspend fun updateCollaborationScript(collabId: String, newScript: String) {
        val collabRef = firestore.collection("collaborations").document(collabId)
        try {
            collabRef.update(
                mapOf(
                    "currentPanelScript" to newScript,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "collaborations/$collabId")
            throw e
        }
    }

    // --- Saved Comic Panels (Firestore Cloud Storage for base image, bubbles, and stickers) ---
    suspend fun saveComicPanelState(
        title: String,
        bitmap: Bitmap?,
        sketchType: String,
        colorStyle: String,
        characterColorHex: String,
        auraType: String,
        inkingEdgeContrast: Float,
        isCleanPaperEnabled: Boolean,
        animationMode: String,
        speechBubbles: List<SpeechBubbleItem>,
        stickers: List<StickerItem>,
        soundEffects: List<SoundEffectItem>
    ): String = withContext(Dispatchers.IO) {
        val uid = requireUserId()
        val docRef = firestore.collection("users").document(uid).collection("saved_panels").document()

        // Encode base image to compact base64 data URI if bitmap provided, or built-in reference
        val baseImageString = if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            val maxDim = 512
            val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                val targetW = if (ratio > 1f) maxDim else (maxDim * ratio).toInt()
                val targetH = if (ratio > 1f) (maxDim / ratio).toInt() else maxDim
                Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
            } else {
                bitmap
            }
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
            val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } else {
            "builtin:$sketchType"
        }

        // Build serialized JSON array of speech bubbles
        val bubblesArray = JSONArray()
        speechBubbles.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("text", b.text)
                put("type", b.type)
                put("xRatio", b.xRatio.toDouble())
                put("yRatio", b.yRatio.toDouble())
                put("scale", b.scale.toDouble())
            }
            bubblesArray.put(obj)
        }

        // Build serialized JSON array of stickers
        val stickersArray = JSONArray()
        stickers.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("emojiOrIcon", s.emojiOrIcon)
                put("label", s.label)
                put("category", s.category)
                put("xRatio", s.xRatio.toDouble())
                put("yRatio", s.yRatio.toDouble())
                put("scale", s.scale.toDouble())
                put("rotation", s.rotation.toDouble())
            }
            stickersArray.put(obj)
        }

        // Build serialized JSON array of sound effects
        val sfxArray = JSONArray()
        soundEffects.forEach { sfx ->
            val obj = JSONObject().apply {
                put("id", sfx.id)
                put("text", sfx.text)
                put("colorHex", sfx.colorHex)
                put("xRatio", sfx.xRatio.toDouble())
                put("yRatio", sfx.yRatio.toDouble())
            }
            sfxArray.put(obj)
        }

        val panelsContainer = JSONObject().apply {
            put("bubbles", bubblesArray)
            put("stickers", stickersArray)
            put("soundEffects", sfxArray)
        }
        val panelsJson = panelsContainer.toString()

        val payload = mapOf(
            "userId" to uid,
            "title" to title.ifBlank { "Comic Scene Panel" },
            "baseImage" to baseImageString,
            "sketchType" to sketchType,
            "colorStyle" to colorStyle,
            "characterColor" to characterColorHex,
            "auraType" to auraType,
            "inkingEdgeContrast" to inkingEdgeContrast.toDouble(),
            "isCleanPaperEnabled" to isCleanPaperEnabled,
            "animationMode" to animationMode,
            "panelsJson" to panelsJson,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/$uid/saved_panels")
            throw e
        }
    }

    fun observeSavedPanels(): Flow<List<SavedComicPanelState>> = flow {
        val uid = userIdProvider() ?: return@flow
        val path = "users/$uid/saved_panels"
        emitAll(
            firestore.collection("users").document(uid).collection("saved_panels")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        parseSavedPanelDoc(doc)
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }.flowOn(Dispatchers.IO)

    suspend fun deleteSavedPanel(panelId: String) {
        val uid = requireUserId()
        try {
            firestore.collection("users").document(uid).collection("saved_panels").document(panelId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid/saved_panels/$panelId")
            throw e
        }
    }

    private fun parseSavedPanelDoc(doc: DocumentSnapshot): SavedComicPanelState? {
        val data = doc.data ?: return null
        val bubblesList = mutableListOf<SpeechBubbleItem>()
        val stickersList = mutableListOf<StickerItem>()
        val sfxList = mutableListOf<SoundEffectItem>()

        val panelsJsonStr = data["panelsJson"] as? String ?: ""
        if (panelsJsonStr.isNotBlank()) {
            try {
                val json = JSONObject(panelsJsonStr)
                val bubblesArr = json.optJSONArray("bubbles")
                if (bubblesArr != null) {
                    for (i in 0 until bubblesArr.length()) {
                        val obj = bubblesArr.getJSONObject(i)
                        bubblesList.add(
                            SpeechBubbleItem(
                                id = obj.optString("id", "sb_$i"),
                                text = obj.optString("text", ""),
                                type = obj.optString("type", "Speech"),
                                xRatio = obj.optDouble("xRatio", 0.5).toFloat(),
                                yRatio = obj.optDouble("yRatio", 0.2).toFloat(),
                                scale = obj.optDouble("scale", 1.0).toFloat()
                            )
                        )
                    }
                }

                val stickersArr = json.optJSONArray("stickers")
                if (stickersArr != null) {
                    for (i in 0 until stickersArr.length()) {
                        val obj = stickersArr.getJSONObject(i)
                        stickersList.add(
                            StickerItem(
                                id = obj.optString("id", "stk_$i"),
                                emojiOrIcon = obj.optString("emojiOrIcon", "⭐"),
                                label = obj.optString("label", "Star"),
                                category = obj.optString("category", "Power"),
                                xRatio = obj.optDouble("xRatio", 0.5).toFloat(),
                                yRatio = obj.optDouble("yRatio", 0.5).toFloat(),
                                scale = obj.optDouble("scale", 1.0).toFloat(),
                                rotation = obj.optDouble("rotation", 0.0).toFloat()
                            )
                        )
                    }
                }

                val sfxArr = json.optJSONArray("soundEffects")
                if (sfxArr != null) {
                    for (i in 0 until sfxArr.length()) {
                        val obj = sfxArr.getJSONObject(i)
                        sfxList.add(
                            SoundEffectItem(
                                id = obj.optString("id", "sfx_$i"),
                                text = obj.optString("text", "POW!"),
                                colorHex = obj.optString("colorHex", "#FF5722"),
                                xRatio = obj.optDouble("xRatio", 0.5).toFloat(),
                                yRatio = obj.optDouble("yRatio", 0.5).toFloat()
                            )
                        )
                    }
                }
            } catch (_: Exception) {}
        }

        return SavedComicPanelState(
            id = doc.id,
            userId = data["userId"] as? String ?: "",
            title = data["title"] as? String ?: "Comic Panel",
            baseImage = data["baseImage"] as? String ?: "",
            sketchType = data["sketchType"] as? String ?: "dog",
            colorStyle = data["colorStyle"] as? String ?: "Vibrant Cel-Shaded",
            characterColor = data["characterColor"] as? String ?: "#FDCB6E",
            auraType = data["auraType"] as? String ?: "Sparkle",
            inkingEdgeContrast = (data["inkingEdgeContrast"] as? Number)?.toFloat() ?: 0.85f,
            isCleanPaperEnabled = data["isCleanPaperEnabled"] as? Boolean ?: true,
            animationMode = data["animationMode"] as? String ?: "Camera Pan & Zoom",
            panelsJson = panelsJsonStr,
            speechBubbles = bubblesList,
            stickers = stickersList,
            soundEffects = sfxList,
            updatedAt = data["updatedAt"] as? Timestamp
        )
    }
}
