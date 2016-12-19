package com.topface.topface.ui.fragments.feed.feed_base

import android.databinding.ViewDataBinding
import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

/**
 * Created by ppavlik on 28.09.16.
 * Базовая модель контроллера заглушек, которая умеет вызывать обновление списка если был куплен вип
 */
abstract class BaseLockScreenViewModel<T : ViewDataBinding>(binding: T, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<T>(binding) {
    @Inject lateinit var mState: TopfaceAppState
    private var mBalanceSubscription: Subscription? = null
    private var mBalance: BalanceData? = null

    init {
        App.get().inject(this)
        mBalanceSubscription = mState.getObservable(BalanceData::class.java)
                .subscribe(object : RxUtils.ShortSubscription<BalanceData>() {
                    override fun onNext(balanceData: BalanceData?) {
                        balanceData?.let {
                            data ->
                            if (data.premium) {
                                if (!(mBalance?.premium ?: true)) {
                                    mIFeedUnlocked.onFeedUnlocked()
                                }
                                RxUtils.safeUnsubscribe(mBalanceSubscription)
                            }
                            mBalance = data
                        }
                    }
                })
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mBalanceSubscription)
    }
}