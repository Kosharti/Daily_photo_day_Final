package com.example.daily_photo_day.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daily_photo_day.data.AppDatabase
import com.example.daily_photo_day.data.entity.PhotoPost
import com.example.daily_photo_day.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PhotoRepository
    private val _allPosts = MutableStateFlow<List<PhotoPost>>(emptyList())
    val allPosts: StateFlow<List<PhotoPost>> = _allPosts.asStateFlow()

    init {
        val photoPostDao = AppDatabase.getDatabase(application).photoPostDao()
        repository = PhotoRepository(photoPostDao)
        repository.getAllPosts().onEach { posts ->
            _allPosts.value = posts
        }.launchIn(viewModelScope)
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