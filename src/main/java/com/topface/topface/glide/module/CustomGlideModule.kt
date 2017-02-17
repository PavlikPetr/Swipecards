package com.topface.topface.glide.module

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.module.GlideModule
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache


/**
 * Created by petrp on 17.02.2017.
 */
class CustomGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val yourSizeInBytes = 104857600
        builder.setDiskCache(
                InternalCacheDiskCacheFactory(context, yourSizeInBytes))
        builder.setMemoryCache(LruResourceCache(yourSizeInBytes));
        // Apply options to the builder here.
    }

    override fun registerComponents(context: Context, glide: Glide) {
        // register ModelLoaders here.
    }
}