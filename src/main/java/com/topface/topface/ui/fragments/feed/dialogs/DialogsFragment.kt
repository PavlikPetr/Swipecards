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
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController

/**
 * Это кароч диалоги
 * Created by tiberal on 18.09.16.
 */

@FlurryOpenEvent(name = DialogsFragment.PAGE_NAME)
class DialogsFragment : BaseFeedFragment<FeedDialog, LayoutEmptyDialogsBinding>() {

    companion object {
        const val PAGE_NAME = "Dialogs"
    }

    private val mAppOfTheDayModel by lazy {
        DialogAppOfTheDayModel(App.getContext(), App.get().options.appOfTheDay)
    }

    override val mViewModel by lazy {
        DialogsFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        DialogsLockController(mBinding.emptyFeedStub as ViewStubProxy)
    }
    override val mAdapter by lazy {
        val adapter = DialogsAdapter(mNavigator)
        App.get().options.appOfTheDay?.let {
            adapter.setHeader(FixedViewInfo(R.layout.header_photo_blog, mAppOfTheDayModel))
        }
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
}