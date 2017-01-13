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
class PeopleNearbyListViewModel(val mApi: FeedApi, private var mFeedGeoList: FeedListData<FeedGeo>? ) : ILifeCycle {

    companion object {
        val LAT = "lat"
        val LON = "lon"
    }

    @Inject lateinit var mState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus
    private var mSubscriptionPeopleNearbyEvent: Subscription
    private var mSubscribtionLocation: Subscription
    private var mSubscriptionPeopleNearbyList: Subscription? = null
    private var mTimeForFun = false
    private lateinit var mGeoLocationManager: GeoLocationManager
    private var mWaitLocationTimer: CountDownTimer? = null
    private val WAIT_LOCATION_DELAY = 10000
    val data = SingleObservableArrayList<Any>()
    private var isForPremiun = true
    private val mLocationAction = Action1<Location> { location ->
            sendPeopleNearbyRequest(location)

    }

    var isProgressBarVisible: ObservableField<Int> = ObservableField<Int>(View.VISIBLE)

    init {
            Debug.error("------------Конструктор МоделВью Листа Людей рядом-------------")
        App.get().inject(this)
        mSubscribtionLocation = mState.getObservable(Location::class.java).subscribe(mLocationAction)
        mSubscriptionPeopleNearbyEvent = mEventBus.getObservable(PeopleNearbyEvent::class.java).subscribe(object : RxUtils.ShortSubscription<PeopleNearbyEvent>() {
            override fun onError(e: Throwable?) {
                super.onError(e)
            }

            override fun onNext(event: PeopleNearbyEvent) {
                mTimeForFun = event.isPossibleDownload
            }

            override fun onCompleted() {
                super.onCompleted()
                safeUnsubscribe()
            }
        })

        geolocationManagerInit()

    }


    fun sendPeopleNearbyRequest(location: Location) {
        Debug.error("------------Запрос на получение людей-------------")

        mApi.callNewGeo(location.latitude,location.longitude).subscribe(object: RxUtils.ShortSubscription<FeedListData<FeedGeo>>(){
            override fun onNext(data: FeedListData<FeedGeo>?) {
                data?.let {
                    Debug.error("------------Запрос на людей-----------Получен размер коллекции: " + data.items.size+ " кто-то - " + data.items.first.user.nameAndAge)
                    with(this@PeopleNearbyListViewModel.data.observableList) {
                        clear()
                        addAll(it.items)
                    }
                }
            }

            override fun onError(e: Throwable?) {
                Debug.error("------------Запрос на людей------------ОШИБКА" + e?.message)
            }
        })
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    fun geolocationManagerInit() {
        mGeoLocationManager = GeoLocationManager()
        mGeoLocationManager.registerProvidersChangedActionReceiver()
        startWaitLocationTimer()
    }

    private fun startWaitLocationTimer() {
        stopWaitLocationTimer()
        mWaitLocationTimer = object : CountDownTimer(WAIT_LOCATION_DELAY.toLong(), WAIT_LOCATION_DELAY.toLong()) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
//                updateListWithOldGeo()
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
        mSubscriptionPeopleNearbyEvent.safeUnsubscribe()
        mSubscriptionPeopleNearbyList.safeUnsubscribe()
    }

}

