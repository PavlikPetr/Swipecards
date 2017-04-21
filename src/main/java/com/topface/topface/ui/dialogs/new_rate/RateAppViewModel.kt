package com.topface.topface.ui.dialogs.new_rate

import android.databinding.ObservableBoolean
import android.databinding.ObservableFloat
import android.databinding.ObservableInt
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.requests.AppRateRequest
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.dialogs.new_rate.RateAppFragment.Companion.RATING
import com.topface.topface.utils.ILifeCycle

class RateAppViewModel(private val iDialogCloser: IDialogCloser) : ILifeCycle {

    companion object {
        const val IS_ENABLED_BUTTON = "enabled_button"
        const val IS_ENABLED_RATE_LAYOUT = "enabled_rate_layout"
        const val IS_ENABLED_FEEDBACK_LAYOUT = "enabled_feedback_layout"
        const val IS_ENABLED_GOOGLE_LAYOUT = "enabled_google_layout"
        const val CURRENT_RATING = "current_rating"
        const val IS_GOOD_RATE = "is_good_rated"
    }

    var currentRating = ObservableFloat()
    val buttonEnabled = ObservableBoolean(false)

    val layoutRateVisibility = ObservableInt(View.VISIBLE)
    val layoutGoogleVisibility = ObservableInt(View.GONE)
    val layoutFeedbackVisibility = ObservableInt(View.GONE)

    /**
     * результат голосования
     * first - была ли нажата оценка?
     * second - норм оценка была нажата?
     */
    var rateResult = Pair(false, false)

    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    fun okButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_RATE(Slices().apply { putSlice(RATING, currentRating?.toString()) })
        val rateInInt = currentRating.get()
        sendRateRequest(rateInInt.toLong())
        if (rateInInt <= 0) {
        } else if (rateInInt >= 4) {
            layoutRateVisibility.set(View.GONE)
            layoutGoogleVisibility.set(View.VISIBLE)
            rateResult = Pair(true, true)
        } else {
            layoutRateVisibility.set(View.GONE)
            layoutFeedbackVisibility.set(View.VISIBLE)
            rateResult = Pair(true, false)
        }
        mEventBus.setData(AppPopupModel(currentRating.get()))
    }

    fun onRatingChanged(ratingBar: RatingBar, rating: Float, fromUser: Boolean) {
        currentRating.set(rating)
        buttonEnabled.set(rating > 0)
    }

    private fun sendRateRequest(rating: Long) = AppRateRequest(App.getContext(), rating).exec()

    fun closeButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLOSE()
        sendRateRequest(AppRateRequest.NO_RATE)
        iDialogCloser.closeIt()
    }

    //    сохраняем состояние
    override fun onSavedInstanceState(state: Bundle) {
        with(state) {
            putBoolean(IS_GOOD_RATE, rateResult.second)
            putBoolean(IS_ENABLED_BUTTON, buttonEnabled.get())
            putFloat(CURRENT_RATING, currentRating.get())
            putInt(IS_ENABLED_RATE_LAYOUT, layoutRateVisibility.get())
            putInt(IS_ENABLED_FEEDBACK_LAYOUT, layoutFeedbackVisibility.get())
            putInt(IS_ENABLED_GOOGLE_LAYOUT, layoutGoogleVisibility.get())
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        with(state) {
            rateResult = Pair(this.getBoolean(IS_ENABLED_BUTTON, buttonEnabled.get()), this.getBoolean(IS_GOOD_RATE, false))
            buttonEnabled.set(this.getBoolean(IS_ENABLED_BUTTON, buttonEnabled.get()))
            currentRating.set(this.getFloat(CURRENT_RATING, currentRating.get()))
            layoutRateVisibility.set(this.getInt(IS_ENABLED_RATE_LAYOUT, layoutRateVisibility.get()))
            layoutFeedbackVisibility.set(this.getInt(IS_ENABLED_FEEDBACK_LAYOUT, layoutFeedbackVisibility.get()))
            layoutGoogleVisibility.set(this.getInt(IS_ENABLED_GOOGLE_LAYOUT, layoutGoogleVisibility.get()))
        }
        super.onRestoreInstanceState(state)
    }

}