package com.example.daily_photo_day

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        setupGlideCache()
    }

    private fun setupGlideCache() {
        val memoryCacheSize = (Runtime.getRuntime().maxMemory() / 8).toLong() // 1/8 от доступной памяти

        Glide.init(this, GlideBuilder()
            .setMemoryCache(LruResourceCache(memoryCacheSize)) // Кэш в оперативной памяти
            .setDiskCache(
                InternalCacheDiskCacheFactory(
                    this,
                    "glide_cache",
                    100 * 1024 * 1024 // 100 MB на диске
                )
            )
        )
    }
}