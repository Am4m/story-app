package com.nasikhunamin.storyapp.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nasikhunamin.storyapp.data.entity.StoryEntity

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<StoryEntity>)

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    fun getAllStory(): PagingSource<Int, StoryEntity>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}