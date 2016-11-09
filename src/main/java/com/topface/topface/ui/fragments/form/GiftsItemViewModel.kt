package com.topface.topface.ui.fragments.form

import android.databinding.ObservableField
import android.widget.Toast
import com.topface.topface.data.Profile
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.safeUnsubscribe
import rx.Subscriber
import rx.Subscription

/**
 * VM для итема подарка
 * Created by tiberal on 07.11.16.
 */
class GiftsItemViewModel(private val mApi: FeedApi, val gifts: Profile.Gifts,
                         private val userId: Int, val update: (Profile.Gifts) -> Unit) {

    val amount = ObservableField<String>(if (gifts.items.isNotEmpty()) gifts.count.toString() else Utils.EMPTY)
    private var mLoadGiftsSubscription: Subscription? = null

    fun loadGifts(loadedCount: Int, lastGiftId: Int) {
        if (loadedCount < gifts.count) {
            mLoadGiftsSubscription = mApi.callGetGifts(userId, lastGiftId).subscribe(object : Subscriber<Profile.Gifts>() {

                override fun onNext(data: Profile.Gifts?) {
                    data?.let {
                        update(it)
                    }
                }

                override fun onError(e: Throwable?) {
                    Utils.showErrorMessage()
                }

                override fun onCompleted() = unsubscribe()

            })
        }
    }

    fun sendGift() {
        Utils.showToastNotification("Скоро", Toast.LENGTH_SHORT)
    }

    fun release() = mLoadGiftsSubscription.safeUnsubscribe()

}