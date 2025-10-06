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
        val memoryCacheSize = (Runtime.getRuntime().maxMemory() / 8).toLong()

        Glide.init(this, GlideBuilder()
            .setMemoryCache(LruResourceCache(memoryCacheSize))
            .setDiskCache(
                InternalCacheDiskCacheFactory(
                    this,
                    "glide_cache",
                    100 * 1024 * 1024
                )
            )
        )
    }
}