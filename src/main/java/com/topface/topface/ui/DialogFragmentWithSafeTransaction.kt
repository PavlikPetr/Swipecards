package com.topface.topface.ui

import android.support.v4.app.DialogFragment

/**
 * Created by mbulgakov on 13.12.16.
 */
open class DialogFragmentWithSafeTransaction : DialogFragment() {
    open val mTimeForTransaction: Boolean? = null
}