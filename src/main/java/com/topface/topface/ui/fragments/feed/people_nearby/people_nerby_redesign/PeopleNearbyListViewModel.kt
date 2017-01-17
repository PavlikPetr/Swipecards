package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.location.Location
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedGeo
import com.topface.topface.data.FeedListData
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.showShortToast
import com.topface.topface.utils.geo.GeoLocationManager
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import rx.functions.Action1
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ВьюМодель Листа "Людей рядом"
 */
class PeopleNearbyListViewModel(val api: FeedApi, private var mFeedGeoList: FeedListData<FeedGeo>?) : ILifeCycle {

    @Inject lateinit var mState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus
    private var mSubscribtionLocation: Subscription
    private var mSubscriptionPeopleNearbyList: Subscription? = null
    private var mSubscriptionPTR: Subscription
    private var mIntervalSubscription: Subscription? = null
    private var mLastLocation: Location? = null
    private lateinit var mGeoLocationManager: GeoLocationManager
    val data = MultiObservableArrayList<Any>()

    companion object {
        const val WAIT_LOCATION_DELAY = 10L
    }

    init {
        App.get().inject(this)
        mSubscribtionLocation = mState.getObservable(Location::class.java).subscribe(object : Action1<Location> {
            override fun call(location: Location?) {
                location?.let {
                    mIntervalSubscription.safeUnsubscribe()
                    mLastLocation = location
                    sendPeopleNearbyRequest()
                }
            }
        })
        mSubscriptionPTR = mEventBus.getObservable(PeopleNearbyRefreshStatus::class.java)
                .subscribe(shortSubscription {
                    if (it?.isRefreshing ?: false) {
                        sendPeopleNearbyRequest(true)
                    }
                })
        geolocationManagerInit()
    }

    fun sendPeopleNearbyRequest(isPullToRefresh: Boolean = false) {
        mLastLocation?.let {
            api.callNewGeo(it.latitude, it.longitude).subscribe(object : RxUtils.ShortSubscription<FeedListData<FeedGeo>>() {
                override fun onNext(data: FeedListData<FeedGeo>?) {
                    mEventBus.setData(PeopleNearbyLoaded(data?.items?.isEmpty() ?: true, isPullToRefresh))
                    data?.let {
                        this@PeopleNearbyListViewModel.data.replaceData(if (it.items.size > 0) {
                            arrayListOf<Any>().apply { addAll(it.items) }
                        } else {
                            arrayListOf<Any>(PeopleNearbyEmptyList())
                        })
                    }
                }

                override fun onError(e: Throwable?) {
                    mEventBus.setData(PeopleNearbyLoaded(handleError(e?.message), isPullToRefresh))
                }
            })
        }
    }

    fun geolocationManagerInit() {
        mGeoLocationManager = GeoLocationManager().apply {
            registerProvidersChangedActionReceiver()
        }
        mIntervalSubscription = Observable.interval(WAIT_LOCATION_DELAY, TimeUnit.SECONDS)
                .subscribe(shortSubscription {
                    data.replaceData(arrayListOf<Any>(PeopleNearbyEmptyList()))
                })
    }

    private fun showStub(errorCode: Int) =
            when (errorCode) {
                ErrorCodes.CANNOT_GET_GEO -> {
                    data.replaceData(arrayListOf<Any>(PeopleNearbyEmptyLocation()))
                    true
                }
                ErrorCodes.PREMIUM_ACCESS_ONLY, ErrorCodes.BLOCKED_PEOPLE_NEARBY -> {
                    data.replaceData(arrayListOf<Any>(PeopleNearbyLocked()))
                    true
                }
                else -> {
                    R.string.general_data_error.showShortToast()
                    false
                }
            }


    private fun handleError(errorCode: String?) =
            showStub(getErrorCode(errorCode))

    private fun getErrorCode(error: String?) =
            error?.let {
                try {
                    it.toInt()
                } catch (e: NumberFormatException) {
                    ErrorCodes.INTERNAL_SERVER_ERROR
                }
            } ?: ErrorCodes.INTERNAL_SERVER_ERROR


    fun release() {
        mIntervalSubscription.safeUnsubscribe()
        mSubscribtionLocation.safeUnsubscribe()
        mSubscriptionPeopleNearbyList.safeUnsubscribe()
    }

}