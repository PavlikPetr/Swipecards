package com.topface.topface.ui.fragments.feed.feed_api

import android.content.Context
import android.os.Bundle
import com.topface.topface.data.FeedItem
import com.topface.topface.data.FeedListData
import com.topface.topface.data.Rate
import com.topface.topface.requests.*
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.requests.handlers.SimpleApiHandler
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.http.IRequestClient
import rx.Observable
import java.util.*

/** Набор методов для взаимодействия с сервером в фидах
 * Created by tiberal on 09.08.16.
 */
class FeedApi(private val mContext: Context, private val mRequestClient: IRequestClient,
              private val mDeleteRequestFactory: IRequestFactory, private val mFeedRequestFactory: IRequestFactory) {

    fun callLikesAccessRequest(): Observable<IApiResponse> {
        return Observable.create {
            val request = BuyLikesAccessRequest(mContext)
            mRequestClient.registerRequest(request)
            request.callback(object : SimpleApiHandler() {
                override fun success(response: IApiResponse) = it.onNext(response)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }).exec()
        }
    }

    fun <T : FeedItem> callUpdate(isForPremium: Boolean, mItemClass: Class<T>, requestArgs: Bundle): Observable<FeedListData<T>> {
        return Observable.create {
            val request = mFeedRequestFactory.construct(requestArgs)
            if (request != null) {
                mRequestClient.registerRequest(request)
                request.callback(object : DataApiHandler<FeedListData<T>>() {
                    override fun parseResponse(response: ApiResponse): FeedListData<T> = FeedListData(response.jsonResult, mItemClass)
                    override fun success(data: FeedListData<T>, response: IApiResponse) = it.onNext(data)
                    override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                    override fun isShowPremiumError() = !isForPremium
                    override fun always(response: IApiResponse) {
                        super.always(response)
                        it.onCompleted()
                    }
                }).exec()
            } else {
                Utils.showErrorMessage()
            }
        }

    }

    fun callAddToBlackList(items: List<FeedItem>): Observable<Boolean> {
        val ids = getFeedIntIds(items)
        return Observable.create {
            val request = BlackListAddRequest(ids, mContext).callback(BlackListAndBookmarkHandler(mContext, BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                    ids, true, object : ApiHandler() {
                override fun success(response: IApiResponse) = it.onNext(true)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }))
            mRequestClient.registerRequest(request)
            request.exec()
        }
    }

    fun callDelete(feedsType: FeedsCache.FEEDS_TYPE, ids: List<FeedItem>): Observable<Boolean> {
        return Observable.create {
            val arg = Bundle()
            arg.putStringArrayList(DeleteFeedRequestFactory.USER_ID_FOR_DELETE, getFeedStringIds(ids))
            arg.putSerializable(DeleteFeedRequestFactory.FEED_TYPE, feedsType)
            val deleteFeedsRequest = mDeleteRequestFactory.construct(arg)
            if (deleteFeedsRequest != null) {
                deleteFeedsRequest.callback(object : SimpleApiHandler() {
                    override fun success(response: IApiResponse) = it.onNext(true)
                    override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                    override fun always(response: IApiResponse) {
                        super.always(response)
                        it.onCompleted()
                    }
                })
                mRequestClient.registerRequest(deleteFeedsRequest)
                deleteFeedsRequest.exec()
            } else {
                Utils.showErrorMessage()
            }
        }
    }

    fun callSendLike(userId: Int, blockUnconfirmed: Boolean): Observable<Rate> {
        return Observable.create {
            val request = SendLikeRequest(mContext, userId, SendLikeRequest.DEFAULT_NO_MUTUAL, SendLikeRequest.FROM_FEED, blockUnconfirmed)
            request.callback(object : DataApiHandler<Rate>() {
                override fun success(rate: Rate, response: IApiResponse) = it.onNext(rate)
                override fun parseResponse(response: ApiResponse) = Rate.parse(response)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            })
            mRequestClient.registerRequest(request)
            request.exec()
        }
    }

    private fun getFeedIntIds(list: List<FeedItem>): ArrayList<Int> {
        val ids = ArrayList<Int>()
        list.filter {
            !it.isLoaderOrRetrier
        }.forEach {
            ids.add(it.id.toInt())
        }
        return ids
    }

    private fun getFeedStringIds(list: List<FeedItem>): ArrayList<String> {
        val ids = ArrayList<String>()
        list.filter {
            !it.isLoaderOrRetrier
        }.forEach {
            ids.add(it.id)
        }
        return ids
    }

}