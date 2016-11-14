package com.topface.topface.ui.fragments.feed.dating

import android.view.View
import com.topface.topface.R
import com.topface.topface.databinding.LayoutEmptyDatingBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.http.IRequestClient

/**
 * Фрагмент "ненайденных по фильтру" профайлов для экрана знакомств
 */
class DatingEmptyFragment() : AbstractDialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "empty_dating_fragment"
        fun newInstance() = DatingEmptyFragment()
    }

    private lateinit var mBinding: LayoutEmptyDatingBinding

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient)
    }

    private val mNavigator by lazy {
        FeedNavigator(activity as IActivityDelegate)
    }

    private val mViewModel by lazy {
        DatingEmptyFragmentViewModel(mBinding, mApi, mNavigator, this)
    }

    override fun initViews(root: View) {
        mBinding = LayoutEmptyDatingBinding.bind(root)
        mBinding.setViewModel(mViewModel)
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.layout_empty_dating

    override fun closeIt() = dialog.cancel()

}