package com.topface.topface.ui.fragments.feed.dialogs

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.IntDef
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.api.responses.FeedBookmark
import com.topface.topface.data.FeedDialog
import com.topface.topface.data.FeedItem
import com.topface.topface.databinding.DeleteOrBlacklistPopupBinding
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.http.IRequestClient
import org.jetbrains.anko.layoutInflater


class PopupMenuFragment : DialogFragment(), IDialogCloser {

    var item: FeedItem? = null

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(DIALOGS_TYPE, MUTUAL_TYPE, ADMIRATION_TYPE)
    annotation class MenuPopupType

    companion object {
        const val TAG = "popup_menu_fragment"
        const val FEED_ITEM_TAG = "feed_item"
        const val POPUP_MENU_TYPE = "popup_menu_type"
        const val DIALOGS_TYPE = 1L
        const val MUTUAL_TYPE = 2L
        const val ADMIRATION_TYPE = 3L

        fun getInstance(item: FeedItem, @PopupMenuFragment.MenuPopupType menuPopupType: Long) = PopupMenuFragment().apply {
            arguments = Bundle().apply {
                putParcelable(FEED_ITEM_TAG, item)
                putLong(POPUP_MENU_TYPE, menuPopupType)
            }
        }
    }

    private lateinit var mViewModel: MenuPopupViewModel

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding) {
        item = arguments.getParcelable(FEED_ITEM_TAG)
        val type = arguments.getLong(POPUP_MENU_TYPE)
        item?.let {
            mViewModel = when (type) {
                DIALOGS_TYPE -> DialogsMenuPopupViewModel(it as FeedDialog, mApi, this@PopupMenuFragment)
                MUTUAL_TYPE -> SympathyMenuPopupViewModel(it as FeedBookmark, App.getAppComponent().api(), this@PopupMenuFragment)
                ADMIRATION_TYPE -> AdmirationMenuPopupViewModel(it as FeedBookmark, App.getAppComponent().api(), this@PopupMenuFragment)
                else -> DialogsMenuPopupViewModel(it as FeedDialog, mApi, this@PopupMenuFragment)
            }
            model = mViewModel
            dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        }
        root
    }

    private val mBinding by lazy {
        DataBindingUtil.inflate<DeleteOrBlacklistPopupBinding>(context.layoutInflater, R.layout.delete_or_blacklist_popup, null, false)
    }

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient, DeleteFeedRequestFactory(context))
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.release()
    }

    override fun closeIt() = dialog?.cancel() ?: Unit
}