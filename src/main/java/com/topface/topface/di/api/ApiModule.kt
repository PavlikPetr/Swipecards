package com.topface.topface.di.api

import android.content.Context
import com.google.gson.JsonObject
import com.topface.scruffy.ICounterUpdater
import com.topface.scruffy.IUserAgentProvider
import com.topface.scruffy.ScruffyManager
import com.topface.scruffy.utils.objectFromJson
import com.topface.topface.App
import com.topface.topface.api.Api
import com.topface.topface.api.DeleteFeedRequestFactory
import com.topface.topface.api.FeedRequestFactory
import com.topface.topface.data.BalanceData
import com.topface.topface.data.CountersData
import com.topface.topface.state.TopfaceAppState
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
    fun provideScruffyManager(context: Context, links: Array<String>, userAgent: String, state: TopfaceAppState) =
            ScruffyManager(context, links, object : IUserAgentProvider {
                override fun createUserAgent() = userAgent
            }).apply {
                counterUpdater = object : ICounterUpdater {
                    override fun onCounters(unread: JsonObject) {
                        state.setData(unread.objectFromJson(CountersData::class.java))
                    }

                    override fun onBalance(balance: JsonObject) {
                        state.setData(balance.objectFromJson(BalanceData::class.java))
                    }

                }
            }

    @Provides
    @Singleton
    fun provideDeleteFeedRequestFactory() = DeleteFeedRequestFactory()

    @Provides
    @Singleton
    fun provideFeedRequestFactory() = FeedRequestFactory()

    @Provides
    @Singleton
    fun provideApi(deleteFeedRequestFactory: DeleteFeedRequestFactory,
                   feedRequestFactory: FeedRequestFactory, mScruffyManager: ScruffyManager) = Api(deleteFeedRequestFactory, feedRequestFactory, mScruffyManager)
}