package com.topface.topface.ui.dialogs.new_rate

import android.app.DialogFragment
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.statistics.android.Slices
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.RatePopupNewVersion
import com.topface.topface.databinding.RateAppLayoutBinding
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.DateUtils
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.http.IRequestClient
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate

class RateAppFragment : DialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "RateAppFragment"
        const val RATING = "val"

        fun isApplicable(ratePopupOptions: RatePopupNewVersion): Boolean {

            val userConfig = App.getUserConfig()
            val dateStart = userConfig.ratingPopup
            var rateTimeout = if (userConfig.ratingPopupValue) ratePopupOptions.badRateTimeout.toLong() else ratePopupOptions.notNowTimeout.toLong()
            // first time do not show rate popup
            if (dateStart.toInt() == -1) {
                saveRatingPopupStatus(System.currentTimeMillis(), false)
                return false
            }
            return dateStart.toInt() != 0 && System.currentTimeMillis() - dateStart > rateTimeout * DateUtils.MINUTE_IN_MILLISECONDS && ratePopupOptions.enabled
        }

        /**
         * Saves status of rate popup show
         * @param value timestamp of last show; 0 - to stop showing ;
         * @param badRate need for choose timeOut
         */
        fun saveRatingPopupStatus(value: Long, isBadRate: Boolean) {
            val userConfig = App.getUserConfig()
            with(userConfig) {
                ratingPopup = value
                ratingPopupValue = isBadRate
                saveConfig()
            }
        }
    }

    private val mViewModel by lazy {
        RateAppViewModel(this).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    private val mFeedbackViewModel by lazy {
        GoogleFeedbackPopopViewModel(this, mApi).apply {
            activity.registerLifeCycleDelegate(this)
        }
    }

    private val mGoogleIntiteViewModel by lazy {
        GoogleFeedbackInviteViewModel(this, activity as IActivityDelegate)
    }

    private val mApi by lazy { FeedApi(activity.applicationContext, activity as IRequestClient) }

    private val mBinding by lazy {
        DataBindingUtil.inflate<RateAppLayoutBinding>(activity.layoutInflater, R.layout.rate_app_layout, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View = with(mBinding) {
        viewModel = mViewModel
        feedbackViewModel = mFeedbackViewModel
        googleIntiteViewModel = mGoogleIntiteViewModel
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(R.drawable.rate_popup_corners_background)
        root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_SHOW(Slices().apply {
            putSlice(RatePopupStatistics.DIALOG_TYPE, RatePopupStatistics.NEW_DIALOG)
        })
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        val rateResult = mViewModel.rateResult
        if (rateResult.first) {
            if (rateResult.second) {
                // stop showing this popup
                saveRatingPopupStatus(0, false)
            } else {
                // была нажата плохая оценка, запомним это
                saveRatingPopupStatus(System.currentTimeMillis(), true)
            }
        } else {
            // было выбрано "не сейчас"/"закрыть" запомним это
            saveRatingPopupStatus(System.currentTimeMillis(), false)
        }
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLOSE(Slices().apply {
            putSlice(RatePopupStatistics.DIALOG_TYPE, RatePopupStatistics.NEW_DIALOG)
        })
    }

    override fun onDetach() {
        super.onDetach()
        activity.unregisterLifeCycleDelegate(this)
        mFeedbackViewModel.release()
        mGoogleIntiteViewModel.release()
        mViewModel.release()
    }

    override fun closeIt() = dialog.cancel()

}