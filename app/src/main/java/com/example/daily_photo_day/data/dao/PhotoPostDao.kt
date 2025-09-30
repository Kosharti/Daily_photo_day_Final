package com.example.daily_photo_day.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.daily_photo_day.data.entity.PhotoPost
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoPostDao {
    @Query("SELECT * FROM photo_posts ORDER BY date DESC")
    fun getAllPosts(): Flow<List<PhotoPost>>

    @Query("SELECT * FROM photo_posts WHERE id = :id")
    suspend fun getPostById(id: Long): PhotoPost?

    @Insert
    suspend fun insertPost(post: PhotoPost): Long

    @Update
    suspend fun updatePost(post: PhotoPost)

    @Delete
    suspend fun deletePost(post: PhotoPost)
}