package com.topface.topface.ui.dialogs.new_rate

import android.databinding.ObservableBoolean
import android.databinding.ObservableFloat
import android.os.Bundle
import android.widget.RatingBar
import com.topface.topface.data.FeedDialog
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.ILifeCycle

class RateAppViewModel(private val mNavigator: FeedNavigator, private val iDialogCloser: IDialogCloser, private val mApi: FeedApi) : ILifeCycle {
    companion object {
        const val IS_ENABLED_BUTTON = "enabled_button"
        const val CURRENT_RATING = "current_rating"
    }

    var currentRating = ObservableFloat()
    val buttonEnabled = ObservableBoolean(false)

    var isTimeForGoogleFeedback = false


    fun okButtonClick() {
        if (isTimeForGoogleFeedback){
            mNavigator.showGoogleFeedbackPopup(mNavigator,mApi)
        } else{
            mNavigator.showFeedbackInvitePopup(mNavigator,mApi)
        }
    }

    fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        if (rating <= 0) {
            buttonEnabled.set(false)
        } else isTimeForGoogleFeedback = rating < 4
    }

    fun closeButtonClick() {

    }


    //    сохраняем состояние
    override fun onSavedInstanceState(state: Bundle) {
        super.onSavedInstanceState(state)
        with(state) {
            putBoolean(IS_ENABLED_BUTTON, buttonEnabled.get())
            putFloat(CURRENT_RATING, currentRating.get())

        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        with(state) {
            getBoolean(IS_ENABLED_BUTTON, buttonEnabled.get())
            getFloat(CURRENT_RATING, currentRating.get())
        }
    }

}