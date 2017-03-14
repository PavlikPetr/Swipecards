package com.topface.topface.ui.bottom_sheet

import android.support.design.widget.BottomSheetBehavior
import android.view.View

/**
 * Базовый класс для создания BottomSheetBehavior
 * Created by petrp on 09.03.2017.
 */
abstract class BottomSheetBase<V : View>(mBottomSheetView: V) {
    abstract fun configurateBottomSheet(bottoSheet: BottomSheetBehavior<V>)

    private val mBottomSheetBehavior by lazy {
        BottomSheetBehavior.from(mBottomSheetView)
    }

    init {
        initBottomSheet()
    }

    private fun initBottomSheet() = mBottomSheetBehavior.apply { configurateBottomSheet(this) }

    fun hide() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun show() {
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}