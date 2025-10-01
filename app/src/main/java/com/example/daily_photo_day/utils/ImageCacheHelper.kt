package com.example.daily_photo_day.utils

import android.content.Context
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageCacheHelper {

    /**
     * Предварительная загрузка изображений в кэш
     */
    suspend fun preloadImages(context: Context, imageUris: List<String>) {
        withContext(Dispatchers.IO) {
            imageUris.forEach { uri ->
                try {
                    Glide.with(context)
                        .load(uri)
                        .preload() // Загружаем в кэш без отображения
                } catch (e: Exception) {
                    // Игнорируем ошибки предзагрузки
                }
            }
        }
    }

    /**
     * Очистка кэша Glide
     */
    fun clearCache(context: Context) {
        Glide.get(context).clearMemory() // Очищаем память (выполнять в UI потоке)

        // Очищаем диск в фоне
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }

    /**
     * Получить размер кэша (приблизительно)
     */
    suspend fun getCacheSize(context: Context): Long {
        return withContext(Dispatchers.IO) {
            // В реальном приложении здесь была бы логика подсчёта размера кэша
            // Для простоты возвращаем 0
            0L
        }
    }
}