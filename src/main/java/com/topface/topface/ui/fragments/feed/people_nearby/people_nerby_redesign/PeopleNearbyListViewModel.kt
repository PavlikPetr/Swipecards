package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.location.Location
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.extensions.isValidLocation
import com.topface.topface.utils.extensions.safeToInt
import com.topface.topface.utils.extensions.showShortToast
import com.topface.topface.utils.geo.GeoLocationManager
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * ВьюМодель Листа "Людей рядом"
 */
class PeopleNearbyListViewModel(val api: FeedApi) : ILifeCycle {

    @Inject lateinit var mState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus
    private var mSubscribtionLocation: Subscription
    private var mSubscriptionPeopleNearbyList: Subscription? = null
    private var mSubscriptionPTR: Subscription
    private var mIntervalSubscription: Subscription? = null
    private var mLastLocation: Location? = null
    private lateinit var mGeoLocationManager: GeoLocationManager

    val data: MultiObservableArrayList<Any> by lazy {
        MultiObservableArrayList<Any>()
    }

    companion object {
        const val WAIT_LOCATION_DELAY = 20L
    }

    init {
        App.get().inject(this)
        geolocationManagerInit()
        mSubscribtionLocation = mState.getObservable(Location::class.java)
                .filter { it.isValidLocation() }
                .subscribe(shortSubscription {
                    it?.let {
                        mLastLocation = it
                        loadList()
                    }
                })

        mSubscriptionPTR = mEventBus.getObservable(PeopleNearbyRefreshStatus::class.java)
                .subscribe(shortSubscription {
                    if (it?.isRefreshing ?: false) {
                        loadList(true)
                    }
                })
        if (!isGeoEnabled()) {
            emptyLocation()
            mEventBus.setData(PeopleNearbyLoaded(true, false))
        }
    }

    private fun sendPeopleNearbyRequest(isPullToRefresh: Boolean = false) {
        mLastLocation?.let {
            api.callNewGeo(it.latitude, it.longitude)
                    .subscribe({
                        mEventBus.setData(PeopleNearbyLoaded(it?.items?.isEmpty() ?: true, isPullToRefresh))
                        it?.let {
                            this@PeopleNearbyListViewModel.data.replaceData(if (it.items.size > 0) {
                                arrayListOf<Any>().apply { addAll(it.items) }
                            } else {
                                arrayListOf<Any>(PeopleNearbyEmptyList())
                            })
                        }
                    }, {
                        mEventBus.setData(PeopleNearbyLoaded(handleError(it?.message), isPullToRefresh))
                    })
        }
    }

    fun geolocationManagerInit() {
        mGeoLocationManager = GeoLocationManager().apply {
            registerProvidersChangedActionReceiver()
        }
        mIntervalSubscription = Observable.interval(WAIT_LOCATION_DELAY, TimeUnit.SECONDS)
                .first()
                .applySchedulers()
                .subscribe(shortSubscription {
                    emptyLocation()
                })
    }

    private fun showStub(errorCode: Int) =
            when (errorCode) {
                ErrorCodes.CANNOT_GET_GEO -> {
                    emptyLocation()
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

    private fun loadList(isPullToRefresh: Boolean = false) {
        mIntervalSubscription.safeUnsubscribe()
        if (isGeoEnabled()) {
            sendPeopleNearbyRequest(isPullToRefresh)
        } else {
            emptyLocation()
            mEventBus.setData(PeopleNearbyLoaded(true, isPullToRefresh))
        }
    }

    private fun emptyLocation() = data.replaceData(arrayListOf<Any>(PeopleNearbyEmptyLocation()))

    private fun isGeoEnabled() = mGeoLocationManager.enabledProvider != GeoLocationManager.NavigationType.DISABLE

    private fun handleError(errorCode: String?) =
            showStub(errorCode.safeToInt(ErrorCodes.INTERNAL_SERVER_ERROR))

    fun release() {
        mIntervalSubscription.safeUnsubscribe()
        mSubscribtionLocation.safeUnsubscribe()
        mSubscriptionPeopleNearbyList.safeUnsubscribe()
    }
}