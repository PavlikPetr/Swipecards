package com.topface.topface.ui.fragments.feed.admiration

import android.databinding.ViewStubProxy
import com.topface.topface.databinding.LayoutEmptyAdmirationsBinding
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Контроллер заглушек для экрана восхищений
 * Created by siberia87 on 30.09.16.
 */
class AdmirationLockController(stub: ViewStubProxy) :
		BaseFeedLockerController<LayoutEmptyAdmirationsBinding, AdmirationLockScreenViewModel>(stub) {

	override fun initLockedFeedStub(errorCode: Int) {
		when (errorCode) {
			ErrorCodes.PREMIUM_ACCESS_ONLY -> {
				mStubModel?.currentChildPod?.set(1)
			}
		}
	}

	override fun initEmptyFeedStub() {
		mStubModel?.currentChildPod?.set(0)
	}
}