package com.topface.topface.ui.fragments.dating.form

import android.databinding.ObservableField
import com.topface.framework.utils.Debug
import com.topface.topface.App
import com.topface.topface.data.Profile
import com.topface.topface.state.EventBus
import com.topface.topface.ui.fragments.dating.FakeGift
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscriber
import rx.Subscription

/**
 * VM для итема подарка
 * Created by tiberal on 07.11.16.
 */
class GiftsItemViewModel(private val mApi: FeedApi, private val mNavigator: IFeedNavigator, gifts: Profile.Gifts,
                         private val userId: Int) {

    val data = MultiObservableArrayList<Any>()
    val amount = ObservableField<String>(if (gifts.more) gifts.count.toString() else Utils.EMPTY)

    var gifts: Profile.Gifts = gifts
        set(value) {
            amount.set(value.count.toString())
            field = value
        }
    private var mLoadGiftsSubscription: Subscription? = null
    private val mEventBus: EventBus by lazy {
        App.getAppComponent().eventBus()
    }

    fun loadFakeGift() {
        this@GiftsItemViewModel.data.addAll(listOf(FakeGift()))
    }

    fun loadGifts(lastGiftId: Int) {
        //если подарочки есть, а приходит дефолтный id(-1 от нас или 0 если только что отправили
        // подарочек и он не добавлен еще в фиды) для выборки, значит что то не так и
        // мы пытаемся згрузить дубликаты, НЕНАДО ТАК
        if (gifts.items.isNotEmpty() && (lastGiftId == 0 || lastGiftId == -1)) return
        mLoadGiftsSubscription = mApi.callGetGifts(userId, lastGiftId)
                .subscribe(object : Subscriber<Profile.Gifts>() {
                    override fun onNext(data: Profile.Gifts?) {
                        Debug.log("GIFTS_BUGS loadGifts onNext ${data?.items?.count()}")
                        data?.let {
                            this@GiftsItemViewModel.data.addAll(it.items)
                            mEventBus.setData(GiftId(it.items.last().id))
                            // говно со счетчиками и веб подарками на сервере
                            // ответ на этот запрос приносит обновленные поля more и count
                            // изначально может прийти more == true и count == 1, но это будет от веб-подарка
                            gifts.more = it.more
                            gifts.count = it.count
                        }
                    }

                    override fun onError(e: Throwable?) {
                        Debug.log("GIFTS_BUGS loadGifts onError")
                        Utils.showErrorMessage()
                    }

                    override fun onCompleted() = unsubscribe()

                })
    }

    fun sendGift() = mNavigator.showGiftsActivity(userId, "dating")

    fun release() = mLoadGiftsSubscription.safeUnsubscribe()

}