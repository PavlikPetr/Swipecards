package com.topface.billing.ninja.dialogs

import android.support.v7.app.AlertDialog
import com.topface.topface.R

/**
 * Constructs payment error dialogs
 * Created by m.bayutin on 06.03.17.
 */
class ErrorDialogFactory {
    fun construct(builder: AlertDialog.Builder, singleButton: Boolean, receiver: IErrorDialogResultReceiver)
            = builder.apply {
        setTitle(R.string.ninja_error_dialog_title)
        setIcon(R.drawable.ic_warning)
        setPositiveButton(R.string.ninja_error_dialog_button_retry) { dialog, witch ->
            receiver.onRetryClick()
        }
        if (singleButton) {
            setMessage(R.string.ninja_error_dialog_message_single)
        } else {
            setMessage(R.string.ninja_error_dialog_message_dual)
            setNegativeButton(R.string.ninja_error_dialog_button_change) { dialog, witch ->
                receiver.onSwitchClick()
            }
        }
        show()
    }
}