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

    private val mMarginPaint by lazy {
        Paint().apply {
            color = R.color.ninja_payments_screen_background.getColor()
            strokeWidth = R.dimen.payment_ninja_payments_different_type_items_margin.getDimen()
        }
    }

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        val params = view?.layoutParams as? RecyclerView.LayoutParams
        val position = params?.viewAdapterPosition
        if (position != null && position < state?.itemCount ?: 0 && view != null) {
            isNeedDivider(parent, view)?.let {
                outRect?.set(0, 0, 0, (if (it)
                    R.dimen.payment_ninja_payments_same_type_items_divider_height
                else
                    R.dimen.payment_ninja_payments_different_type_items_margin).getDimen().toInt())
            }
        } else {
            outRect?.setEmpty()
        }
    }

    override fun onDraw(c: Canvas?, parent: RecyclerView?, state: RecyclerView.State?) {
        for (i in 0..(parent?.childCount ?: 1 - 1)) {
            val child = parent?.getChildAt(i)
            child?.let { child ->
                isNeedDivider(parent, child)?.let { isNeedDivider ->
                    val startX = child.translationX
                    val startY = child.bottom + child.translationY
                    c?.let {
                        if (isNeedDivider) {
                            it.drawLine(startX, startY, R.dimen.payment_ninja_payments_item_text_padding_left.getDimen() + startX,
                                    startY, mDividerFirstPart)
                            it.drawLine(R.dimen.payment_ninja_payments_item_text_padding_left.getDimen() + startX, startY+R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen(),
                                    child.right.toFloat(), startY+R.dimen.payment_ninja_payments_same_type_items_divider_height.getDimen(), mDividerSecondPart)
                        } else {
                            it.drawLine(startX, startY, child.right.toFloat(), startY+R.dimen.payment_ninja_payments_different_type_items_margin.getDimen(), mMarginPaint)
                        }
                    }
                }
            }

        }
    }

    private fun isNeedDivider(parent: RecyclerView?, view: View): Boolean? {
        if (parent != null) {
            val position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (position + 1 < parent.adapter.itemCount) {
                    return parent.adapter.getItemViewType(position) == parent.adapter.getItemViewType(position + 1)
                }
            }
        }
        return null
    }
}