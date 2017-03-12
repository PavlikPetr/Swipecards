package com.topface.topface.ui.bottom_sheet

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.ui.new_adapter.enhanced.ITypeProvider

/**
 * Базовый класс для создания BottomSheetBehavior
 * Created by petrp on 09.03.2017.
 */
abstract class BottomSheetBase<V : View> {
    abstract val mBottomSheetLayout: V
    abstract fun configurateBottomSheet(bottoSheet: BottomSheetBehavior<V>)

    private val mBottomSheetBehavior by lazy {
        BottomSheetBehavior.from(mBottomSheetLayout).apply {
            configurateBottomSheet(this)
        }
    }

    fun hide() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun show() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}