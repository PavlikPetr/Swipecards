package com.topface.topface.ui.dialogs.new_rate

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.topface.R
import com.topface.topface.databinding.GoogleFeedbackInviteBinding
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import org.jetbrains.anko.layoutInflater

class FeedbackInvitePopup(private val mNavigator: FeedNavigator, private val mApi: FeedApi): DialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "Feedback_inite_popup"
    }

    private val mViewModel by lazy { GoogleFeedbackInviteViewModel(this@FeedbackInvitePopup) }

    private val mBinding by lazy{
        DataBindingUtil.inflate<GoogleFeedbackInviteBinding>(context.layoutInflater, R.layout.google_feedback_invite,null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding){
        viewModel = mViewModel
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        root
    }

    override fun isCancelable(): Boolean {
        return super.isCancelable()
    }

    override fun closeIt() = dialog.cancel()
}
