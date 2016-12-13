package com.topface.topface.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment

/**
 * Created by mbulgakov on 13.12.16.
 */
open class DialogFragmentWithSafeTransaction : DialogFragment() {
    var mTimeForTransaction = true


    override fun onSaveInstanceState(outState: Bundle?) {
        mTimeForTransaction = false
    }
}