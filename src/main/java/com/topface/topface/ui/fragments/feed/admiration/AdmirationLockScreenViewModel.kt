package com.topface.topface.ui.fragments.feed.admiration

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.annotation.DrawableRes
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.BalanceData
import com.topface.topface.data.Profile
import com.topface.topface.databinding.LayoutEmptyAdmirationsBinding
import com.topface.topface.state.IStateDataUpdater
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import com.topface.topface.utils.RxUtils
import com.topface.topface.viewModels.BaseViewModel
import rx.Subscription
import javax.inject.Inject

/**
 * МV для заглушки экрана восхищений
 * Created by siberia87 on 30.09.16.
 */
class AdmirationLockScreenViewModel(binding: LayoutEmptyAdmirationsBinding,
                                    private val mNavigator: IFeedNavigator,
                                    private val dataUpdater: IStateDataUpdater) : BaseViewModel<LayoutEmptyAdmirationsBinding>(binding) {
	@Inject lateinit var mState: TopfaceAppState
	lateinit private var mBalanceData: BalanceData
	private var mBalanceSubscription: Subscription
	val message = ObservableField<String>()

	init {
		App.get().inject(this)
		mBalanceSubscription = mState.getObservable(BalanceData::class.java).subscribe {
			mBalanceData = it
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
	val flipperVisibility = ObservableInt(View.VISIBLE)

	fun onBuyCoins() = mNavigator.showPurchaseCoins()

	fun onBuyVipClick() = mNavigator.showPurchaseVip()

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