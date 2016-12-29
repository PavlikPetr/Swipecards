package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.topface.R
import com.topface.topface.data.FeedDialog
import com.topface.topface.databinding.DeleteOrBlacklistPopupBinding
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.http.IRequestClient
import org.jetbrains.anko.layoutInflater


class DialogMenuFragment : DialogFragment(), IDialogCloser {

    var item: FeedDialog? = null

    companion object {
        const val TAG = "dialog_menu_fragment"
        const val FEED_DIALIG_ITEM = "feed_dialig_item"

        fun getInstance(item: FeedDialog) = DialogMenuFragment().apply { arguments = Bundle().apply { putParcelable(FEED_DIALIG_ITEM, item) } }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding) {
        item = arguments.getParcelable(FEED_DIALIG_ITEM)
        item?.let {
            model = DialogsMenuPopupViewModel(it, mApi, this@DialogMenuFragment)
        }
        root
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<DeleteOrBlacklistPopupBinding>(context.layoutInflater, R.layout.delete_or_blacklist_popup, null, false)
    }

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient, DeleteFeedRequestFactory(context))
    }

    override fun closeIt() = dialog?.cancel() ?: Unit
}