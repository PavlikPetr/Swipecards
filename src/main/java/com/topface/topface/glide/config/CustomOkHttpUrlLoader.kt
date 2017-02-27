package com.topface.topface.glide.config

import android.content.Context
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.GenericLoaderFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import okhttp3.Call
import okhttp3.OkHttpClient
import java.io.InputStream
import java.util.concurrent.TimeUnit

// ПУСТЬ БУДЕТ КАК ПРИМЕР КАСТОМИЗАЦИИ okhttp3 ДЛЯ ИСПОЛЬЗОВАНИЯ С GLIDE

/**
 * A simple model loader for fetching media over http/https using OkHttp.
 * Created by ppavlik on 15.02.17.
 */
//class CustomOkHttpUrlLoader(private val client: Call.Factory?) : ModelLoader<GlideUrl, InputStream> {
//
//    override fun getResourceFetcher(model: GlideUrl, width: Int, height: Int): DataFetcher<InputStream> {
//        return OkHttpStreamFetcher(client, model)
//    }
//
//    /**
//     * The default factory for [CustomOkHttpUrlLoader]s.
//     */
//    class Factory
//    /**
//     * Constructor for a new Factory that runs requests using given client.
//
//     * @param client this is typically an instance of `OkHttpClient`.
//     */
//    @JvmOverloads constructor(private val client: Call.Factory? = CustomOkHttpUrlLoader.Factory.getInternalClient()) : ModelLoaderFactory<GlideUrl, InputStream> {
//
//        override fun build(context: Context, factories: GenericLoaderFactory): ModelLoader<GlideUrl, InputStream> {
//            return CustomOkHttpUrlLoader(client)
//        }
//
//        override fun teardown() {
//            // Do nothing, this instance doesn't own the client.
//        }
//
//        companion object {
//            @Volatile private var myInternalClient: Call.Factory? = null
//
//            private fun getInternalClient(): Call.Factory? {
//                if (myInternalClient == null) {
//                    synchronized(Factory::class.java) {
//                        if (myInternalClient == null) {
//                            myInternalClient = OkHttpClient.Builder()
//                                    .connectTimeout(45, TimeUnit.SECONDS)
//                                    .readTimeout(45, TimeUnit.SECONDS)
//                                    .writeTimeout(45, TimeUnit.SECONDS)
//                                    .retryOnConnectionFailure(true)
//                                    .build()
//                        }
//                    }
//                }
//                return myInternalClient
//            }
//        }
//    }
//}
