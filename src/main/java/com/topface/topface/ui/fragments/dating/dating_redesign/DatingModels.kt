package com.topface.topface.ui.fragments.dating.dating_redesign

import android.os.Build
import org.json.JSONObject

data class TargetSettings @JvmOverloads constructor(var isEnabled: Boolean = false) {

    fun parse(response: JSONObject){
        isEnabled = response.optBoolean("datingRedesignEnabled")
    }

    val isKitKatWithNoTranslucent by lazy {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                && !isEnabled
    }
}