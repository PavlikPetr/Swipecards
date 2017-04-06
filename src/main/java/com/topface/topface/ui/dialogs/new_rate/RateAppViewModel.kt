package com.topface.topface.ui.dialogs.new_rate

import android.content.Context
import android.databinding.ObservableBoolean
import android.databinding.ObservableFloat
import android.os.Bundle
import android.widget.RatingBar
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.requests.AppRateRequest
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.handlers.ApiHandler
import com.topface.topface.ui.dialogs.new_rate.RateAppFragment.Companion.RATING
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
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_RATE(Slices().apply { putSlice(RATING, currentRating?.toString()) })
        if (isTimeForGoogleFeedback) {
            mNavigator.showFeedbackInvitePopup(mNavigator, mApi)
        } else {
            mNavigator.showGoogleFeedbackPopup(mNavigator, mApi, currentRating.get())
        }
        sendRateRequest(currentRating.get().toLong())
        iDialogCloser.closeIt()
    }

    fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        currentRating.set(rating)
        if (rating <= 0) {
            buttonEnabled.set(false)
        } else if (rating >= 4) {
            isTimeForGoogleFeedback = true
            buttonEnabled.set(true)
        } else {
            isTimeForGoogleFeedback = false
            buttonEnabled.set(true)
        }
    }

    private fun sendRateRequest(rating: Long) {
        AppRateRequest(App.getContext() ,rating).callback(object : ApiHandler() {
            override fun success(response: IApiResponse) {}

            override fun fail(codeError: Int, response: IApiResponse) {}
        }).exec()
    }

    fun closeButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        sendRateRequest(AppRateRequest.NO_RATE)
        iDialogCloser.closeIt()
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