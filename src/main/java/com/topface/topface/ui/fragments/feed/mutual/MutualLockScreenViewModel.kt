package com.topface.topface.ui.fragments.feed.mutual

import com.topface.topface.App
import com.topface.topface.data.CountersData
import com.topface.topface.databinding.LayoutEmptyMutualBinding
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription

/**
 * VM локскрина
 * Created by tiberal on 22.08.16.
 */
class MutualLockScreenViewModel(binding: LayoutEmptyMutualBinding,
                                private val mNavigator: IFeedNavigator, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<LayoutEmptyMutualBinding>(binding) {

    private var mBalanceSubscription: Subscription? = null

    init {
        mBalanceSubscription = App.getAppComponent().appState()
                .getObservable(CountersData::class.java)
                .subscribe(shortSubscription {
                    if (it?.let { it.admirations > 0 } ?: false) {
                        mIFeedUnlocked.onFeedUnlocked()
                    }
                })
    }

    fun onGoToDatingClick() = mNavigator.showDating()

    fun onGoToPurchaseCoins() = mNavigator.showPurchaseCoins("EmptyMutual")

    override fun release() {
        super.release()
        mBalanceSubscription.safeUnsubscribe()
    }
}