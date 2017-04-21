package com.topface.topface.ui.dialogs.new_rate

import android.databinding.Observable
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.os.Bundle
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.settings.FeedbackMessageFragment
import com.topface.topface.utils.ClientUtils
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils
import com.topface.topface.utils.rx.shortSubscription
import rx.subscriptions.CompositeSubscription


class GoogleFeedbackPopopViewModel(private var mDialogCloseable: IDialogCloser?, private val mApi: FeedApi) : ILifeCycle {

    companion object {
        const val FEEDBACK_POPUP_TEXT = "feedback_popup_text"
        const val FEEDBACK_POPUP_RATE_VALUE = "feedback_popup_rate_value"
        const val FEEDBACK_POPUP_BUTTON_ENABLED = "feedback_popup_button_enabled"
    }

    val text = ObservableField<String>()
    val buttonEnabled = ObservableBoolean(false)
    var rateValue = 0F
    private val mEventBus by lazy {
        App.getAppComponent().eventBus()
    }

    private var mGoogleFeedbackSubscription = CompositeSubscription()
    private val mTextChangeListener = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(observable: Observable?, p1: Int) = (observable as? ObservableField<*>)?.let {
            (it.get() as? String)?.let {
                buttonEnabled.set(it.isNotEmpty())
            }
        } ?: Unit
    }

    init {
        text.addOnPropertyChangedCallback(mTextChangeListener)
        mGoogleFeedbackSubscription.add(mEventBus.getObservable(AppPopupModel::class.java).subscribe(shortSubscription {
            rateValue = it.rateValue
        }))
    }

    fun okButtonClick() {
        with(FeedbackMessageFragment.Report(
                FeedbackMessageFragment.FeedbackType.LOW_RATE_MESSAGE)) {
            subject = String.format(FeedbackMessageFragment.FeedbackType.LOW_RATE_MESSAGE.title, rateValue.toInt())
            body = text.get()
            email = ClientUtils.getSocialAccountEmail()
            FeedbackMessageFragment.fillVersion(App.getContext(), this)
            mGoogleFeedbackSubscription.add(mApi.callSendFeedbackRequest(this).subscribe())
        }
        mDialogCloseable?.closeIt()
    }

    fun closeButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        mDialogCloseable?.closeIt()
    }

    override fun onSavedInstanceState(state: Bundle) {
        super.onSavedInstanceState(state)
        with(state) {
            putFloat(FEEDBACK_POPUP_RATE_VALUE, rateValue)
            putBoolean(FEEDBACK_POPUP_BUTTON_ENABLED, buttonEnabled.get())
            putString(FEEDBACK_POPUP_TEXT, text.get())
        }
    }

    override fun onRestoreInstanceState(state: Bundle) {
        super.onRestoreInstanceState(state)
        with(state) {
            rateValue = this.getFloat(FEEDBACK_POPUP_RATE_VALUE, 0F)
            buttonEnabled.set(this.getBoolean(FEEDBACK_POPUP_BUTTON_ENABLED, false))
            text.set(this.getString(FEEDBACK_POPUP_TEXT, Utils.EMPTY))
        }
    }

    fun release() {
        text.removeOnPropertyChangedCallback(mTextChangeListener)
        mDialogCloseable = null
        mGoogleFeedbackSubscription.clear()
    }
}