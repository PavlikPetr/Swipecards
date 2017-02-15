package com.topface.topface.modules

import android.content.Context
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.module.GlideModule
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit


/**
 * Настройка для Glide
 * Created by ppavlik on 15.02.17.
 */
//class CustomGlideModule : GlideModule {
//    override fun applyOptions(context: Context, builder: GlideBuilder) {
//        // Apply options to the builder here.
//    }
//
//    override fun registerComponents(context: Context, glide: Glide) {
//        // register ModelLoaders here.
////        val client = OkHttpClient.Builder()
////                .connectTimeout(45, TimeUnit.SECONDS)
////                .readTimeout(45, TimeUnit.SECONDS)
////                .writeTimeout(45, TimeUnit.SECONDS)
////
////        val factory = OkHttpUrlLoader.Factory(client.build())
////
////        glide.register(GlideUrl::class.java, InputStream::class.java, factory)
//    }
//}
