package com.topface.topface.ui.dialogs

import android.content.Context
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.utils.extensions.showAppSettings

/**
 * Creates native alert-dialogs
 * Created by m.bayutin on 28.02.17.
 */
class AlertDialogFactory {
    private fun constructAlert(context: Context, @StringRes positiveButtonTextResId: Int,
                               @StringRes negativeButtonResId: Int, @StringRes messageResId: Int,
                               positiveAction: () -> Unit, negativeAction: () -> Unit = {}) = AlertDialog.Builder(context)
            .setPositiveButton(positiveButtonTextResId) { dialog, which ->
                positiveAction()
            }
            .setNegativeButton(negativeButtonResId) { dialog, which ->
                negativeAction()
            }
            .setMessage(messageResId)
            .setCancelable(false)
            .show()

    fun constructNeverAskAgain(context: Context) = constructAlert(context,
            R.string.permission_alert_button_allow,
            R.string.permission_alert_button_disallow,
            R.string.permission_alert_button_message,
            positiveAction = { App.getContext().showAppSettings() }
    )

    fun constructDeleteCard(context: Context, positiveAction: () -> Unit) = constructAlert(context,
            R.string.general_yes,
            R.string.general_no,
            R.string.ninja_ask_to_delete_card_message,
            positiveAction = positiveAction
    )
}