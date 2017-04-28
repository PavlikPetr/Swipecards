package com.topface.topface.ui.fragments.buy

import android.databinding.ObservableField
import android.databinding.ObservableInt
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Products
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.utils.Utils
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * ВьюМодель попапа успешной покупки.
 */
class PurchaseSuccessfullViewModel(private val mType: String, private val mIDialogCloser: IDialogCloser) {


    private val mAppState by lazy {
        App.getAppComponent().appState()
    }

    private var mBalanceSubscription: Subscription? = null
    val popupImage = ObservableInt()
    val popupText = ObservableField<String>()

    init {
        if (mType == Products.ProductType.PREMIUM.getName()) {
            popupText.set(R.string.now_you_have_VIP.getString())
            popupImage.set(R.drawable.pic_vip)
        } else {
            mBalanceSubscription = mAppState.getObservable(BalanceData::class.java)
                    .subscribe(shortSubscription {
                        it?.let {
                            preparePopupText(it)
                        }
                    })
            popupImage.set(preparePopupImage())
        }
    }

    fun preparePopupText(balanceData: BalanceData) =
            popupText.set(
                    if (mType == Products.ProductType.COINS.getName()) {
                        Utils.getQuantityString(R.plurals.you_have_some_coins, balanceData.money, balanceData.money)
                    } else {
                        Utils.getQuantityString(R.plurals.you_have_some_sympathies, balanceData.likes, balanceData.likes)
                    })

    fun preparePopupImage() = if (mType == Products.ProductType.COINS.getName()) R.drawable.pic_coins else R.drawable.pic_coins  // todo вставить картинку для симпатий

    fun closeDialog() = mIDialogCloser.closeIt()

    fun release() = mBalanceSubscription.safeUnsubscribe()

}