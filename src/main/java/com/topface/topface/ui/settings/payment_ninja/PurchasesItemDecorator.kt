package com.topface.topface.ui.settings.payment_ninja

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.R
import com.topface.topface.utils.extensions.getColor
import com.topface.topface.utils.extensions.getDimen

/**
 * Декоратор для создания разднлителей между итемами разных типов
 * Created by ppavlik on 13.03.17.
 */
class PurchasesItemDecorator : RecyclerView.ItemDecoration() {

    companion object {
        private const val NO_DIVIDER = 0
        private const val SOLID_DIVIDER_NO_TOP_DIVIDER = 1
        private const val SOLID_DIVIDER_WITH_TOP_DIVIDER = 2
        private const val PARTIAL_DIVIDER_NO_TOP_DIVIDER = 3
        private const val PARTIAL_DIVIDER_WITH_TOP_DIVIDER = 4
    }

    private val mDividerFirstPart by lazy {
        Paint().apply {
            color = R.color.ninja_payments_screen_item_background.getColor()
            strokeWidth = R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen()
        }
    }

    private val mDividerSecondPart by lazy {
        Paint().apply {
            color = R.color.ninja_payments_screen_item_divider.getColor()
            strokeWidth = R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen()
        }
    }

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        if (view != null && outRect != null) {
            val dividerType = getDividerType(parent, view)
            if (dividerType == PARTIAL_DIVIDER_WITH_TOP_DIVIDER || dividerType == SOLID_DIVIDER_WITH_TOP_DIVIDER) {
                outRect.top = R.dimen.payment_ninja_payments_different_type_items_margin.getDimen().toInt()
            }
            outRect.bottom = R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen().toInt()
        } else outRect?.setEmpty()
    }

    override fun onDraw(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        (0..(parent?.childCount ?: 1 - 1))
                .map { parent?.getChildAt(it) }
                .forEach { child ->
                    child?.let { child ->
                        val dividerType = getDividerType(parent, child)
                        if (dividerType != NO_DIVIDER) {
                            val startX = child.translationX
                            val bottomDividerStartY = child.bottom + child.translationY + R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen() / 2
                            val topDividerStartY = child.top - R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen() / 2
                            c?.let {
                                if (dividerType == PARTIAL_DIVIDER_WITH_TOP_DIVIDER || dividerType == SOLID_DIVIDER_WITH_TOP_DIVIDER) {
                                    // рисуем разделитель сверху
                                    it.drawLine(startX, topDividerStartY, child.right.toFloat(), topDividerStartY, mDividerSecondPart)

                                }
                                if (dividerType == PARTIAL_DIVIDER_NO_TOP_DIVIDER || dividerType == PARTIAL_DIVIDER_WITH_TOP_DIVIDER) {
                                    // рисуем белую линию
                                    it.drawLine(startX, bottomDividerStartY, R.dimen.payment_ninja_payments_item_text_padding_left.getDimen() + startX,
                                            bottomDividerStartY, mDividerFirstPart)
                                    // рисуем серую линию
                                    // в итоге итемы на белом фоне отделены серым divider с отступом слева
                                    it.drawLine(R.dimen.payment_ninja_payments_item_text_padding_left.getDimen() + startX, bottomDividerStartY,
                                            child.right.toFloat(), bottomDividerStartY, mDividerSecondPart)
                                }
                                if (dividerType == SOLID_DIVIDER_NO_TOP_DIVIDER || dividerType == SOLID_DIVIDER_WITH_TOP_DIVIDER) {
                                    // рисуем серую линию во весь итем
                                    it.drawLine(startX, bottomDividerStartY, child.right.toFloat(), bottomDividerStartY, mDividerSecondPart)
                                }
                            }
                        }
                    }
                }
    }

    private fun getDividerType(parent: RecyclerView?, view: View) =
            parent?.let {
                val position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
                if (position in 0..it.adapter.itemCount - 1) {
                    if (position == 0) {
                        if (position + 1 < it.adapter.itemCount &&
                                it.adapter.getItemViewType(position) == it.adapter.getItemViewType(position + 1))
                            PARTIAL_DIVIDER_NO_TOP_DIVIDER
                        else
                            SOLID_DIVIDER_NO_TOP_DIVIDER
                    } else {
                        if (position + 1 < it.adapter.itemCount &&
                                it.adapter.getItemViewType(position) == it.adapter.getItemViewType(position + 1)) {
                            if (it.adapter.getItemViewType(position) == it.adapter.getItemViewType(position - 1)) {
                                PARTIAL_DIVIDER_NO_TOP_DIVIDER
                            } else {
                                PARTIAL_DIVIDER_WITH_TOP_DIVIDER
                            }
                        } else {
                            if (it.adapter.getItemViewType(position) == it.adapter.getItemViewType(position - 1)) {
                                SOLID_DIVIDER_NO_TOP_DIVIDER
                            } else {
                                SOLID_DIVIDER_WITH_TOP_DIVIDER
                            }
                        }
                    }
                } else NO_DIVIDER
            } ?: NO_DIVIDER
}