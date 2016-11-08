package com.topface.topface.ui.fragments.feed.dating.admiration_purchase_popup

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
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.CountersManager
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.Utils
import com.topface.topface.viewModels.BaseViewModel
import rx.lang.kotlin.observable
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * VM для попапа покупки випа или минет
 * Created by siberia87 on 01.11.16.
 */
class AdmirationPurchasePopupViewModel(binding: AdmirationPurchasePopupBinding,
                                       private val mAdmirationPurchasePopupHide: IAdmirationPurchasePopupHide,
                                       private val mNavigator: FeedNavigator,
                                       currentUser: FeedUser?) :
        BaseViewModel<AdmirationPurchasePopupBinding>(binding) {

    companion object {
        const val TRANSITION_NAME = "admiration_purchase_popup"
    }

    val iconUrl = ObservableField(currentUser?.photo)

    @Inject lateinit internal var mAppState: TopfaceAppState
    private val mBalanceDataSubscriptions = CompositeSubscription()
    private var mBalance: BalanceData? = null

    private val mVipBoughtBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getBooleanExtra(CountersManager.VIP_STATUS_EXTRA, false)) {
                mAdmirationPurchasePopupHide.hideAdmirationPurchasePopup(AdmirationPurchasePopupActivity.ADMIRATION_RESULT)
            }
        }
    }

    init {
        App.get().inject(this)
        mBalanceDataSubscriptions.add(mAppState.getObservable(BalanceData::class.java).subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
            override fun onNext(balance: BalanceData?) = balance.let {
                mBalance = it
            }
        }))

        LocalBroadcastManager.getInstance(context).registerReceiver(mVipBoughtBroadcastReceiver, IntentFilter(CountersManager.UPDATE_VIP_STATUS))
        binding.coinsButton.text = String.format(context.resources.getString(R.string.buy_vip_button_text_admiration_purchase_popup),
                App.get().options.priceAdmiration)

        if (Utils.isLollipop()) {
            binding.container.transitionName = TRANSITION_NAME
        }
    }

    fun skip() = mAdmirationPurchasePopupHide.hideAdmirationPurchasePopup(Activity.RESULT_OK)

    fun buyVip() = mNavigator.showPurchaseVip()

    fun buyCoins() = mBalance?.let {
        if (it.money >= App.get().options.priceAdmiration) {
            mAdmirationPurchasePopupHide.hideAdmirationPurchasePopup(AdmirationPurchasePopupActivity.ADMIRATION_RESULT)
        } else {
            mNavigator.showPurchaseCoins()
        }
    }

    override fun release() {
        super.release()
        mBalanceDataSubscriptions.unsubscribe()
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mVipBoughtBroadcastReceiver)
    }
}