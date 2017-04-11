package com.topface.topface.ui.dialogs.new_rate

import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.ui.fragments.dating.IDialogCloser
import com.topface.topface.utils.IActivityDelegate
import com.topface.topface.utils.ILifeCycle
import com.topface.topface.utils.Utils

class GoogleFeedbackInviteViewModel(private val mDialogCloser: IDialogCloser, private val mActivityDelegate: IActivityDelegate) : ILifeCycle {

    companion object {
        const val GPLAY_ACTIVITY = 9999
    }

    fun okButtonClick() {
        Utils.goToMarket(mActivityDelegate, GPLAY_ACTIVITY)
        mDialogCloser.closeIt()
    }

    fun closeButtonClick() {
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLOSE()
        mDialogCloser.closeIt()
    }
}