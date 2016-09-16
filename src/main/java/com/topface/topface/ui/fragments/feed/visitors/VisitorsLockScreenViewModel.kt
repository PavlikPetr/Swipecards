package com.topface.topface.ui.fragments.feed.visitors

import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

/**
 * Моделька локскрина гостей
 * Created by tiberal on 09.09.16.
 */
class VisitorsLockScreenViewModel(binding: LayoutEmptyVisitorsBinding, private val mNavigator: IFeedNavigator, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<LayoutEmptyVisitorsBinding>(binding) {

    @Inject lateinit var mState: TopfaceAppState
    private var mBalanceSubscription: Subscription? = null

    init {
        App.get().inject(this)
        mBalanceSubscription = mState.getObservable(BalanceData::class.java).subscribe {
            if (it.premium) {
                mIFeedUnlocked.onFeedUnlocked()
            }
        }
    }

    fun showPurchaseVip() = mNavigator.showPurchaseVip()

}