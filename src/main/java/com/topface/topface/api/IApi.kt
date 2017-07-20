package com.topface.topface.api

import android.location.Location
import android.os.Bundle
import com.topface.topface.App
import com.topface.topface.api.responses.*
import com.topface.topface.data.AdsSettings
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.utils.Utils
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

    fun callDelete(feedsType: FeedsCache.FEEDS_TYPE, ids: ArrayList<String>): Observable<Completed>

    fun <D : FeedItem, T : IBaseFeedResponse> callGetList(args: Bundle, clazz: Class<T>, item: Class<D>): Observable<T>

    fun callDialogGet(userId: Int, from: String? = null, to: String? = null, leave: Boolean = false): Observable<History>

    fun callSendMessage(userId: Int, message: String, isInstant: Boolean = false): Observable<HistoryItem>

    fun execDeleteMessage(item: HistoryItem)

    fun observeDeleteMessage(): Observable<DeleteComplete>

    fun execAddToBlackList(itemId: List<Int>)

    fun observeAddToBlackList(): Observable<Completed>

    fun execDeleteMutual(userIds: ArrayList<String>)

    fun observeDeleteMutual(): Observable<DeleteComplete>

    fun execDeleteAdmiration(itemsId: ArrayList<String>)

    fun observeDeleteAdmiration(): Observable<DeleteComplete>

    fun callSetProfile(name: String = Utils.EMPTY, age: Int = -1, sex: Int = -1, location: Location? = null,
                       cityid: Int = -1, status: String = Utils.EMPTY, background: Int = -1, invisible: Boolean? = null,
                       xstatus: Int = -1, isAutoReplyAllowed: Boolean? = null): Observable<Completed>

    fun callBannerGetCommon(startNumber: Long = App.getUserConfig().getBannerInterval<Long>()
            .getConfigFieldInfo().getAmount()): Observable<AdsSettings>

    fun observeSendMessage(): Observable<History>
}