package com.topface.topface.ui.fragments.dating.admiration_purchase_popup

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.ObservableField
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.AdmirationPurchasePopupBinding
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.CountersManager
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.subscriptions.CompositeSubscription

/**
 * VM для попапа покупки випа или монет
 * Created by siberia87 on 01.11.16.
 */
class AdmirationPurchasePopupViewModel(binding: AdmirationPurchasePopupBinding,
                                       private val mAdmirationPurchasePopupHide: IAdmirationPurchasePopupHide,
                                       private val mNavigator: FeedNavigator,
                                       currentUser: FeedUser?) :
        BaseViewModel<AdmirationPurchasePopupBinding>(binding) {

    companion object {
        const val TRANSITION_NAME = "admiration_purchase_popup"
        const val RESULT_USER_BUY_VIP = 5
    }

    val buyCoinsButtonText = String.format(context.resources.getString(R.string.buy_vip_button_text_admiration_purchase_popup),
            App.get().options.priceAdmiration)

    val userAvatar = ObservableField(currentUser?.photo)

    private val mAppState by lazy {
        App.getAppComponent().appState()
    }
    private val mBalanceDataSubscriptions = CompositeSubscription()
    private var mBalance: BalanceData? = null

    private val mVipBoughtBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(CountersManager.VIP_STATUS_EXTRA, false)) {
                mAdmirationPurchasePopupHide.hideAdmirationPurchasePopup(AdmirationPurchasePopupViewModel.RESULT_USER_BUY_VIP)
            }
        }
    }

    init {
        mBalanceDataSubscriptions
                .add(mAppState.getObservable(BalanceData::class.java)
                        .subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
                            override fun onNext(balance: BalanceData?) = balance.let {
                                mBalance = it
                            }
                        }))

        LocalBroadcastManager.getInstance(context).registerReceiver(mVipBoughtBroadcastReceiver, IntentFilter(CountersManager.UPDATE_VIP_STATUS))

        if (Utils.isLollipop()) {
            binding.container.transitionName = TRANSITION_NAME
        }
    }

    fun skip() = mAdmirationPurchasePopupHide.hideAdmirationPurchasePopup(Activity.RESULT_CANCELED)

    fun buyVip() = mNavigator.showPurchaseVip("Admirations")

    fun buyCoins() = mBalance?.let {
        if (it.money >= App.get().options.priceAdmiration) {
            mAdmirationPurchasePopupHide.hideAdmirationPurchasePopup(Activity.RESULT_OK)
        } else {
            mNavigator.showPurchaseCoins("EmptyAdmirations")
        }
    }

    override fun release() {
        super.release()
        mBalanceDataSubscriptions.unsubscribe()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mVipBoughtBroadcastReceiver)
    }
}