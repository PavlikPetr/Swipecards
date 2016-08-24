package com.topface.topface.ui.fragments.feed.mutual

import com.topface.topface.App
import com.topface.topface.data.CountersData
import com.topface.topface.databinding.LayoutEmptyMutualBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

/**
 * VM локскрина
 * Created by tiberal on 22.08.16.
 */
class MutualLockScreenViewModel(binding: LayoutEmptyMutualBinding,
                                private val mNavigator: IFeedNavigator, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<LayoutEmptyMutualBinding>(binding) {

    @Inject lateinit var mState: TopfaceAppState
    private var mBalanceSubscription: Subscription? = null

    init {
        App.get().inject(this)
        mBalanceSubscription = mState.getObservable(CountersData::class.java).subscribe {
            if (it.admirations > 0) {
                mIFeedUnlocked.onFeedUnlocked()
            }
        }
    }

    fun onGoToDatingClick() = mNavigator.showDating()

    fun onGoToPurchaseCoins() = mNavigator.showPurchaseCoins()

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mBalanceSubscription)
    }
}