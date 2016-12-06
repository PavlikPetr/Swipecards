package com.topface.topface.ui.fragments.feed.dialogs

import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.DeleteOrBlacklistPopupBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.feed.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_api.FeedRequestFactory
import com.topface.topface.utils.http.IRequestClient

/**
 * Created by mbulgakov on 06.12.16.
 */
class DialogMenuFragment(val item: FeedDialog) : AbstractDialogFragment(), IDialogCloser {

    companion object {
        fun newInstance(item: FeedDialog) = DialogMenuFragment(item)
    }

    private lateinit var mBinding: DeleteOrBlacklistPopupBinding

    private val mFeedRequestFactory by lazy {
        FeedRequestFactory(context)
    }

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient, mFeedRequestFactory)
    }

    private val mViewModel by lazy {
        DialogsMenuPopupViewModel(item, mApi, this)
    }

    override fun initViews(root: View?) {
        mBinding = DeleteOrBlacklistPopupBinding.bind(root)
        mBinding.setModel(mViewModel)
    }

    override fun isModalDialog(): Boolean {
        return true
    }

    override fun getDialogLayoutRes() = R.layout.delete_or_blacklist_popup

    override fun closeIt() = dialog.cancel()
}