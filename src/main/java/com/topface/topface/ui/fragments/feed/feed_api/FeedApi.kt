package com.topface.topface.ui.fragments.feed.feed_api

import android.content.Context
import android.os.Bundle
import android.os.Looper
import com.topface.framework.JsonUtils
import com.topface.topface.App
import com.topface.topface.data.*
import com.topface.topface.data.search.SearchUser
import com.topface.topface.data.search.UsersList
import com.topface.topface.requests.*
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler
import com.topface.topface.requests.handlers.SimpleApiHandler
import com.topface.topface.requests.response.DialogContacts
import com.topface.topface.requests.response.SimpleResponse
import com.topface.topface.ui.edit.filter.model.FilterData
import com.topface.topface.ui.fragments.feed.app_day.AppDay
import com.topface.topface.utils.Utils
import com.topface.topface.utils.config.FeedsCache
import com.topface.topface.utils.http.IRequestClient
import com.topface.topface.utils.loadcontollers.AlbumLoadController
import rx.Observable
import java.util.*

/**
 * Набор методов для взаимодействия с сервером в фидах
 * Created by tiberal on 09.08.16.
 */

class FeedApi(private val mContext: Context, private var mRequestClient: IRequestClient? = null,
              private val mDeleteRequestFactory: IRequestFactory? = null, private val mFeedRequestFactory: IRequestFactory? = null) {

    fun callStandartMessageRequest(userId: Int, messageId: Int, blockUnconfirmed: Boolean = App.get().options.blockUnconfirmed): Observable<IApiResponse> {
        return Observable.create {
            val request = StandardMessageSendRequest(mContext, userId, messageId, blockUnconfirmed)
            mRequestClient?.registerRequest(request)
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

    fun callGetGifts(userId: Int, from: Int, limit: Int = 15): Observable<Profile.Gifts> {
        return Observable.create {
            val request = FeedGiftsRequest(mContext)
            request.uid = userId
            request.from = from
            request.limit = limit
            mRequestClient?.registerRequest(request)
            request.callback(object : DataApiHandler<FeedListData<FeedGift>>() {
                override fun success(data: FeedListData<FeedGift>?, response: IApiResponse?) {
                    val gifts = Profile.Gifts()
                    data?.let {
                        it.items?.forEach {
                            gifts.items.add(it.gift)
                        }
                        gifts.more = it.more
                    }
                    it.onNext(gifts)
                }

                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))

                override fun parseResponse(response: ApiResponse?): FeedListData<FeedGift>? =
                        response?.let { FeedListData(it.getJsonResult(), FeedGift::class.java) }

                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }

            }).exec()
        }
    }

    fun callAlbumRequest(currentSearchUser: SearchUser, loadedPosition: Int): Observable<AlbumPhotos> {
        return callAlbumRequest(currentSearchUser.id, loadedPosition, AlbumRequest.MODE_SEARCH, AlbumLoadController.FOR_PREVIEW)
    }

    fun callAlbumRequest(uid: Int, loadedPosition: Int, mode: String, type: Int): Observable<AlbumPhotos> {
        return Observable.create {
            val request = AlbumRequest(mContext, uid, loadedPosition, mode, type)
            request.callback(object : DataApiHandler<AlbumPhotos>() {
                override fun success(data: AlbumPhotos, response: IApiResponse) = it.onNext(data)
                override fun parseResponse(response: ApiResponse) = AlbumPhotos(response)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
            }).exec()
        }
    }

    /**
     * @param isNeedRefresh обнулить кэш на сервере и показать юзеров заново
     */
    fun callDatingUpdate(onlyOnline: Boolean, isNeedRefresh: Boolean): Observable<UsersList<SearchUser>> {
        return Observable.create {
            val request = SearchRequest(onlyOnline, mContext, isNeedRefresh, true, true)
            mRequestClient?.registerRequest(request)
            request.callback(object : DataApiHandler<UsersList<SearchUser>>() {
                override fun parseResponse(response: ApiResponse): UsersList<SearchUser> = UsersList(response, SearchUser::class.java)
                override fun success(data: UsersList<SearchUser>, response: IApiResponse) = it.onNext(data)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }).exec()
        }
    }

    fun callFilterRequest(filter: FilterData): Observable<DatingFilter> {
        return Observable.create {
            val request = FilterRequest(filter, mContext)
            mRequestClient?.registerRequest(request)
            request.callback(object : DataApiHandler<DatingFilter>() {
                override fun parseResponse(response: ApiResponse): DatingFilter = DatingFilter(response.getJsonResult())
                override fun success(dataFilter: DatingFilter, response: IApiResponse) = it.onNext(dataFilter)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                override fun cancel() {
                    super.cancel()
                    it.onCompleted()
                }

                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }).exec()
        }
    }

    fun callResetFilterRequest(): Observable<DatingFilter> {
        return Observable.create {
            val request = ResetFilterRequest(mContext)
            mRequestClient?.registerRequest(request)
            request.callback(object : DataApiHandler<DatingFilter>() {
                override fun parseResponse(response: ApiResponse) = DatingFilter(response.getJsonResult())
                override fun success(dataFilter: DatingFilter, response: IApiResponse) = it.onNext(dataFilter)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                override fun cancel() {
                    super.cancel()
                    it.onCompleted()
                }

                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }).exec()
        }
    }

    fun callLikesAccessRequest(): Observable<IApiResponse> {
        return Observable.create {
            val request = BuyLikesAccessRequest(mContext)
            mRequestClient?.registerRequest(request)
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

    fun callAppDayRequest(typeFeedFragment: String): Observable<AppDay> {
        return Observable.create {
            val request = AppDayRequest(mContext, typeFeedFragment)
            mRequestClient?.registerRequest(request)
            request.callback(object : DataApiHandler<AppDay>() {
                override fun success(data: AppDay?, response: IApiResponse?) =
                        it.onNext(data)

                override fun parseResponse(response: ApiResponse?): AppDay =
                        JsonUtils.fromJson(response?.jsonResult.toString(), AppDay::class.java)

                override fun fail(codeError: Int, response: IApiResponse?) =
                        it.onError(Throwable(codeError.toString()))

            }).exec()
        }
    }

    fun <T : FeedItem> callFeedUpdate(isForPremium: Boolean, mItemClass: Class<T>, requestArgs: Bundle): Observable<FeedListData<T>> {
        return Observable.create {
            val request = mFeedRequestFactory?.construct(requestArgs)
            if (request != null) {
                mRequestClient?.registerRequest(request)
                request.callback(object : DataApiHandler<FeedListData<T>>(Looper.getMainLooper()) {
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

    fun callNewGeo(lat: Double, lon: Double): Observable<FeedListData<FeedGeo>> {
        return Observable.create {
            val request = PeopleNearbyRequest(mContext, lat, lon)
            request.callback(object : DataApiHandler<FeedListData<FeedGeo>>() {

                override fun success(data: FeedListData<FeedGeo>, response: IApiResponse) = it.onNext(data)

                override fun parseResponse(response: ApiResponse): FeedListData<FeedGeo> = FeedListData(response.jsonResult, FeedGeo::class.java)

                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Throwable(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            }).exec()
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
            mRequestClient?.registerRequest(request)
            request.exec()
        }
    }

    fun callPeopleNearbyAccess(): Observable<IApiResponse> {
        return Observable.create {
            with(PeopleNearbyAccessRequest(mContext)) {
                callback(object : SimpleApiHandler() {
                    override fun success(response: IApiResponse) = it.onNext(response)
                    override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                    override fun always(response: IApiResponse) {
                        super.always(response)
                        it.onCompleted()
                    }
                })
                mRequestClient?.registerRequest(this)
                exec()
            }
        }
    }

    fun callDelete(feedsType: FeedsCache.FEEDS_TYPE, ids: ArrayList<String>): Observable<Boolean> {
        return Observable.create {
            val arg = Bundle()
            arg.putStringArrayList(DeleteFeedRequestFactory.USER_ID_FOR_DELETE, ids)
            arg.putSerializable(DeleteFeedRequestFactory.FEED_TYPE, feedsType)
            val deleteFeedsRequest = mDeleteRequestFactory?.construct(arg)
            if (deleteFeedsRequest != null) {
                deleteFeedsRequest.callback(object : SimpleApiHandler() {
                    override fun success(response: IApiResponse) = it.onNext(true)
                    override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                    override fun always(response: IApiResponse) {
                        super.always(response)
                        it.onCompleted()
                    }
                })
                mRequestClient?.registerRequest(deleteFeedsRequest)
                deleteFeedsRequest.exec()
            } else {
                Utils.showErrorMessage()
            }
        }
    }

    fun callSendLike(userId: Int, blockUnconfirmed: Boolean, mutualId: Int = SendLikeRequest.DEFAULT_NO_MUTUAL,
                     from: Int = SendLikeRequest.FROM_FEED) =
            callRate { SendLikeRequest(mContext, userId, mutualId, from, blockUnconfirmed) }


    fun callSendAdmiration(userId: Int, blockUnconfirmed: Boolean, mutualId: Int = SendLikeRequest.DEFAULT_NO_MUTUAL,
                           from: Int = SendLikeRequest.FROM_FEED) =
            callRate { SendAdmirationRequest(mContext, userId, mutualId, from, blockUnconfirmed) }

    private inline fun callRate(func: () -> ApiRequest): Observable<Rate> {
        val request = func()
        return Observable.create {
            request.callback(object : DataApiHandler<Rate>(Looper.getMainLooper()) {
                override fun success(rate: Rate, response: IApiResponse) = it.onNext(rate)
                override fun parseResponse(response: ApiResponse) = Rate.parse(response)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }
            })
            mRequestClient?.registerRequest(request)
            request.exec()
        }
    }

    fun callSkipRequest(id: Int): Observable<IApiResponse> = Observable.create { subscriber ->
        val skipRateRequest = SkipRateRequest(mContext)
        mRequestClient?.registerRequest(skipRateRequest)
        skipRateRequest.userid = id
        skipRateRequest.callback(object : SimpleApiHandler() {
            override fun success(response: IApiResponse?) {
                response?.let {
                    subscriber.onNext(it)
                }
            }

            override fun fail(codeError: Int, response: IApiResponse?) {
                super.fail(codeError, response)
                response?.let {
                    subscriber.onError(Exception(codeError.toString()))
                }
            }

            override fun always(response: IApiResponse?) {
                super.always(response)
                subscriber.onCompleted()
            }
        }).exec()
    }

    fun callMutualRead(idList: List<Int>): Observable<SimpleResponse> {
        return Observable.create {
            val request = MutualReadRequest(mContext, idList)
            request.callback(object : DataApiHandler<SimpleResponse>() {
                override fun success(data: SimpleResponse?, response: IApiResponse?) = it.onNext(data)
                override fun fail(codeError: Int, response: IApiResponse?) = it.onError(Exception(codeError.toString()))
                override fun always(response: IApiResponse?) {
                    super.always(response)
                    it.onCompleted()
                }

                override fun parseResponse(response: ApiResponse?) = response?.jsonResult?.toString()?.run {
                    JsonUtils.fromJson<SimpleResponse>(this, SimpleResponse::class.java)
                }
            })
            mRequestClient?.registerRequest(request)
            request.exec()
        }
    }

    fun callAdmirationRead(idList: List<Int>): Observable<SimpleResponse> {
        return Observable.create {
            val request = ReadAdmirationRequest(mContext, idList)
            request.callback(object : DataApiHandler<SimpleResponse>() {
                override fun success(data: SimpleResponse?, response: IApiResponse?) = it.onNext(data)
                override fun fail(codeError: Int, response: IApiResponse?) = it.onError(Exception(codeError.toString()))
                override fun always(response: IApiResponse?) {
                    super.always(response)
                    it.onCompleted()
                }

                override fun parseResponse(response: ApiResponse?) = response?.jsonResult?.toString()?.run {
                    JsonUtils.fromJson<SimpleResponse>(this, SimpleResponse::class.java)
                }
            })
            mRequestClient?.registerRequest(request)
            request.exec()
        }
    }

    fun callMutualBandGetList(limit: Int = 10, from: Int? = null, to: Int? = null): Observable<DialogContacts> {
        return Observable.create {
            val request = MutualBandGetListRequest(mContext, limit, from, to)
            request.callback(object : DataApiHandler<DialogContacts>() {
                override fun success(data: DialogContacts?, response: IApiResponse?) = it.onNext(data)
                override fun fail(codeError: Int, response: IApiResponse) = it.onError(Exception(codeError.toString()))
                override fun always(response: IApiResponse) {
                    super.always(response)
                    it.onCompleted()
                }

                override fun parseResponse(response: ApiResponse?) = response?.jsonResult?.toString()?.run {
                    JsonUtils.fromJson<DialogContacts>(this, DialogContacts::class.java)
                }
            })
            mRequestClient?.registerRequest(request)
            request.exec()
        }
    }

    private fun getFeedIntIds(list: List<FeedItem>): ArrayList<Int> {
        val ids = ArrayList<Int>()
        list.filter {
            !it.isLoaderOrRetrier
        }.forEach {
            ids.add(it.user.id)
        }
        return ids
    }

    private fun getFeedStringIds(list: List<Int>): ArrayList<String> {
        val ids = ArrayList<String>()
        list.forEach {
            ids.add(it.toString())
        }
        return ids
    }

}