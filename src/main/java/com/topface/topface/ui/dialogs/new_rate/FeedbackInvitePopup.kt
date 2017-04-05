package com.topface.topface.ui.dialogs.new_rate

import android.support.v4.app.DialogFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator

class FeedbackInvitePopup(private val mNavigator: FeedNavigator, private val mApi: FeedApi): DialogFragment() {
    companion object {
        const val TAG = "Feedback_inite_popup"

    }
}