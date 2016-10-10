package com.topface.topface.ui.fragments.feed.dialogs

import android.content.Intent
import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FixedViewInfo
import com.topface.topface.databinding.LayoutEmptyDialogsBinding
import com.topface.topface.statistics.FlurryOpenEvent
import com.topface.topface.ui.ChatActivity
import com.topface.topface.ui.fragments.feed.app_day.models.AppDay
import com.topface.topface.ui.fragments.feed.app_day.models.AppDayImage
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.ui.fragments.feed.feed_utils.convertFeedIdList
import com.topface.topface.ui.fragments.feed.feed_utils.getUserIdList
import java.util.*

/**
 * Это кароч диалоги
 * Created by tiberal on 18.09.16.
 */

@FlurryOpenEvent(name = DialogsFragment.PAGE_NAME)
class DialogsFragment : BaseFeedFragment<FeedDialog, LayoutEmptyDialogsBinding>() {

	private val tempData by lazy {
		AppDay(0, 0, 0, arrayListOf(
				AppDayImage("http://batona.net/uploads/posts/2011-08/1313578288_15.jpg",
						"http://batona.net/uploads/posts/2011-08/1313578288_15.jpg",
						false),
				AppDayImage("https://www.android.com/static/img/android.png",
						"https://www.android.com/static/img/android.png",
						false)
		))
	}

	override fun getDeleteItemsList(mSelected: MutableList<FeedDialog>): ArrayList<String> {
		return mSelected.getUserIdList().convertFeedIdList()
	}

	companion object {
		const val PAGE_NAME = "Dialogs"
	}

	override val mViewModel by lazy {
		DialogsFragmentViewModel(mBinding, mNavigator, mApi)
	}
	override val mLockerControllerBase by lazy {
		DialogsLockController(mBinding.emptyFeedStub as ViewStubProxy)
	}
	override val mAdapter by lazy {
		val adapter = DialogsAdapter(mNavigator, activity)
		adapter.setHeader(FixedViewInfo(bannerRes, tempData))
		adapter
	}

	override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyDialogsBinding> {
		override fun construct(binding: ViewDataBinding) = DialogsLockScreenViewModel(binding as LayoutEmptyDialogsBinding, mNavigator, this@DialogsFragment)
	}

	override fun getEmptyFeedLayout() = R.layout.layout_empty_dialogs

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == ChatActivity.REQUEST_CHAT) {
			data?.let { mViewModel.updatePreview(it) }
		}
	}

	override fun getTitle(): String = getString(R.string.settings_messages)
}