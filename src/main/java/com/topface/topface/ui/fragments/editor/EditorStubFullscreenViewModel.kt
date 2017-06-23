package com.topface.topface.ui.fragments.editor

import android.content.SharedPreferences
import android.databinding.ObservableField
import com.topface.topface.App
import com.topface.topface.R
import com.topface.topface.utils.extensions.getString

/**
 * VM for editor fragment shows fullscreen counter, resets it and show fullscreen ads
 */
class EditorStubFullscreenViewModel(val showFullscreenAction:() -> Unit) {
    val text = ObservableField("0")
    val configChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        updateCounter()
    }

    private fun updateCounter() =
        text.set(R.string.editor_fullscreen_now.getString().format(
                App.getUserConfig().getFullscreenInterval<Int>().configFieldInfo.amount.toString()
        ))

    fun resetCounter() {
        App.getUserConfig().resetFullscreenInterval()
        updateCounter()
    }

    init {
        updateCounter()
    }
}