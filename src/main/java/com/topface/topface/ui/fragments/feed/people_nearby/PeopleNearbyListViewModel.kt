package com.topface.topface.ui.fragments.feed.people_nearby

import android.Manifest
import android.databinding.ObservableField
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.data.FeedGeo
import com.topface.topface.data.FeedListData
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.geo.GeoLocationManager
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import permissions.dispatcher.NeedsPermission
import rx.Subscription
import rx.functions.Action1
import javax.inject.Inject

/**
 * ВьюМодель Листа "Людей рядом"
 */
class PeopleNearbyListViewModel(val api: FeedApi, private var mFeedGeoList: FeedListData<FeedGeo>? ) : ILifeCycle {

    @Inject lateinit var mState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus
    private var mSubscribtionLocation: Subscription
    private var mSubscriptionPeopleNearbyList: Subscription? = null
    private lateinit var mGeoLocationManager: GeoLocationManager
    private var mWaitLocationTimer: CountDownTimer? = null
    private val WAIT_LOCATION_DELAY = 10000
    val data = SingleObservableArrayList<Any>()
    var isProgressBarVisible: ObservableField<Int> = ObservableField<Int>(View.VISIBLE)

    init {
        App.get().inject(this)
        mSubscribtionLocation = mState.getObservable(Location::class.java).subscribe(object :Action1<Location>{
            override fun call(location: Location?) {
                location?.let { sendPeopleNearbyRequest(it) }
            }
        })
        geolocationManagerInit()
    }

    fun sendPeopleNearbyRequest(location: Location) {
        api.callNewGeo(location.latitude,location.longitude).subscribe(object: RxUtils.ShortSubscription<FeedListData<FeedGeo>>(){
            override fun onNext(data: FeedListData<FeedGeo>?) {
                data?.let {
                    with(this@PeopleNearbyListViewModel.data.observableList) {
                        clear()
                        addAll(it.items)
                    }
                }
            }

            override fun onError(e: Throwable?) {
                // todo обработка ошибки при запросе списка
            }
        })
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun geolocationManagerInit() {
        mGeoLocationManager = GeoLocationManager().apply{
            registerProvidersChangedActionReceiver()
        }
        startWaitLocationTimer()
    }

    private fun startWaitLocationTimer() {
        stopWaitLocationTimer()
        mWaitLocationTimer = object : CountDownTimer(WAIT_LOCATION_DELAY.toLong(), WAIT_LOCATION_DELAY.toLong()) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                stopWaitLocationTimer()
            }
        }.start()
    }

    private fun stopWaitLocationTimer() {
            mWaitLocationTimer?.cancel()
            mWaitLocationTimer = null
    }

    fun release(){
        mSubscribtionLocation.safeUnsubscribe()
        mSubscriptionPeopleNearbyList.safeUnsubscribe()
    }

}

