package com.topface.topface.utils.popups.start_actions

import android.support.v4.app.FragmentActivity
import com.topface.topface.App
import com.topface.topface.ui.dialogs.RateAppDialog
import com.topface.topface.utils.AppUtils
import com.topface.topface.utils.controllers.startactions.IStartAction
import com.topface.topface.utils.popups.PopupManager

/**
 * Стартуем попап оценки
 * Created by tiberal on 31.08.16.
 */
class RatePopupStartAction(private val mActivity: FragmentActivity, private val mPriority: Int, private val mFrom: String) : IStartAction {

    override fun callInBackground() {
    }

    override fun callOnUi() = showRatePopup()


    override fun isApplicable(): Boolean {
        val options = App.get().options
        return App.isOnline() && RateAppDialog.isApplicable(options.ratePopupTimeout, options.ratePopupEnabled) &&
                !AppUtils.isOldVersion(options.maxVersion)
    }

    override fun getPriority() = mPriority

    override fun getActionName(): String? = this.javaClass.simpleName

    private fun showRatePopup() {
        val rateAppDialog = RateAppDialog()
        rateAppDialog.show(mActivity.supportFragmentManager, RateAppDialog.TAG)
        rateAppDialog.setOnCancelListener {
            PopupManager.informManager(mFrom)
        }
        rateAppDialog.setOnDismissListener {
            PopupManager.informManager(mFrom)
        }
    }
}