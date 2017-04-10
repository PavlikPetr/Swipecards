package com.topface.topface.ui.dialogs.new_rate

import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.topface.framework.utils.BackgroundThread
import com.topface.framework.utils.Debug
import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.databinding.RateAppLayoutBinding
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.ui.fragments.feed.feed_api.DeleteFeedRequestFactory
import com.topface.topface.ui.fragments.feed.feed_api.FeedApi
import com.topface.topface.ui.fragments.feed.feed_base.FeedNavigator
import com.topface.topface.utils.http.IRequestClient
import org.jetbrains.anko.layoutInflater

class RateAppFragment(private val mFeedNavigator: FeedNavigator) : DialogFragment(), IDialogCloser {

    companion object {
        const val TAG = "RateAppFragment"
        const val RATING_POPUP = "RATING_POPUP"
        const val RATING_POPUP_VALUE = "RATING_POPUP_VALUE"
        const val RATING = "val"

        /**
         * Note: method has operations with disk(SharedPreferences)
         *
         * @return whether the rate popup is applicable or not
         */
        fun isApplicable (notNowTimeout:Long, badRateTimeout: Long, ratePopupEnabled: Boolean): Boolean {

            val preferences = App.getContext().getSharedPreferences(
                    App.PREFERENCES_TAG_SHARED,
                    Context.MODE_PRIVATE
            )
            val dateStart = preferences.getLong(RATING_POPUP, -1)

            val rateTimeout = if (preferences.getBoolean(RATING_POPUP_VALUE, false))badRateTimeout else notNowTimeout
            // first time do not show rate popup
            if (dateStart.toInt() == -1) {
                saveRatingPopupStatus(System.currentTimeMillis())
                return false
            }

            return dateStart.toInt() != 0 && System.currentTimeMillis() - dateStart > rateTimeout && ratePopupEnabled
        }

        /**
         * Saves status of rate popup show
         * @param value timestamp of last show; 0 - to stop showing ;
         * @param badRate need for choose timeOut
         */
        fun saveRatingPopupStatus(value: Long, isBadRate: Boolean = false) {
            object : BackgroundThread() {
                override fun execute() {
                    val editor = App.getContext().getSharedPreferences(
                            App.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE
                    ).edit()
                    editor.putLong(RATING_POPUP, value)
                    editor.putBoolean(RATING_POPUP_VALUE, isBadRate)
                    editor.apply()
                }
            }
        }
    }

    private val mViewModel by lazy { RateAppViewModel(mFeedNavigator, this@RateAppFragment, mApi) }

    private val mBinding by lazy {
        DataBindingUtil.inflate<RateAppLayoutBinding>(context.layoutInflater, R.layout.rate_app_layout, null, false)
    }

    private val mApi by lazy {
        FeedApi(context, activity as IRequestClient, DeleteFeedRequestFactory(context))
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = with(mBinding) {
        viewModel = mViewModel
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawableResource(R.drawable.rate_popup_corners_background)
        root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_SHOW()
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)
        saveRatingPopupStatus(0)
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLOSE()
    }

    override fun closeIt() = dialog.dismiss()

}