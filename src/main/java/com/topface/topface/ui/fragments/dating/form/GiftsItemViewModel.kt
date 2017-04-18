package com.topface.topface.ui.fragments.dating.form

import android.app.Activity
import android.content.Intent
import android.databinding.ObservableField
import com.topface.framework.JsonUtils
import com.topface.framework.utils.Debug
import com.topface.topface.data.Gift
import com.topface.topface.data.Profile
import com.topface.topface.data.SendGiftAnswer
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.GiftsActivity
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.databinding.MultiObservableArrayList
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscriber
import rx.Subscription
import java.util.*

/**
 * VM для итема подарка
 * Created by tiberal on 07.11.16.
 */
class GiftsItemViewModel(private val mApi: FeedApi, private val mNavigator: IFeedNavigator, gifts: Profile.Gifts,
                         private val userId: Int, val update: (Int?, Profile.Gifts) -> Unit) : ILifeCycle {

    val data = MultiObservableArrayList<Any>()
    val amount = ObservableField<String>(if (gifts.more) gifts.count.toString() else Utils.EMPTY)

    var gifts: Profile.Gifts = gifts
        set(value) {
            amount.set(value.count.toString())
            field = value
        }
    private var mLoadGiftsSubscription: Subscription? = null

    fun loadFakeGift() =
            this@GiftsItemViewModel.data.replaceData(arrayListOf(FakeGift()))

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
                            update(null, it)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                val gifts = ArrayList<Gift>().apply {
                    val answer = data?.getParcelableExtra<SendGiftAnswer>(GiftsActivity.INTENT_SEND_GIFT_ANSWER)
                    add(JsonUtils.fromJson(answer?.history?.mJsonForParse, Gift::class.java))
                }
                handleGifts(gifts)
            }
            if (requestCode == ChatActivity.REQUEST_CHAT) {
                data?.getParcelableArrayListExtra<Gift>(ChatActivity.DISPATCHED_GIFTS)?.let {
                    handleGifts(it)
                }
            }
        }
    }

    private fun handleGifts(newGifts: ArrayList<Gift>) {
        if (newGifts.isEmpty()) return
        // если подарков нет, то выпиливаем фэйк
        if (data.getList().filter { it !is FakeGift }.isEmpty()) {
            data.clear()
        }
        data.addAll(0, arrayListOf<Any>().apply { addAll(newGifts) })
        update(0, Profile.Gifts().apply {
            count = newGifts.count()
            more = true
            gifts.addAll(newGifts)
        })
    }
}