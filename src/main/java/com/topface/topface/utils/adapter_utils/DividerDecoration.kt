package com.topface.topface.utils.adapter_utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.ui.fragments.feed.dialogs.dialogs_redesign.AppDayStubItem
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter

/**
 * Декоратор для отрисовки дивайдера!! Сделан не просто так, а для того, чтобы дивайдеры при удалении итема убирались.
 */
class DividerDecoration(dividerColor: Int, strokeWidth: Float, val marginLeft: Float) : RecyclerView.ItemDecoration() {

    private val mPaint: Paint
    private val mAlpha: Int

    init {
        mPaint = Paint()
        mPaint.color = dividerColor
        mPaint.strokeWidth = strokeWidth
        mAlpha = mPaint.alpha
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val params = view.layoutParams as RecyclerView.LayoutParams
        val position = params.viewAdapterPosition
        if (position < state.itemCount) {
            outRect.set(0, 0, 0, mPaint.strokeWidth.toInt())
        } else {
            outRect.setEmpty()
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val offset = (mPaint.strokeWidth / 2).toInt()
        val typeWithoutDivider = (parent.adapter as CompositeAdapter).typeProvider.getType(AppDayStubItem::class.java)
        for (i in 0..parent.childCount - 1) {
            val childAt = parent.getChildAt(i)
            val params = childAt.layoutParams as RecyclerView.LayoutParams
            val position = params.viewAdapterPosition
            // эта проверка нужна дабы не профакапать момент, когда вернется -1 в position.
            if (position != RecyclerView.NO_POSITION) {
                val currentType = parent.adapter.getItemViewType(position)
                val nextType = if (state.itemCount - position > 2) parent.adapter.getItemViewType(position + 1) else currentType
                if (position < state.getItemCount()) {
                    if (position != 0 && nextType != typeWithoutDivider && currentType != typeWithoutDivider) {
                        mPaint.alpha = (childAt.alpha * mAlpha).toInt()
                        val positionY = childAt.bottom.toFloat() + offset.toFloat() + childAt.translationY
                        c.drawLine(marginLeft,
                                positionY,
                                childAt.right + childAt.translationX,
                                positionY,
                                mPaint)
                    }
                }
            }
        }
    }
}
