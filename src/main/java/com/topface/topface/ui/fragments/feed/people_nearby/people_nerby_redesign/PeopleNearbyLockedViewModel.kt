package com.topface.topface.ui.fragments.feed.people_nearby.people_nerby_redesign

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Options
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.state.EventBus
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.PurchasesFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.FlurryManager
import com.topface.topface.utils.extensions.safeToInt
import com.topface.topface.utils.extensions.showShortToast
import com.topface.topface.utils.rx.safeUnsubscribe
import com.topface.topface.utils.rx.shortSubscription
import rx.Observable
import rx.Subscription
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * ViewModel блокировки экрана "Люди рядом"
 * Created by ppavlik on 11.01.17.
 */

class PeopleNearbyLockedViewModel(private val mApi: FeedApi, private val mNavigator: FeedNavigator) {

    val description = ObservableField<String>()
    val unlockByCoinsText = ObservableField<String>()
    val unlockByCoinsVisibility = ObservableInt(View.GONE)
    val premiumText = ObservableField<String>()
    val premiumTextVisibility = ObservableInt(View.GONE)
    val unlockByPremiumButtonText = ObservableField<String>()
    val unlockByPremiumButtonVisibility = ObservableInt(View.GONE)

    @Inject lateinit var mState: TopfaceAppState
    @Inject lateinit var mEventBus: EventBus

    companion object {
        private const val BUY_COINS_FROM = "PeoplePaidNearby"
        private const val BUY_VIP_FROM = "PeopleNearby"
    }

    private val mSubscriptions = CompositeSubscription()
    private var mPeopleNearbyAccessSubscription: Subscription? = null
    private var mBlockPeopleNearby = App.get().options.blockPeople
    private var mUnlockAllForPremium = App.get().options.unlockAllForPremium
    private var mBalanceData: BalanceData? = null

    init {
        App.get().inject(this)
        unlockByCoinsText.addOnPropertyChangedCallback(object : android.databinding.Observable
        .OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: android.databinding.Observable?, p1: Int) =
                    unlockByCoinsVisibility.set(getVisibility(obs))
        })
        premiumText.addOnPropertyChangedCallback(object : android.databinding.Observable
        .OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: android.databinding.Observable?, p1: Int) =
                    premiumTextVisibility.set(if (mUnlockAllForPremium) {
                        getVisibility(obs)
                    } else View.GONE)
        })
        unlockByPremiumButtonText.addOnPropertyChangedCallback(object : android.databinding.Observable
        .OnPropertyChangedCallback() {
            override fun onPropertyChanged(obs: android.databinding.Observable?, p1: Int) =
                    unlockByPremiumButtonVisibility.set(if (mUnlockAllForPremium) {
                        getVisibility(obs)
                    } else View.GONE)
        })
        mSubscriptions.add(mState.getObservable(BalanceData::class.java)
                .distinctUntilChanged()
                .subscribe(shortSubscription {
                    mBalanceData = it
                }))

        mSubscriptions.add(mState.getObservable(Options::class.java)
                .flatMap { options -> Observable.just(Pair(options.unlockAllForPremium, options.blockPeople)) }
                .distinctUntilChanged()
                .subscribe(shortSubscription {
                    mUnlockAllForPremium = it.first
                    with(it.second) {
                        mBlockPeopleNearby = this
                        description.set(text)
                        unlockByCoinsText.set(buttonText)
                        premiumText.set(textPremium)
                        unlockByPremiumButtonText.set(buttonTextPremium)
                    }
                }))
    }

    private fun getVisibility(obs: android.databinding.Observable?) =
            if (((obs as? ObservableField<*>)?.get() as? String)?.isEmpty() ?: true) View.GONE else View.VISIBLE

    fun release() {
        mSubscriptions.safeUnsubscribe()
    }

    fun onBuyVipClick() {
        mNavigator.showPurchaseVip(BUY_VIP_FROM)
    }

    fun unlockByCoinsClick() {
        if (mBalanceData?.money ?: 0 < mBlockPeopleNearby.price) {
            mNavigator.showPurchaseCoins(BUY_COINS_FROM)
        } else {
            mPeopleNearbyAccessSubscription = mApi.callPeopleNearbyAccess()
                    .subscribe({
                        FlurryManager.getInstance()
                                .sendSpendCoinsEvent(mBlockPeopleNearby.price,
                                        FlurryManager.PEOPLE_NEARBY_UNLOCK)
                        mEventBus.setData(PeopleNearbyUpdate())

                    },
                            {
                                when (it.message.safeToInt(ErrorCodes.INTERNAL_SERVER_ERROR)) {
                                    ErrorCodes.PAYMENT -> mNavigator.showPurchaseCoins(BUY_COINS_FROM,
                                            PurchasesFragment.TYPE_PEOPLE_NEARBY, mBlockPeopleNearby.price)
                                    else -> R.string.general_data_error.showShortToast()
                                }
                            }
                    )
        }
    }
}