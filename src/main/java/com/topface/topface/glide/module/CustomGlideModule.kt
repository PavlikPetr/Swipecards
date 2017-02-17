package com.topface.topface.glide.module

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator
import com.bumptech.glide.module.GlideModule


/**
 * Кастомизируем настройки glide
 * Created by petrp on 17.02.2017.
 */
class CustomGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // размер дискового кеша возьмем 100Мб
        val yourSizeInBytes = 104857600
        builder.setDiskCache(
                InternalCacheDiskCacheFactory(context, yourSizeInBytes))

        with(MemorySizeCalculator(context)) {
            // берем 70% от дефолта memory cache и bitmap pool
            builder.setMemoryCache(LruResourceCache((0.7 * getMemoryCacheSize()).toInt()))
            builder.setBitmapPool(LruBitmapPool((0.7 * getBitmapPoolSize()).toInt()))
        }
    }

    override fun registerComponents(context: Context, glide: Glide) {
        // register ModelLoaders here.
    }
}