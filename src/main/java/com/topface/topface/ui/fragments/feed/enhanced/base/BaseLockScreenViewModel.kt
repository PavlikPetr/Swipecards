package com.topface.topface.ui.fragments.feed.enhanced.base

import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscribe
import rx.Subscription

/**
 * Created by ppavlik on 28.09.16.
 * Базовая модель контроллера заглушек, которая умеет вызывать обновление списка если был куплен вип
 */
abstract class BaseLockScreenViewModel(private val mIFeedUnlocked: IFeedUnlocked) : BaseViewModel() {
    private val mState by lazy {
        App.getAppComponent().appState()
    }
    private var mBalanceSubscription: Subscription? = null
    private var mBalance: BalanceData? = null

    init {
        mBalanceSubscription = mState.getObservable(BalanceData::class.java)
                .shortSubscribe {
                    it?.let {
                        data ->
                        if (data.premium) {
                            if (!(mBalance?.premium ?: true)) {
                                mIFeedUnlocked.onFeedUnlocked()
                            }
                            mBalanceSubscription.safeUnsubscribe()
                        }
                        mBalance = data
                    }
                }
    }

    override fun release() {
        mBalanceSubscription.safeUnsubscribe()
    }
}