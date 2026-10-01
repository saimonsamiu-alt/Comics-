package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ComicDao
import com.example.data.local.dao.CommentDao
import com.example.data.local.dao.SketchDao
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.ComicEntity
import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.SketchEntity
import com.example.data.local.entity.UserEntity

@Database(
    entities = [ComicEntity::class, CommentEntity::class, UserEntity::class, SketchEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ComicCraftDatabase : RoomDatabase() {

    abstract fun comicDao(): ComicDao
    abstract fun commentDao(): CommentDao
    abstract fun userDao(): UserDao
    abstract fun sketchDao(): SketchDao

    companion object {
        @Volatile
        private var INSTANCE: ComicCraftDatabase? = null

        fun getDatabase(context: Context): ComicCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ComicCraftDatabase::class.java,
                    "comiccraft_offline.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
