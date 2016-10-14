package com.topface.topface.ui.fragments.feed.admiration

import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedLike
import com.topface.topface.databinding.LayoutEmptyAdmirationsBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.admiration.AdmirationsFeedAdapter
import com.topface.topface.viewModels.BaseViewModel

/**
 * Фрагмент восхищений
 * Created by siberia87 on 29.09.16.
 */
@FlurryOpenEvent(name = AdmirationFragment.PAGE_NAME)
class AdmirationFragment : BaseFeedFragment<FeedLike, LayoutEmptyAdmirationsBinding>() {

	companion object {
		const val PAGE_NAME = "Admirations"
	}

	override val mViewModel by lazy {
		AdmirationFragmentViewModel(mBinding, mNavigator, mApi)
	}

	override val mLockerControllerBase by lazy {
		AdmirationLockController(mBinding.emptyFeedStub as ViewStubProxy)
	}

	override val mAdapter by lazy {
		AdmirationsFeedAdapter(mNavigator, mApi)
	}

	override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyAdmirationsBinding> {
		override fun construct(binding: ViewDataBinding): BaseViewModel<LayoutEmptyAdmirationsBinding> {
			return AdmirationLockScreenViewModel(binding as LayoutEmptyAdmirationsBinding, mNavigator, App.get().dataUpdater, this@AdmirationFragment)
		}
	}

	override fun getEmptyFeedLayout(): Int = R.layout.layout_empty_admirations

	override fun getTitle(): String? = getString(R.string.general_sympathies)
}