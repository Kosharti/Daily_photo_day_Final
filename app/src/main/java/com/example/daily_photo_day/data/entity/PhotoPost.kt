package com.example.daily_photo_day.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_posts")
data class PhotoPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val title: String,
    val description: String,
    val location: String,
    val date: String
)