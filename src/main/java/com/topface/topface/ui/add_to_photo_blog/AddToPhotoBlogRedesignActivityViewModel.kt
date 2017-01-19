package com.topface.topface.ui.add_to_photo_blog

import android.app.Activity
import android.content.Intent
import android.databinding.ObservableBoolean
import android.widget.Toast
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.requests.AddPhotoFeedRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * VM for AddToPhotoBlogRedesignActivity
 * Created by m.bayutin on 19.01.17.
 */
class AddToPhotoBlogRedesignActivityViewModel(var activityDelegate: IActivityDelegate?, val feedNavigator: FeedNavigator) {
    val isLockerVisible = ObservableBoolean(false)
    @Inject lateinit var mAppState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus
    private var mBalance: BalanceData? = null
    private val mSubscriptions = CompositeSubscription()
    // договаривались использовать цену из первой кнопки лидеров
    val price by lazy { App.get().options.buyLeaderButtons[0].price }

    private var mLastSelectedPhotoId = 0
    val lastSelectedPhotoId: Int
        get() = mLastSelectedPhotoId

    init {
        App.get().inject(this)
        mSubscriptions.add(mEventBus.getObservable(PhotoSelectedEvent::class.java)
                .subscribe { mLastSelectedPhotoId = it.id })
        mSubscriptions.add(mEventBus.getObservable(PlaceButtonTapEvent::class.java)
                .subscribe { placeOrBuy() })
        mSubscriptions.add(mAppState.getObservable(BalanceData::class.java)
                .subscribe({ mBalance = it }))

    }

    fun release() {
        mSubscriptions.safeUnsubscribe()
        activityDelegate = null
    }

    private fun placeOrBuy() = mBalance?.let {
        if (it.money < price) {
            startPurchasesActivity()
        } else {
            isLockerVisible.set(true)
            AddPhotoFeedRequest(mLastSelectedPhotoId, App.get(), 1, ""
                    , price.toLong()).callback(object : ApiHandler() {

                override fun success(response: IApiResponse) {
                    FlurryManager.getInstance().sendSpendCoinsEvent(price, FlurryManager.GET_LEAD)
                    activityDelegate?.let {
                        it.setResult(Activity.RESULT_OK, Intent())
                        it.finish()
                    }
                }

                override fun fail(codeError: Int, response: IApiResponse) {
                    isLockerVisible.set(false)
                    if (codeError == ErrorCodes.PAYMENT) {
                        startPurchasesActivity()
                    } else {
                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }).exec()
        }
    }

    private fun startPurchasesActivity() = feedNavigator.showPurchaseCoins()
}