package com.topface.topface.ui.fragments.feed.dialogs

import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.DeleteOrBlacklistPopupBinding
import com.topface.topface.ui.dialogs.BaseDialog
import com.topface.topface.ui.fragments.feed.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.http.IRequestClient

/**
 * Created by mbulgakov on 06.12.16.
 */
class DialogMenuFragment(val item: FeedDialog) : BaseDialog(), IDialogCloser {

    companion object {
        fun newInstance(item: FeedDialog) = DialogMenuFragment(item)
    }


    override fun getDialogStyleResId(): Int = 0

    private lateinit var mBinding: DeleteOrBlacklistPopupBinding

    private val mDeleteFeedRequestFactory by lazy {
        DeleteFeedRequestFactory(context)
    }

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient, mDeleteFeedRequestFactory)
    }

    private val mViewModel by lazy {
        DialogsMenuPopupViewModel(item, mApi, this)
    }

    override fun initViews(root: View?) {
        mBinding = DeleteOrBlacklistPopupBinding.bind(root)
        mBinding.setModel(mViewModel)
    }

    override fun getDialogLayoutRes() = R.layout.delete_or_blacklist_popup

    override fun closeIt() = dialog.cancel()
}