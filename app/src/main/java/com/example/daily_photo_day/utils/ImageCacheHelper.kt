package com.example.daily_photo_day.utils

import android.content.Context
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageCacheHelper {

    suspend fun preloadImages(context: Context, imageUris: List<String>) {
        withContext(Dispatchers.IO) {
            imageUris.forEach { uri ->
                try {
                    Glide.with(context)
                        .load(uri)
                        .preload()
                } catch (e: Exception) {
                }
            }
        }
    }

    fun clearCache(context: Context) {
        Glide.get(context).clearMemory()

        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }

    suspend fun getCacheSize(context: Context): Long {
        return withContext(Dispatchers.IO) {
            0L
        }
    }
}