package com.topface.topface.ui.fragments.dating.form

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.Subscriber
import rx.Subscription

/**
 * VM для итема подарка
 * Created by tiberal on 07.11.16.
 */
class GiftsItemViewModel(private val mApi: FeedApi, private val mNavigator: IFeedNavigator, gifts: Profile.Gifts,
                         private val userId: Int, val update: (Profile.Gifts) -> Unit) {

    val amount = ObservableField<String>(if (gifts.items.isNotEmpty()) gifts.count.toString() else Utils.EMPTY)
    var gifts: Profile.Gifts = gifts
        set(value) {
            amount.set(value.count.toString())
            field = value
        }
    private var mLoadGiftsSubscription: Subscription? = null

    fun loadGifts(loadedCount: Int, lastGiftId: Int) {
        if (loadedCount < gifts.count) {
            mLoadGiftsSubscription = mApi.callGetGifts(userId, lastGiftId).subscribe(object : Subscriber<Profile.Gifts>() {
                override fun onNext(data: Profile.Gifts?) {
                    Debug.log("GIFTS_BUGS loadGifts onNext ${data?.items?.count()}")
                    data?.let {
                        update(it)
                    }
                }

                override fun onError(e: Throwable?) {
                    Debug.log("GIFTS_BUGS loadGifts onError")
                    Utils.showErrorMessage()
                }

                override fun onCompleted() = unsubscribe()

            })
        }
    }

    fun sendGift() = mNavigator.showGiftsActivity(userId)

    fun release() = mLoadGiftsSubscription.safeUnsubscribe()

}