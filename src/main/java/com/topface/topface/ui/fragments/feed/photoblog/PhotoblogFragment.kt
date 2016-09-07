package com.topface.topface.ui.fragments.feed.photoblog

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.databinding.ViewStubProxy
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedPhotoBlog
import com.topface.topface.databinding.HeaderPhotoBlogBinding
import com.topface.topface.databinding.LayoutEmptyPhotoblogBinding
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedFragment
import com.topface.topface.ui.fragments.feed.feed_base.BaseFeedLockerController
import com.topface.topface.utils.AddPhotoHelper
import com.topface.topface.utils.RxUtils
import com.topface.topface.utils.adapter_utils.InjectViewBucket
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * Фрагмент постановки в лидеры
 * Created by tiberal on 05.09.16.
 */
class PhotoblogFragment : BaseFeedFragment<FeedPhotoBlog, LayoutEmptyPhotoblogBinding>() {

    companion object {
        val ADD_TO_PHOTO_BLOG_ACTIVITY_ID = 1
        private val UPDATE_DELAY = 20L

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
        val bucket = InjectViewBucket() {
            val binding = DataBindingUtil.inflate<HeaderPhotoBlogBinding>(App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater,
                    R.layout.header_photo_blog, it, true)
            binding.viewModel = mHeaderViewModel
            binding.executePendingBindings()
            binding.root
        }
        bucket.addFilter { pos -> pos == 0 }
        adapter.registerViewBucket(bucket)
        adapter
    }
    private val mPhotoHelper by lazy {
        AddPhotoHelper(this@PhotoblogFragment, null).setOnResultHandler(mHandler)
    }
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            AddPhotoHelper.handlePhotoMessage(msg)
        }
    }
    private lateinit var mRefreshIntervalSubscription: Subscription

    init {
        mRefreshIntervalSubscription = Observable.interval(UPDATE_DELAY, UPDATE_DELAY, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : RxUtils.ShortSubscription<Long>() {
            override fun onNext(type: Long?) = mViewModel.onRefresh()
        })
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.refresh.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        RxUtils.safeUnsubscribe(mHeaderViewModel.profileSubscription)
        RxUtils.safeUnsubscribe(mRefreshIntervalSubscription)
        mPhotoHelper.releaseHelper()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_TO_PHOTO_BLOG_ACTIVITY_ID) {
            mViewModel.onRefresh()
        }
        mPhotoHelper.processActivityResult(requestCode, resultCode, data)
    }

    override fun createLockerFactory() = object : BaseFeedLockerController.ILockScreenVMFactory<LayoutEmptyPhotoblogBinding> {
        override fun construct(binding: ViewDataBinding) = PhotoblogLockScreenViewModel(binding as LayoutEmptyPhotoblogBinding)
    }

    override fun getEmptyFeedLayout() = R.layout.layout_empty_photoblog

    override fun getTitle(): String? = getString(R.string.general_photoblog)

}

