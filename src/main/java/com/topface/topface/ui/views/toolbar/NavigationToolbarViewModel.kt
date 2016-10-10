package com.topface.topface.ui.views.toolbar

import android.support.v7.widget.Toolbar
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.CountersData
import com.topface.topface.state.TopfaceAppState
import com.topface.topface.utils.RxUtils
import rx.Subscription
import javax.inject.Inject

/**
 * Created by petrp on 09.10.2016.
 */

class NavigationToolbarViewModel(val toolbar: Toolbar, mNavigation: IToolbarNavigation)
: ToolbarBaseViewModel(toolbar, mNavigation = mNavigation) {
	@Inject lateinit var mState: TopfaceAppState
	private var mBalanceSubscription: Subscription? = null
	private var mHasNotification: Boolean? = null

	init {
		App.get().inject(this)
		mBalanceSubscription = mState.getObservable(CountersData::class.java)
				.map {
					it.dialogs > 0 || it.mutual > 0
				}
				.filter {
					mHasNotification == null || mHasNotification != it
				}
				.subscribe(object : RxUtils.ShortSubscription<Boolean>() {
					override fun onNext(isHasNotif: Boolean?) {
						super.onNext(isHasNotif)
						mHasNotification = isHasNotif
						setUpButton(if (mHasNotification != null && mHasNotification!!)
							R.drawable.ic_home_notification
						else
							R.drawable.ic_home)
					}
				})
	}

	override fun release() {
		super.release()
		RxUtils.safeUnsubscribe(mBalanceSubscription)
	}
}
