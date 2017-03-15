package com.topface.topface.di.api

import android.content.Context
import com.topface.scruffy.IUserAgentProvider
import com.topface.scruffy.ScruffyManager
import com.topface.topface.App
import com.topface.topface.utils.http.HttpUtils
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Модуль скраффи
 * Created by tiberal on 06.03.17.
 */
@Module
class ApiModule {

    @Provides
    @Singleton
    fun provideConnectionLinks() = arrayOf(App.getAppConfig().scruffyApiUrl)

    @Provides
    @Singleton
    fun provideUserAgent(): String = HttpUtils.getUserAgent("Scruffy/2")

    @Provides
    @Singleton
    fun provideScruffyManager(context: Context, links: Array<String>, userAgent: String) =
            ScruffyManager(context, links, object : IUserAgentProvider {
                override fun createUserAgent() = userAgent
            })
}