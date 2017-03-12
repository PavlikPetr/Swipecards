package com.topface.billing.ninja.dialogs

import android.app.AlertDialog
import android.content.Context
import com.topface.topface.R

/**
 * Constructs payment error dialogs
 * Created by m.bayutin on 06.03.17.
 */
class ErrorDialogFactory {
    private fun constructError(context: Context, dualButton: Boolean, receiver: IErrorDialogResultReceiver)
            = AlertDialog.Builder(context, R.style.NinjaTheme_Dialog).apply {
        setTitle(R.string.ninja_error_dialog_title)
        setIcon(R.drawable.ic_warning)
        setPositiveButton(R.string.ninja_error_dialog_button_retry) { dialog, witch ->
            receiver.onRetryClick()
        }
        if (dualButton) {
            setMessage(R.string.ninja_error_dialog_message_dual)
            setNegativeButton(R.string.ninja_error_dialog_button_change) { dialog, witch ->
                receiver.onSwitchClick()
            }
        } else {
            setMessage(R.string.ninja_error_dialog_message_single)
        }
        show()
    }

    /**
     * Use to construct dialog with buttons "retry" and "switch payment method"
     */
    fun constructDualButton(context: Context, receiver: IErrorDialogResultReceiver) = constructError(context, true, receiver)

    /**
     * Use to construct dialog with single button "retry"
     */
    fun constructSingleButton(context: Context, receiver: IErrorDialogResultReceiver) = constructError(context, false, receiver)

}