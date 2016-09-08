package com.topface.topface.utils.popups.start_actions

import android.support.v4.app.FragmentActivity
import com.topface.topface.App
import com.topface.topface.ui.IDialogListener
import com.topface.topface.ui.dialogs.OldVersionDialog
import com.topface.topface.utils.AppUtils
import com.topface.topface.utils.Utils
import com.topface.topface.utils.controllers.startactions.IStartAction
import com.topface.topface.utils.popups.PopupManager

/**
 * Показываем попап старой версии
 * Created by tiberal on 31.08.16.
 */
class OldVersionStartAction(private val mActivity: FragmentActivity, private val mPriority: Int, private val mFrom: String) : IStartAction {

    override fun callInBackground() {
    }

    override fun callOnUi() {
        val oldVersionDialog = OldVersionDialog.newInstance(true)
        oldVersionDialog.setDialogInterface(object : IDialogListener {
            override fun onPositiveButtonClick() = Utils.goToMarket(mActivity, null)
            override fun onNegativeButtonClick() = oldVersionDialog.dialog.cancel()
            override fun onDismissListener() = PopupManager.informManager(mFrom)
        })
        oldVersionDialog.show(mActivity.supportFragmentManager, OldVersionDialog::class.java.name)
    }

    override fun isApplicable() = AppUtils.isOldVersion(App.get().options.maxVersion)

    override fun getPriority() = mPriority

    override fun getActionName(): String? = this.javaClass.simpleName

}