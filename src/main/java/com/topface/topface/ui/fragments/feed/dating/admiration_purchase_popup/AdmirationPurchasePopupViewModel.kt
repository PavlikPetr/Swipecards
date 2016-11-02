package com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableField
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.AdmirationPurchasePopupBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.CountersManager
import com.topface.topface.utils.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * VM для попапа покупки випа или минет
 * Created by siberia87 on 01.11.16.
 */
class AdmirationPurchasePopupViewModel(binding: AdmirationPurchasePopupBinding,
                                       private val mAdmirationPurchasePopupVisible: IAdmirationPurchasePopupVisible,
                                       private val mStartPurchaseScreenDelegate: IStartPurchaseScreenDelegate,
                                       currentUser: FeedUser?) :
        BaseViewModel<AdmirationPurchasePopupBinding>(binding) {

    companion object {
        const val CURRENT_COINS_COUNT = 3
    }

    val iconUrl = ObservableField(currentUser?.photo)

    @Inject lateinit internal var mAppState: TopfaceAppState
    private val mBalanceDataSubscriptions = CompositeSubscription()
    lateinit private var mBalance: BalanceData

    private val mVipBoughtBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(CountersManager.VIP_STATUS_EXTRA, false)) {
                mAdmirationPurchasePopupVisible.hideAdmirationPurchasePopup(AdmirationPurchasePopupActivity.RESULT_CODE_BOUGHT_VIP)
            }
        }
    }

    init {
        App.get().inject(this)
        mBalanceDataSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
            override fun onNext(balance: BalanceData?) = balance?.let {
                mBalance = it
            } ?: Unit
        }))

        LocalBroadcastManager.getInstance(context).registerReceiver(mVipBoughtBroadcastReceiver, IntentFilter(CountersManager.UPDATE_VIP_STATUS))
    }

    fun skip() = mAdmirationPurchasePopupVisible.hideAdmirationPurchasePopup(Activity.RESULT_CANCELED)

    fun buyVip() = mStartPurchaseScreenDelegate.startVIPScreenPurchase()

    fun buyCoins() =
            if (mBalance.money >= CURRENT_COINS_COUNT) {
                mAdmirationPurchasePopupVisible.hideAdmirationPurchasePopup(AdmirationPurchasePopupActivity.RESULT_CODE_BOUGHT_COINS)
            } else {
                mStartPurchaseScreenDelegate.startCoinScreenPurchase()
            }

    override fun release() {
        super.release()
        mBalanceDataSubscriptions.unsubscribe()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mVipBoughtBroadcastReceiver)
    }
}