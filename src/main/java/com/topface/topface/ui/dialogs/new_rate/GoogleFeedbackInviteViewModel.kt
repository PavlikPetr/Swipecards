package com.topface.topface.ui.dialogs.new_rate

import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.ui.dialogs.IDialogCloser
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.Utils

class GoogleFeedbackInviteViewModel(private var mDialogCloser: IDialogCloser?, private var mActivityDelegate: IActivityDelegate?) {

    companion object {
        const val GPLAY_ACTIVITY = 9999
    }

    fun okButtonClick() {
        Utils.goToMarket(mActivityDelegate, GPLAY_ACTIVITY)
        mDialogCloser?.closeIt()
    }

    fun closeButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        mDialogCloser?.closeIt()
    }

    fun release() {
        mActivityDelegate = null
        mDialogCloser = null
    }
}