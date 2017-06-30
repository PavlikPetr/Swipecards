package com.topface.topface.ui.fragments.feed.feed_base

import android.content.Context
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedItem
import com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI
import com.topface.topface.utils.ListOfJson
import com.topface.topface.utils.config.FeedsCache
import java.util.*

/**
 * Реализация кэша фидов на основе нашего конфига
 * Created by tiberal on 15.08.16.
 */
class FeedCacheManager<T : FeedItem>(private val mFeedType: FeedsCache.FEEDS_TYPE) : IFeedCache<T> {

    val mCacheItemsCount = App.getContext().resources.getIntArray(R.array.feed_limit)[CONNECTION_WIFI.int]

    private val mCache by lazy {
        App.getFeedsCache()
    }

    override fun saveToCache(feedList: ArrayList<T>) = if (!feedList.isEmpty()) {
        cacheData(JsonUtils.toJson(prepareCacheItems(feedList)))
    } else {
        cacheData("")
    }

    private fun prepareCacheItems(data: ArrayList<T>): ArrayList<T> {
        val result = mutableListOf<T>()
        data.filter {
            !it.isLoaderOrRetrier && result.count() < mCacheItemsCount
        }.map {
            result.add(it)
        }
        return data
    }


    override fun restoreFromCache(clazz: Class<T>): ArrayList<T>? {
        val fromCacheString = mCache.getFeedFromCache(mFeedType)
        return JsonUtils.fromJson<ArrayList<T>>(fromCacheString, ListOfJson(clazz))
    }

    override fun clearCache() = cacheData("")

    private fun cacheData(value: String) {
        if (mFeedType != FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE) {
            mCache.setFeedToCache(value, mFeedType).saveConfig()
        }
    }
}