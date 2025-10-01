package com.example.daily_photo_day.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "photo_posts")
data class PhotoPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imageUri: String,
    val title: String,
    val description: String,
    val location: String,
    val date: String, // Формат: "dd.MM.yyyy HH:mm"
    val originalDate: Long? = null // Timestamp оригинальной даты фото
) {
    fun getYear(): Int {
        return try {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val dateObj = format.parse(date)
            val calendar = java.util.Calendar.getInstance()
            calendar.time = dateObj ?: Date()
            calendar.get(java.util.Calendar.YEAR)
        } catch (e: Exception) {
            val calendar = java.util.Calendar.getInstance()
            calendar.time = Date()
            calendar.get(java.util.Calendar.YEAR)
        }
    }

    companion object {
        fun isValidDate(dateString: String): Boolean {
            return try {
                val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                format.isLenient = false
                format.parse(dateString)
                true
            } catch (e: Exception) {
                false
            }
        }

        fun getCurrentDateTime(): String {
            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return format.format(Date())
        }
    }
}