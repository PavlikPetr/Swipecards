package com.topface.topface.ui.fragments.feed.likes

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Options
import com.topface.topface.data.Profile
import com.topface.topface.databinding.LayoutEmptyLikesBinding
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.IStateDataUpdater
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.ui.fragments.feed.feed_base.IFeedUnlocked
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.rx.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscriber
import rx.Subscription
import javax.inject.Inject

/**
 * Моделька для заглушки лайков
 * Created by tiberal on 10.08.16.
 */
class LikesLockScreenViewModel(binding: LayoutEmptyLikesBinding, private val mApi: FeedApi,
                               private val mNavigator: IFeedNavigator, private val dataUpdater: IStateDataUpdater,
                               private val mIFeedUnlocked: IFeedUnlocked) : BaseViewModel<LayoutEmptyLikesBinding>(binding) {

    @Inject lateinit var mState: TopfaceAppState
    lateinit private var mBalanceData: BalanceData
    private var mBalanceSubscription: Subscription
    private var mOptionsSubscription: Subscription
    val message = ObservableField<String>()
    val buttonMessage = ObservableField<String>()
    private var mBlockSympathy = dataUpdater.options.blockSympathy

    init {
        //выпилить со вторым даггером
        App.get().inject(this)
        mBalanceSubscription = mState.getObservable(BalanceData::class.java).subscribe {
            mBalanceData = it
        }
        mOptionsSubscription = mState.getObservable(Options::class.java).subscribe {
            mBlockSympathy = it.blockSympathy.apply {
                message.set(it.blockSympathy.text ?: context.getString(R.string.likes_buy_vip))
                buttonMessage.set(it.blockSympathy.buttonText ?: context.getString(R.string.buying_vip_status))
            }
        }
    }

    /*
      0 - buy likes, 1 -  buy VIP, 3 do something or by vip
     */
    val currentChildPod = ObservableInt(1)
    val muzzleVisibility = ObservableInt(View.VISIBLE)
    val firstMuzzle = ObservableInt(getMuzzleIcon(1))
    val secondMuzzle = ObservableInt(getMuzzleIcon(2))
    val thirdMuzzle = ObservableInt(getMuzzleIcon(3))
    val likesAccessProgressVisibility = ObservableInt(View.INVISIBLE)
    val flipperVisibility = ObservableInt(View.VISIBLE)

    fun onBuyCoins() = mNavigator.showPurchaseCoins()

    fun onBuyVipClick() {
        if (mBalanceData.money >= mBlockSympathy.price) {
            likesAccessProgressVisibility.set(View.VISIBLE)
            flipperVisibility.set(View.INVISIBLE)
            mApi.callLikesAccessRequest().subscribe(object : Subscriber<IApiResponse>() {
                override fun onError(e: Throwable?) {
                    if (e?.message?.toInt() == ErrorCodes.PAYMENT) {
                        onBuyCoins()
                    }
                }

                override fun onNext(t: IApiResponse?) {
                    FlurryManager.getInstance().sendSpendCoinsEvent(mBlockSympathy.price, FlurryManager.LIKES_UNLOCK)
                    binding.root.visibility = View.GONE
                    mIFeedUnlocked.onFeedUnlocked()
                }

                override fun onCompleted() {
                    likesAccessProgressVisibility.set(View.INVISIBLE)
                    flipperVisibility.set(View.VISIBLE)
                }
            })
        } else {
            onBuyCoins()
        }
    }

    private fun getMuzzleIcon(iconNumber: Int) = when (iconNumber) {
        1 -> choiceIcon(R.drawable.likes_female_one, R.drawable.likes_male_one)
        2 -> choiceIcon(R.drawable.likes_female_two, R.drawable.likes_male_two)
        3 -> choiceIcon(R.drawable.likes_female_three, R.drawable.likes_male_three)
        else -> -1
    }


    private fun choiceIcon(@DrawableRes femaleIcon: Int, @DrawableRes maleIcon: Int) =
            if (dataUpdater.profile.dating != null && dataUpdater.profile.dating.sex == Profile.GIRL) {
                femaleIcon
            } else {
                maleIcon
            }

    override fun release() {
        RxUtils.safeUnsubscribe(mBalanceSubscription)
    }

}