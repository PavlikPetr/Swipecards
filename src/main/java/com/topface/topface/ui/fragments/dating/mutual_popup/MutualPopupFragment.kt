package com.topface.topface.ui.fragments.dating.mutual_popup

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.FeedUser
import com.topface.topface.databinding.PopupMutuallyBinding
import com.topface.topface.ui.dialogs.AbstractDialogFragment
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.dialogs.new_rate.RateAppFragment
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
        const val GO_TO_BUTTON_TEXT = "go_to_button_text"
        const val BORDERLESS_BUTTON_TEXT = "borderless_button_text"

        fun getInstance(mMutualUser: FeedUser, goToButtonText: String, borderlessButtonText: String) = MutualPopupFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MUTUAL_USER_TAG, mMutualUser)
                putString(GO_TO_BUTTON_TEXT, goToButtonText)
                putString(BORDERLESS_BUTTON_TEXT, borderlessButtonText)
            }
        }
    }

    private var mNavigator: FeedNavigator? = null
        get() = FeedNavigator(activity as IActivityDelegate)

    private val mViewModel by lazy {
        PopupMutualViewModel(getFeedNavigator(), arguments.getParcelable(MUTUAL_USER_TAG), this, arguments.getString(GO_TO_BUTTON_TEXT), arguments.getString(BORDERLESS_BUTTON_TEXT))
    }

    private var mBinding by Delegates.notNull<PopupMutuallyBinding>()

    override fun initViews(root: View?) {
        mBinding = PopupMutuallyBinding.bind(root)
        mBinding.setModel(mViewModel)
    }

    private fun getFeedNavigator(): FeedNavigator {
        if (mNavigator == null) {
            mNavigator = FeedNavigator(activity as IActivityDelegate)
        } else {
            mNavigator
        }
        return mNavigator as FeedNavigator
    }

    override fun onCancel(dialog: DialogInterface?) {
        if (RateAppFragment.isApplicable(App.get().options.ratePopupNewVersion)) {
            getFeedNavigator().showRateAppFragment()
        }
        super.onCancel(dialog)
    }

    override fun isModalDialog() = false

    override fun getDialogLayoutRes() = R.layout.popup_mutually

    override fun closeIt() = dialog.cancel()

}