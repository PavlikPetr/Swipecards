package com.topface.topface.ui.dialogs.trial_vip_experiment

import com.topface.statistics.android.Slices
import com.topface.statistics.android.StatisticsTracker
import com.topface.topface.App

/**
 * Trial vip popup statistics
 * Created by ppavlik on 22.11.16.
 */

object TrialVipExperimentStatistics {
    private const val TRIAL_VIP_POPUP_SHOW = "trial_vip_popup_show"
    private const val TRIAL_VIP_POPUP_CLOSE = "trial_vip_popup_close"
    private const val TRIAL_VIP_POPUP_PURCHASE_BUTTON_PRESSED = "trial_vip_popup_purchase_button_pressed"
    private const val TRIAL_VIP_POPUP_PURCHASE_COMPLETED = "trial_vip_popup_purchase_completed"
    private const val SHOW_COUNT = "int"

    private fun send(command: String, showCount: Int) =
            StatisticsTracker.getInstance().sendEvent(command, 1, with(Slices()) {
                putSlice(SHOW_COUNT, showCount.toString())
            })

    @JvmStatic @JvmOverloads fun sendPopupShow(showCount: Int = getShowCountFromConfig()) =
            send(TRIAL_VIP_POPUP_SHOW, showCount)

    @JvmStatic @JvmOverloads fun sendPopupClose(showCount: Int = getShowCountFromConfig()) =
            send(TRIAL_VIP_POPUP_CLOSE, showCount)

    @JvmStatic @JvmOverloads fun sendPurchaseButtonPressed(showCount: Int = getShowCountFromConfig()) =
            send(TRIAL_VIP_POPUP_PURCHASE_BUTTON_PRESSED, showCount)

    @JvmStatic @JvmOverloads fun sendPurchaseCompleted(showCount: Int = getShowCountFromConfig()) =
            send(TRIAL_VIP_POPUP_PURCHASE_COMPLETED, showCount)

    private fun getShowCountFromConfig() = App.getUserConfig().trialVipShowCounter
}