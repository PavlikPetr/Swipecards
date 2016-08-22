package com.topface.topface.ui.fragments.feed.feed_base

import java.util.*

/**
 * Интерфейс для кэширования фидов
 * Created by tiberal on 15.08.16.
 */
interface IFeedCache<T> {
    fun saveToCache(feedList: ArrayList<T>)
    fun restoreFromCache(clazz: Class<T>): ArrayList<T>?
    fun clearCache()
}