package com.example.daily_photo_day.repository

import com.example.daily_photo_day.data.dao.PhotoPostDao
import com.example.daily_photo_day.data.entity.PhotoPost
import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val photoPostDao: PhotoPostDao) {

    fun getAllPosts(): Flow<List<PhotoPost>> = photoPostDao.getAllPosts()

    suspend fun getPostById(id: Long): PhotoPost? = photoPostDao.getPostById(id)

    suspend fun addPost(post: PhotoPost): Long = photoPostDao.insertPost(post)

    suspend fun updatePost(post: PhotoPost) = photoPostDao.updatePost(post)

    suspend fun deletePost(post: PhotoPost) = photoPostDao.deletePost(post)
}