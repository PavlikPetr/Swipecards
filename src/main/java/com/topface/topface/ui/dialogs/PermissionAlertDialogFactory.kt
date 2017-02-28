package com.topface.topface.ui.dialogs

import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.utils.extensions.showAppSettings

/**
 * Creates native alert-dialogs for permissions block/allow results
 * Created by m.bayutin on 28.02.17.
 */
class PermissionAlertDialogFactory {
    private fun constructAlert(context: Context, @StringRes positiveButtonTextResId: Int,
                               @StringRes negativeButtonResId: Int, @StringRes messageResId: Int,
                               positiveAction: () -> Unit) = AlertDialog.Builder(context)
                .setPositiveButton(positiveButtonTextResId) { dialog, which ->
                    positiveAction()
                }
                .setNegativeButton(negativeButtonResId) { dialog, which ->
                    //do nothing, but we need to show this button
                }
                .setMessage(messageResId)
                .setCancelable(false)
                .create()
                .show()

    fun constructNeverAskAgain(context: Context) = constructAlert(context,
            R.string.permission_alert_button_allow,
            R.string.permission_alert_button_disallow,
            R.string.permission_alert_button_message
            ) { App.getContext().showAppSettings() }
}