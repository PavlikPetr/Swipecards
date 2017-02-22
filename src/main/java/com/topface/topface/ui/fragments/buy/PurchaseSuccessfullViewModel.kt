package com.topface.topface.ui.fragments.buy

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Products
import com.topface.topface.data.leftMenu.FragmentIdData
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.utils.rx.safeUnsubscribe
import rx.Subscription
import javax.inject.Inject

/**
 * ВьюМодель попапа успешной покупки.
 */
class PurchaseSuccessfullViewModel(private val mFeedNavigator: FeedNavigator, val goTo: Int, val sku: String, val iDialogCloser: IDialogCloser) {

    @Inject lateinit var state: TopfaceAppState
    val mBalanceSubscription: Subscription? = null


    val popupImage = ObservableInt()
    val popupText = ObservableField<String>()
    val buttonText = ObservableField<String>()

    init {
        App.get().inject(this)
        if (sku.equals(Products.ProductType.PREMIUM)) {
            popupText.set(R.string.now_you_have_VIP.getString())
            buttonText.set(R.string.start_dating.getString())
            popupImage.set(R.drawable.pic_vip)
        } else {
            state.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
                override fun onNext(balanceData: BalanceData?) {
                    balanceData?.let {
                        preparePopupText(balanceData)
                    }
                }

                override fun onError(e: Throwable?) = super.onError(e)
                override fun onCompleted() = super.onCompleted()
            })
            popupImage.set(preparePopupImage())
            buttonText.set(prepareButtonText())
        }
    }

    fun preparePopupText(balanceData: BalanceData) = popupText.set(if (sku.equals(Products.ProductType.COINS)) Utils.getQuantityString(R.plurals.you_have_some_coins, balanceData.money)
                                                                                    else Utils.getQuantityString(R.plurals.you_have_some_sympathies, balanceData.likes))

    fun prepareButtonText() =
            when (goTo) {
            // todo Тексты согласно goTo
                else -> Utils.EMPTY
            }

    fun preparePopupImage() = if (sku.equals(Products.ProductType.COINS)) R.drawable.pic_coins else R.drawable.pic_coins  // todo вставить картинку для симпатий

    fun onButtonClick() {
        when (goTo) {
            FragmentIdData.DATING -> mFeedNavigator.showDating()

        // todo переходы, согласно тому, что придет в конструкторе
        }
        iDialogCloser.closeIt()
    }

    fun unsubscribe() = mBalanceSubscription?.safeUnsubscribe()

}