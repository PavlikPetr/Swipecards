package com.topface.topface.ui.dialogs.new_rate

import android.app.DialogFragment
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator

class GoogleFeedbackPopup(private val mNavigator: FeedNavigator, private val mApi: FeedApi): DialogFragment() {
    companion object {
        const val TAG = "Feedback_inite_popup"

    }
}