package com.topface.topface.ui.edit

import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.view.View
import com.topface.topface.utils.rx.RxFieldObservable

/**
 * Вьюмодель для переключателя с текстом и прогрессом
 * Created by ppavlik on 21.04.17.
 */
class EditSwitcherViewModel(private val textVisibilityDefault: Int = View.VISIBLE,
                            private val switcVisibilityDefault: Int = View.VISIBLE,
                            private val progressVisibilityDefault: Int = View.GONE,
                            val rootPaddingLeft: Int = 0,
                            val rootPaddingRight: Int = 0,
                            val rootPaddingTop: Int = 0,
                            val rootPaddingBottom: Int = 0,
                            isCheckedDefault: Boolean,
                            textDefault: String) {

    val textVisibility = ObservableInt(textVisibilityDefault)
    val switchVisibility = ObservableInt(switcVisibilityDefault)
    val progressVisibility = ObservableInt(progressVisibilityDefault)
    val text = ObservableField<String>(textDefault)
    val isChecked = RxFieldObservable(isCheckedDefault)
    val isEnabled = ObservableBoolean(true)

    fun setViewVisible(isVisible: Boolean) {
        textVisibility.set(if (isVisible) textVisibilityDefault else View.GONE)
        switchVisibility.set(if (isVisible) switcVisibilityDefault else View.GONE)
        progressVisibility.set(if (isVisible) progressVisibilityDefault else View.GONE)
    }

    fun setProgressVisible(isVisible: Boolean) {
        progressVisibility.set(if (isVisible) View.VISIBLE else View.GONE)
        textVisibility.set(if (isVisible) View.INVISIBLE else View.VISIBLE)
        switchVisibility.set(if (isVisible) View.INVISIBLE else View.VISIBLE)
    }

    fun onRootViewClick() {
        isChecked.set(!isChecked.get())
    }
}