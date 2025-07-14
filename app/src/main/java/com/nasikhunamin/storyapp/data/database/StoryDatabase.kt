package com.nasikhunamin.storyapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nasikhunamin.storyapp.data.dao.RemoteKeysDao
import com.nasikhunamin.storyapp.data.dao.StoryDao
import com.nasikhunamin.storyapp.data.entity.RemoteKeys
import com.nasikhunamin.storyapp.data.entity.StoryEntity


@Database(
    entities = [StoryEntity::class, RemoteKeys::class],
    version = 1,
    exportSchema = false
)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object{
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java, "story_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}