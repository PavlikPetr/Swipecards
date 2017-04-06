package com.topface.topface.ui.dialogs.new_rate

import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.topface.R
import com.topface.topface.databinding.GoogleFeedbackPopupBinding
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import org.jetbrains.anko.layoutInflater

class GoogleFeedbackPopup(private val mFeedNavigator: FeedNavigator, private val mApi: FeedApi, private val rate: Float): DialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "Google_feedback_popup"
    }

    private val mViewModel by lazy { GoogleFeedbackPopopViewModel(this@GoogleFeedbackPopup, rate) }

    private val mBinding by lazy {
        DataBindingUtil.inflate<GoogleFeedbackPopupBinding>(context.layoutInflater, R.layout.google_feedback_popup, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding) {
        viewModel = mViewModel
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        root
    }

    override fun closeIt() = dialog.cancel()
}