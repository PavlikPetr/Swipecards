package com.topface.topface.ui

import android.os.Bundle
import android.support.v4.app.DialogFragment

/**
 * Created by mbulgakov on 13.12.16.
 */
abstract class DialogFragmentWithSafeTransaction : DialogFragment() {

    var mTimeForTransaction = false

    override fun onCreate(savedInstanceState: Bundle?) {
        mTimeForTransaction = true
        super.onCreate(savedInstanceState)
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        mTimeForTransaction = false
    }

}