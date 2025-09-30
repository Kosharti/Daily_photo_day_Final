package com.example.daily_photo_day.utils

import com.example.daily_photo_day.data.entity.PhotoPost
import java.text.SimpleDateFormat
import java.util.Locale

object SearchFilterHelper {

    fun filterPosts(
        posts: List<PhotoPost>,
        query: String? = null,
        sortBy: SortType = SortType.DATE_DESC,
        dateFilter: String? = null
    ): List<PhotoPost> {
        var filtered = posts

        // Поиск по запросу
        if (!query.isNullOrBlank()) {
            filtered = filtered.filter { post ->
                post.title.contains(query, true) ||
                        post.description.contains(query, true) ||
                        post.location.contains(query, true) ||
                        post.date.contains(query, true)
            }
        }

        // Фильтрация по дате
        if (!dateFilter.isNullOrBlank()) {
            filtered = filtered.filter { post ->
                post.date.startsWith(dateFilter)
            }
        }

        // Сортировка
        return when (sortBy) {
            SortType.DATE_DESC -> filtered.sortedByDescending { parseDate(it.date) }
            SortType.DATE_ASC -> filtered.sortedBy { parseDate(it.date) }
            SortType.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortType.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
        }
    }

    private fun parseDate(dateString: String): Long {
        return try {
            val formats = arrayOf(
                "dd.MM.yyyy HH:mm",
                "dd.MM.yyyy"
            )
            formats.forEach { format ->
                try {
                    return SimpleDateFormat(format, Locale.getDefault())
                        .parse(dateString)?.time ?: 0L
                } catch (e: Exception) {
                    // Пробуем следующий формат
                }
            }
            0L
        } catch (e: Exception) {
            0L
        }
    }

    enum class SortType {
        DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC
    }
}