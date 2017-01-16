package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.databinding.Observable.OnPropertyChangedCallback
import android.databinding.ObservableBoolean
import android.support.v4.widget.SwipeRefreshLayout
import com.topface.statistics.generated.PeopleNearbyStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.state.EventBus
import com.topface.topface.statistics.PeopleNearbyStatistics
import com.topface.topface.ui.fragments.feed.dialogs.IFeedPushHandlerListener
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.SingleObservableArrayList
import com.topface.topface.utils.extensions.PermissionsExtensions
import com.topface.topface.utils.extensions.PermissionsExtensions.PermissionState
import com.topface.topface.utils.rx.applySchedulers
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * VM для нового дизайна "Люди рядом"
 * Created by tiberal on 30.11.16.
 */
class PeopleNearbyFragmentViewModel(private val mPopoverControl:IPopoverControl) : SwipeRefreshLayout.OnRefreshListener, IFeedPushHandlerListener {
    var isRefreshing = ObservableBoolean()
    var isEnable = ObservableBoolean(false)
    val data = SingleObservableArrayList<Any>()
    private var mSubscriptions = CompositeSubscription()

    @Inject lateinit var mEventBus: EventBus

    init {
        App.get().inject(this)
        // отслеживаем изменения ptr
        isRefreshing.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: android.databinding.Observable?, p1: Int) {
                // отправляем актуальный статус ptr подписчикам
                (obs as? ObservableBoolean)?.let { mEventBus.setData(PeopleNearbyRefreshStatus(it.get())) }
            }
        })
        mSubscriptions.add(mEventBus.getObservable(PhotoBlogLoaded::class.java)
                .applySchedulers()
                .subscribe(shortSubscription {
                    removeLoader()
                    isRefreshing.set(false)
                }))
    }

    private fun removeLoader() {
        if (data.observableList.remove(PeopleNearbyLoader())) {
            isEnable.set(true)
            mPopoverControl.show()
        }
    }

    override fun onRefresh() {
        isRefreshing.set(true)
    }

    fun geoPermissionsGranted() {
        PeopleNearbyStatisticsGeneratedStatistics
                .sendNow_PEOPLE_NEARBY_OPEN(Utils.getUniqueKeyStatistic(PeopleNearbyStatistics.PEOPLE_NEARBY_OPEN))
        PeopleNearbyStatisticsGeneratedStatistics.sendNow_PEOPLE_NOT_UNIQUE_NEARBY_OPEN()
        with(data.observableList) {
            if (find { it is PhotoBlogList } == null) {
                clear()
                addAll(listOf(PhotoBlogList(), PeopleNearbyLoader()))
            }
        }
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    fun askGeoPermissions(@PermissionState state: Long) = with(data.observableList) {
        PeopleNearbyStatisticsGeneratedStatistics
                .sendNow_PEOPLE_NEARBY_PERMISSION_OPEN(Utils.getUniqueKeyStatistic(PeopleNearbyStatistics.PEOPLE_NEARBY_PERMISSION_OPEN))
        when (state) {
            PermissionsExtensions.PERMISSION_DENIED -> PeopleNearbyPermissionDenied()
            PermissionsExtensions.PERMISSION_NEVER_ASK_AGAIN -> PeopleNearbyPermissionNeverAskAgain()
            else -> null
        }?.let {
            clear()
            add(it)
        }
    }

    fun release() {
        mSubscriptions.safeUnsubscribe()
    }
}