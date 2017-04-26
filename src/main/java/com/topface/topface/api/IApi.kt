package com.topface.topface.api

import android.os.Bundle
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.FeedItem
import com.topface.topface.data.History
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.utils.config.FeedsCache
import rx.Observable
import java.util.*

/**
 * Интерфейс апи. Мало ли тестить захотим и придется мокать.
 * Created by tiberal on 06.03.17.
 */
interface IApi {

    fun callAppDayRequest(typeFeedFragment: String): Observable<AppDay>

    fun callAddToBlackList(items: List<FeedItem>): Observable<Completed>

    fun deleteMessage(item: History): Observable<Completed>

    fun callDelete(feedsType: FeedsCache.FEEDS_TYPE, ids: ArrayList<String>): Observable<Completed>

    fun <D : FeedItem, T : IBaseFeedResponse> callGetList(args: Bundle, clazz: Class<T>, item: Class<D>): Observable<T>

}