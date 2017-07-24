package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes.stubs

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.statistics.generated.NewProductsKeysGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.IApi
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Options
import com.topface.topface.data.Profile
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.extensions.getString
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Subscription

/**
 * Заглушка разблокировки за деньги
 */
class LockForMoneyViewModel(private val mIFeedUnlocked: IFeedUnlocked) {

    private val mState by lazy {
        App.getAppComponent().appState()
    }

    var mApi: IApi? = null
    var buyVipAction: () -> Unit = {}
    lateinit private var mBalanceData: BalanceData
    private var mBalanceSubscription: Subscription?
    private var mOptionsSubscription: Subscription?
    val message = ObservableField<String>()
    val buttonMessage = ObservableField<String>()
    private var mBlockSympathy = App.get().options.blockSympathy
    private val isNeedWoman = App.get().profile.dating?.sex == Profile.GIRL
    val muzzleVisibility = ObservableInt(View.VISIBLE)
    val firstMuzzle = ObservableInt(if (isNeedWoman) R.drawable.likes_female_one else R.drawable.likes_male_one)
    val secondMuzzle = ObservableInt(if (isNeedWoman) R.drawable.likes_female_two else R.drawable.likes_male_two)
    val thirdMuzzle = ObservableInt(if (isNeedWoman) R.drawable.likes_female_three else R.drawable.likes_male_three)

    init {
        mBalanceSubscription = mState.getObservable(BalanceData::class.java).subscribe(shortSubscription {
            mBalanceData = it
        })
        mOptionsSubscription = mState.getObservable(Options::class.java).subscribe(shortSubscription {
            mBlockSympathy = it.blockSympathy.apply {
                message.set(it.blockSympathy.text ?: R.string.likes_buy_vip.getString())
                buttonMessage.set(it.blockSympathy.buttonText ?: R.string.buying_vip_status.getString())
            }
        })
    }

    fun onBuyCoins() {
        NewProductsKeysGeneratedStatistics.sendNow_LIKES_ZERODATA_GO_PURCHASES(App.getContext())
        buyVipAction
    }

    fun onBuyVipClick() {
        if (mBalanceData.money >= mBlockSympathy.price) {
            mApi?.callLikesAccessRequest()?.subscribe({
                if (it.completed) {
                    FlurryManager.getInstance().sendSpendCoinsEvent(mBlockSympathy.price, FlurryManager.LIKES_UNLOCK)
                    mIFeedUnlocked.onFeedUnlocked()
                }
            }, {
                if (it?.message?.toInt() == ErrorCodes.PAYMENT) {
                    onBuyCoins()
                }
            })
        } else {
            onBuyCoins()
        }
    }

    fun release() {
        arrayOf(mOptionsSubscription, mBalanceSubscription).safeUnsubscribe()
    }

}