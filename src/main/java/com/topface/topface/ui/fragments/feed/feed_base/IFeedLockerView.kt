package com.topface.topface.ui.fragments.feed.feed_base

/**
 * Интрефейс для взаимодействия с заглушками
 * Created by tiberal on 10.08.16.
 */
interface IFeedLockerView {
    fun onFilledFeed()
    fun onEmptyFeed()
    fun onLockedFeed(errorCode: Int)
}