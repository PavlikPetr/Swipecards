package com.topface.topface.ui.dialogs.new_rate

import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.data.RatePopupNewVersion
import com.topface.topface.databinding.RateAppLayoutBinding
import com.topface.topface.ui.dialogs.RoundedPopupFragment
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.http.IRequestClient
import com.topface.topface.utils.registerLifeCycleDelegate
import com.topface.topface.utils.unregisterLifeCycleDelegate
import org.jetbrains.anko.layoutInflater

class RateAppFragment : RoundedPopupFragment(), IDialogCloser {

    companion object {
        const val TAG = "RateAppFragment"
        const val RATING = "val"

        fun isApplicable(ratePopupOptions: RatePopupNewVersion): Boolean {

            val userConfig = App.getUserConfig()
            val dateStart = userConfig.ratingPopup
            val rateTimeout = if (userConfig.ratingPopupValue) ratePopupOptions.badRateTimeout else ratePopupOptions.notNowTimeout
            // first time do not show rate popup
            if (dateStart.toInt() == -1) {
                saveRatingPopupStatus(System.currentTimeMillis(), false)
                return false
            }
            return dateStart.toInt() != 0 && System.currentTimeMillis() - dateStart > rateTimeout && ratePopupOptions.enabled
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
        GoogleFeedbackPopopViewModel(this, mApi)
    }

    private val mGoogleIntiteViewModel by lazy {
        GoogleFeedbackInviteViewModel(this, activity as IActivityDelegate)
    }

    private val mApi by lazy { FeedApi(context, activity as IRequestClient) }

    private val mBinding by lazy {
        DataBindingUtil.inflate<RateAppLayoutBinding>(context.layoutInflater, R.layout.rate_app_layout, null, false)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View = with(mBinding) {
        viewModel = mViewModel
        feedbackViewModel = mFeedbackViewModel
        googleIntiteViewModel = mGoogleIntiteViewModel
        super.onCreateView(inflater, container, savedInstanceState)
        root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_SHOW()
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
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLOSE()
    }

    override fun onDetach() {
        super.onDetach()
        activity.unregisterLifeCycleDelegate(this)
    }

    override fun closeIt() = dialog.dismiss()

}