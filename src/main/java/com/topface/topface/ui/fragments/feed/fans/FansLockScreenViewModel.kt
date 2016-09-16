package com.topface.topface.ui.fragments.feed.fans

import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.databinding.LayoutEmptyFansBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject


class FansLockScreenViewModel(binding: LayoutEmptyFansBinding, private val mNavigator: IFeedNavigator, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<LayoutEmptyFansBinding>(binding) {

    @Inject lateinit var mState: TopfaceAppState
    private var mBalanceSubscription: Subscription? = null

    init {
        App.get().inject(this)
        mBalanceSubscription = mState.getObservable(BalanceData::class.java)
                .first { it.premium }
                .subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
                    override fun onNext(balanceData: BalanceData?) {
                        if (balanceData != null && balanceData.premium) {
                            mIFeedUnlocked.onFeedUnlocked()
                        }
                    }
                })
    }

    fun showPurchaseVip() = mNavigator.showPurchaseVip()

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mBalanceSubscription)
    }

}