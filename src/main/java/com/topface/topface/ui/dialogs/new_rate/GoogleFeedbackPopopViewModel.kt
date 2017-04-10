package com.topface.topface.ui.dialogs.new_rate

import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.widget.Toast
import com.topface.framework.utils.BackgroundThread
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.requests.IApiResponse
import com.topface.topface.requests.SendFeedbackRequest
import com.topface.topface.requests.handlers.ErrorCodes
import com.topface.topface.requests.handlers.SimpleApiHandler
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.settings.FeedbackMessageFragment
import com.topface.topface.utils.ClientUtils
import com.topface.topface.utils.Utils

class GoogleFeedbackPopopViewModel(private val mDialogCloseable: IDialogCloser, private val mRating: Float) {

    val error = ObservableField<String>()
    val text = ObservableField<String>()
    val buttonEnabled = ObservableBoolean(false)

    init {
        text.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable?, p1: Int) = (observable as? ObservableField<*>)?.let {
                (it.get() as? String)?.let {
                    buttonEnabled.set(it.isNotEmpty())
                }
            } ?: Unit
        })
    }

    fun okButtonClick() {
            val handler = object : SimpleApiHandler() {
                override fun fail(codeError: Int, response: IApiResponse) {
                    if (response.isCodeEqual(ErrorCodes.TOO_MANY_MESSAGES)) {
                        Utils.showToastNotification(R.string.ban_flood_detected, Toast.LENGTH_SHORT)
                    } else {
                        Utils.showErrorMessage()
                    }
                }
            }
            object : BackgroundThread() {
                override fun execute() {

                    val report = FeedbackMessageFragment.Report(
                            FeedbackMessageFragment.FeedbackType.LOW_RATE_MESSAGE
                    )
                    with(report) {
                        subject = String.format(FeedbackMessageFragment.FeedbackType.LOW_RATE_MESSAGE.title, mRating.toInt())
                        body = text.get()
                        email = ClientUtils.getSocialAccountEmail()
                        FeedbackMessageFragment.fillVersion(App.getContext(), this)
                        SendFeedbackRequest(App.getContext(), this).callback(handler).exec()
                    }
                }
            }
            mDialogCloseable.closeIt()
    }

    fun closeButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLOSE()
        mDialogCloseable.closeIt()
    }
}