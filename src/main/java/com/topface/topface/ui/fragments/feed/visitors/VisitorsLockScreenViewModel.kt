package com.topface.topface.ui.fragments.feed.visitors

import android.databinding.ObservableField
import android.view.View
import com.topface.topface.App
import com.topface.topface.data.BalanceData
import com.topface.topface.databinding.LayoutEmptyVisitorsBinding
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

/**
 * Моделька локскрина гостей
 * Created by tiberal on 09.09.16.
 */
class VisitorsLockScreenViewModel(binding: LayoutEmptyVisitorsBinding, val mNavigator: IFeedNavigator, private val mIFeedUnlocked: IFeedUnlocked) :
        BaseViewModel<LayoutEmptyVisitorsBinding>(binding) {

    @Inject lateinit var mState: TopfaceAppState
    private var mBalanceSubscription: Subscription? = null
    private var onButtonClickListener: View.OnClickListener? = null
    val title = ObservableField<String>("")
    val buttonText = ObservableField<String>("")

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

    fun onButtonClick(view: View) {
        onButtonClickListener?.onClick(view)
    }

    fun setOnButtonClickListener(listener: View.OnClickListener) {
        onButtonClickListener = listener;
    }

    override fun release() {
        super.release()
        RxUtils.safeUnsubscribe(mBalanceSubscription)
    }
}