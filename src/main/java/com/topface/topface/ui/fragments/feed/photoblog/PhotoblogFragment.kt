package com.topface.topface.ui.fragments.feed.photoblog

import android.content.Context
import android.content.Intent
import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.data.FixedViewInfo
import com.topface.topface.databinding.LayoutEmptyPhotoblogBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.extensions.getString

/**
 * Фрагмент постановки в лидеры
 * Created by tiberal on 05.09.16.
 */
class PhotoblogFragment : BaseFeedFragment<FeedPhotoBlog, LayoutEmptyPhotoblogBinding>() {

    companion object {
        val ADD_TO_PHOTO_BLOG_ACTIVITY_ID = 1
    }

    private val mHeaderViewModel by  lazy {
        HeaderPhotoBlogViewModel(mNavigator)
    }

    override val mViewModel by lazy {
        PhotoblogFragmentViewModel(mBinding, mNavigator, mApi)
    }
    override val mLockerControllerBase by lazy {
        PhotoblogLockController(mBinding.emptyFeedStub as ViewStubProxy)
    }
    override val mAdapter by lazy {
        val adapter = PhotoblogAdapter(mNavigator)
        adapter.setHeader(FixedViewInfo(R.layout.header_photo_blog, mHeaderViewModel))
        adapter
    }
    private lateinit var mPhotoHelper: AddPhotoHelper

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            AddPhotoHelper.handlePhotoMessage(msg)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mPhotoHelper = AddPhotoHelper(this@PhotoblogFragment, null).setOnResultHandler(mHandler)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.refresh.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        RxUtils.safeUnsubscribe(mHeaderViewModel.profileSubscription)
        mPhotoHelper.releaseHelper()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TO_PHOTO_BLOG_ACTIVITY_ID) {
            mViewModel.loadTopFeeds()
        }
        mPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyPhotoblogBinding> {
        override fun construct(binding: ViewDataBinding) = PhotoblogLockScreenViewModel(binding as LayoutEmptyPhotoblogBinding)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_photoblog

    override fun onResume() {
        super.onResume()
        ToolbarManager.setToolbarSettings(ToolbarSettingsData(R.string.general_photoblog.getString()))
    }
}

