package com.topface.topface.api

import android.location.Location
import android.os.Bundle
import com.topface.scruffy.ScruffyManager
import com.topface.topface.api.requests.*
import com.topface.topface.api.responses.*
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.rx.applySchedulers
import rx.Observable
import java.util.*

/**
 * Api interactor
 * Created by tiberal on 06.03.17.
 */
class Api(private val mDeleteRequestFactory: IRequestFactory<Completed>,
          private val mFeedRequestFactory: IFeedRequestFactory,
          private val mScruffyManager: ScruffyManager) : IApi {

    override fun callAppDayRequest(typeFeedFragment: String) =
            AppDayRequest(typeFeedFragment).subscribe()

    override fun callAddToBlackList(items: List<FeedItem>) =
            BlackListAddRequest(items.getFeedIntIds()).subscribe()

    override fun callDelete(feedsType: FeedsCache.FEEDS_TYPE, ids: ArrayList<String>) =
            mDeleteRequestFactory.construct(Bundle().apply {
                putStringArrayList(DeleteFeedRequestFactory.USER_ID_FOR_DELETE, ids)
                putSerializable(DeleteFeedRequestFactory.FEED_TYPE, feedsType)
            }).subscribe()

    override fun <D : FeedItem, T : IBaseFeedResponse> callGetList(args: Bundle, clazz: Class<T>, item: Class<D>): Observable<T> =
            mFeedRequestFactory.construct(args, clazz).subscribe()

    override fun callDialogGet(userId: Int, from: String?, to: String?, leave: Boolean) = DialogGetRequest(userId, from, to, leave).subscribe()

    override fun callSendMessage(userId: Int, message: String, isInstant: Boolean) = SendMessageRequest(userId, message, isInstant).subscribe()

    override fun observeDeleteMessage() = mScruffyManager.mEventManager
            .observeEventInBackground(DeleteMessageRequest.REQUEST_METHOD_NAME, DeleteComplete::class.java)
            .applySchedulers()

    override fun execDeleteMessage(item: HistoryItem) = DeleteMessageRequest(item.id).exec()

    override fun callDeleteMutual(userIds: ArrayList<String>) = DeleteMutualRequest(userIds).subscribe()

    override fun callDeleteAdmiration(itemsId: ArrayList<String>) = DeleteAdmirationRequest(itemsId).subscribe()

    override fun callSetProfile(name: String, age: Int, sex: Int, location: Location?, cityid: Int,
                                status: String, background: Int, invisible: Boolean?, xstatus: Int,
                                isAutoReplyAllowed: Boolean?): Observable<Completed> =
            SettingsRequest(name, age, sex, location, cityid, status, background, invisible, xstatus, isAutoReplyAllowed).subscribe()

    override fun observeSendMessage() = mScruffyManager.mEventManager
            .observeEventInBackground(DialogGetRequest.REQUEST_METHOD_NAME, History::class.java)
            .applySchedulers()

    override fun callBannerGetCommon(startNumber: Long) = BannerSettingsRequest(startNumber).subscribe()
}