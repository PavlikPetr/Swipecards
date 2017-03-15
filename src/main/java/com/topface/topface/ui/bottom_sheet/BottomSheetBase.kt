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

    private var mShowPredivcate = { mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED }
    private var mHidePredivcate = { mBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN }

    init {
        initBottomSheet()
    }

    private fun initBottomSheet() = mBottomSheetBehavior.apply { configurateBottomSheet(this) }

    fun hide() {
        mHidePredivcate.invoke()
    }

    fun show() {
        mShowPredivcate.invoke()
    }

    fun BottomSheetBehavior<V>.showPredicate(predicate: () -> Unit) {
        mShowPredivcate = predicate
    }

    fun BottomSheetBehavior<V>.hidePredicate(predicate: () -> Unit) {
        mHidePredivcate = predicate
    }

}