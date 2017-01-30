package com.topface.topface.utils.adapter_utils

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

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

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val offset = (mPaint.strokeWidth / 2).toInt()
        for (i in 1..parent.childCount - 1) {
            val view = parent.getChildAt(i)
            val params = view.layoutParams as RecyclerView.LayoutParams
            val position = params.viewAdapterPosition
            state?.let {
                if (position < state.itemCount) {
                    mPaint.alpha = (view.alpha * mAlpha).toInt()
                    val positionY = view.bottom.toFloat() + offset.toFloat() + view.translationY
                    c.drawLine(marginLeft + view.translationX,
                            positionY,
                            view.right + view.translationX,
                            positionY,
                            mPaint)
                }
            }
        }
    }
}
