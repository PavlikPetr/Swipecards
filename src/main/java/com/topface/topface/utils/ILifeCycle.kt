package com.topface.topface.utils

import android.content.Intent
import android.os.Bundle

/**
 * Save/restore state
 * Created by tiberal on 21.10.16.
 */
interface ILifeCycle {
    fun onSavedInstanceState(state: Bundle) {}
    fun onRestoreInstanceState(state: Bundle) {}
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    fun onResume() {}
    fun onPause() {}
}