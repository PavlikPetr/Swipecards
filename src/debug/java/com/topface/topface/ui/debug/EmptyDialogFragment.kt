package com.topface.topface.ui.debug

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import com.topface.topface.utils.popups.PopupManager

/**
 * Created by tiberal on 29.08.16.
 */
class EmptyDialogFragment : DialogFragment() {

    //var unit: (() -> Unit) = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(toString())
                .setPositiveButton("OK") { dialog, which -> PopupManager.informManager("test") }
                .create()
    }
}