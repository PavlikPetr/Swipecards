package com.topface.topface.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment

/**
 * Created by mbulgakov on 13.12.16.
 */
abstract class DialogFragmentWithSafeTransaction : DialogFragment() {

    var mTimeForTransaction = false

    override fun onResume() {
        mTimeForTransaction = true
        super.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        mTimeForTransaction = false
    }

}