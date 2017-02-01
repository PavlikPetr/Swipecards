package com.topface.topface.utils.adapter_utils

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.R
import com.topface.topface.utils.extensions.getDimen

/**
 * Methods for generating common item decorations for RecyclerView's items
 * Created by m.bayutin on 31.01.17.
 */

/**
 * Creates item decorator with 8dp margin on all sides
 * But first item will have 16dp margin at left
 */
fun create16Left8TotalMargin() = object : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        val itemPosition = (view?.layoutParams as? RecyclerView.LayoutParams)?.viewAdapterPosition ?: 0
        outRect?.apply {
            set(
                    // нулевой итем имеет отступ отличный от остальных
                    if (itemPosition == 0) {
                        R.dimen.photoblog_add_button_margin_left
                    } else {
                        R.dimen.photoblog_item_margin_left
                    }.getDimen().toInt(),
                    R.dimen.photoblog_item_margin_top.getDimen().toInt(),
                    R.dimen.photoblog_item_margin_right.getDimen().toInt(),
                    R.dimen.photoblog_item_margin_bottom.getDimen().toInt())
        }
    }
}
