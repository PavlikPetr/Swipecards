package com.topface.topface.ui.fragments.feed.dialogs

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.DeleteOrBlacklistPopupBinding
import com.topface.topface.ui.fragments.feed.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.http.IRequestClient

/**
 * Created by mbulgakov on 06.12.16.
 */
class DialogMenuFragment(val item: FeedDialog) : DialogFragment(), IDialogCloser {

    companion object {
        fun newInstance(item: FeedDialog) = DialogMenuFragment(item)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val resLayoutId = getDialogLayoutRes()
        var root: View? = null
        if (resLayoutId != 0) {
            root = inflater!!.inflate(resLayoutId, container, false)
            initViews(root)
        }
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        return root
    }

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

    fun initViews(root: View?) {
        mBinding = DeleteOrBlacklistPopupBinding.bind(root)
        mBinding.setModel(mViewModel)
    }

    fun getDialogLayoutRes() = R.layout.delete_or_blacklist_popup

    override fun closeIt() = dialog.cancel()
}