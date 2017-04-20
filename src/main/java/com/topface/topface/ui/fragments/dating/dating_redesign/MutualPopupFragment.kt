package com.topface.topface.ui.fragments.dating.dating_redesign

import android.os.Bundle
import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.BuyVipStubChatBinding
import com.topface.topface.databinding.MutualStubChatBinding
import com.topface.topface.databinding.PopupMutuallyBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.enhanced.chat.BuyVipStubViewModel
import com.topface.topface.ui.fragments.feed.enhanced.chat.MutualStubChatViewModel
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.IActivityDelegate
import kotlin.properties.Delegates

/**
 * Попап Взаимных симпатий
 */
class MutualPopupFragment : AbstractDialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "mutual_popup_fragment"
        const val MUTUAL_USER_TAG = "mutual_user_tag"

        fun getInstance(mMutualUser: FeedUser) = MutualPopupFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MUTUAL_USER_TAG, mMutualUser)
            }
        }
    }

    private val mNavigator by lazy { FeedNavigator(activity as IActivityDelegate) }

//    private val mViewModel by lazy {
//        PopupMutualViewModel(mNavigator, arguments.getParcelable(MUTUAL_USER_TAG), this)
//    }

    private val mViewModel by lazy {
        MutualStubChatViewModel(arguments.getParcelable(MUTUAL_USER_TAG))
    }

//    private var mBinding by Delegates.notNull<PopupMutuallyBinding>()

    private var mBinding by Delegates.notNull<MutualStubChatBinding>()

//    override fun initViews(root: View?) {
//        mBinding = PopupMutuallyBinding.bind(root)
//        mBinding.setModel(mViewModel)
//    }

    override fun initViews(root: View?) {
        mBinding = MutualStubChatBinding.bind(root)
        mBinding.setViewModel(mViewModel)
    }
    override fun isModalDialog() = false

//    override fun getDialogLayoutRes() = R.layout.popup_mutually

    override fun getDialogLayoutRes() = R.layout.mutual_stub_chat

    override fun closeIt() = dialog.cancel()

}