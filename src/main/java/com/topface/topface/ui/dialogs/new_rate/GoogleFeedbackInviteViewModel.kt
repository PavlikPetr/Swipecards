package com.topface.topface.ui.dialogs.new_rate

import com.topface.statistics.generated.RatePopupStatisticsGeneratedStatistics
import com.topface.topface.ui.fragments.dating.IDialogCloser

class GoogleFeedbackInviteViewModel(private val mDialogCloser: IDialogCloser) {
    fun okButtonClick(){
        // todo переход в ГУГЛПЛЭЙ
    }
    fun closeButtonClick(){
        RatePopupStatisticsGeneratedStatistics.sendNow_RATE_POPUP_CLICK_BUTTON_CLOSE()
        mDialogCloser.closeIt()
    }
}