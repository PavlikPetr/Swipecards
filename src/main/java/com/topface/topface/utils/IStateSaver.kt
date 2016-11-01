package com.topface.topface.utils

import android.os.Bundle

/**
 * Save/restore state
 * Created by tiberal on 21.10.16.
 */
interface IStateSaver {
    fun onSavedInstanceState(state: Bundle)
    fun onRestoreInstanceState(state: Bundle)
}