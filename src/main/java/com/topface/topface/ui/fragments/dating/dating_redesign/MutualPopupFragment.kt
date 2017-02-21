package com.topface.topface.ui.fragments.dating.dating_redesign

import android.view.View
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.PopupMutuallyBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_base.IFeedNavigator
import kotlin.properties.Delegates

/**
 * Попап Взаимных симпатий
 */
class MutualPopupFragment(private val mNavigator: IFeedNavigator, private val mMutualUser: FeedUser) : AbstractDialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "mutual_popup_fragment"
//        fun newInstance(mNavigator: IFeedNavigator, mMutualUser: FeedUser) = MutualPopupFragment(mNavigator, mMutualUser)
    }

    private val mViewModel by lazy {
        PopupMutualViewModel(mNavigator, mMutualUser, this)
    }

    private var mBinding by Delegates.notNull<PopupMutuallyBinding>()

    override fun initViews(root: View?) {
        mBinding = PopupMutuallyBinding.bind(root)
        mBinding.setModel(mViewModel)
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.popup_mutually

    override fun closeIt() = dialog.cancel()

}