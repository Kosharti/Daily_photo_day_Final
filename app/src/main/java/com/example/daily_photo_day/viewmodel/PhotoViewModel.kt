package com.example.daily_photo_day.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily_photo_day.data.AppDatabase
import com.example.daily_photo_day.data.entity.PhotoPost
import com.example.daily_photo_day.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository
    val allPosts: Flow<List<PhotoPost>>

    init {
        val photoPostDao = AppDatabase.getDatabase(application).photoPostDao()
        repository = PhotoRepository(photoPostDao)
        allPosts = repository.getAllPosts()
    }

    suspend fun getPostById(id: Long): PhotoPost? {
        return repository.getPostById(id)
    }

    fun getPostsByDate(date: String): Flow<List<PhotoPost>> {
        return allPosts.map { posts ->
            posts.filter { it.date.startsWith(date) }
        }
    }

    fun addPost(post: PhotoPost) = viewModelScope.launch {
        repository.addPost(post)
    }

    fun updatePost(post: PhotoPost) = viewModelScope.launch {
        repository.updatePost(post)
    }

    fun deletePost(post: PhotoPost) = viewModelScope.launch {
        repository.deletePost(post)
    }
}