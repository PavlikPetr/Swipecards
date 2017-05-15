package com.topface.topface.api

import android.os.Bundle
import com.topface.topface.api.requests.*
import com.topface.topface.api.responses.Completed
import com.topface.topface.api.responses.HistoryItem
import com.topface.topface.api.responses.IBaseFeedResponse
import com.topface.topface.data.FeedItem
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.utils.config.FeedsCache
import rx.Observable
import java.util.*

/**
 * Api interactor
 * Created by tiberal on 06.03.17.
 */
class Api(private val mDeleteRequestFactory: IRequestFactory<Completed>,
          private val mFeedRequestFactory: IFeedRequestFactory) : IApi {

    override fun callAppDayRequest(typeFeedFragment: String) =
            AppDayRequest(typeFeedFragment).subscribe()

    override fun callAddToBlackList(items: List<FeedItem>) =
            BlackListAddRequest(items.getFeedIntIds()).subscribe()

    override fun callDelete(feedsType: FeedsCache.FEEDS_TYPE, ids: ArrayList<String>) =
            mDeleteRequestFactory.construct(Bundle().apply {
                putStringArrayList(DeleteFeedRequestFactory.USER_ID_FOR_DELETE, ids)
                putSerializable(DeleteFeedRequestFactory.FEED_TYPE, feedsType)
            }).subscribe()

    override fun callDeleteMessage(item: HistoryItem) = DeleteMessageRequest(item.id).subscribe()

    override fun <D : FeedItem, T : IBaseFeedResponse> callGetList(args: Bundle, clazz: Class<T>, item: Class<D>): Observable<T> =
            mFeedRequestFactory.construct(args, clazz).subscribe()

    override fun callDialogGet(userId: Int, from: String?, to: String?) = DialogGetRequest(userId, from, to).subscribe()

    override fun callSendMessage(userId: Int, message: String, isInstant: Boolean) = SendMessageRequest(userId, message, isInstant).subscribe()

}