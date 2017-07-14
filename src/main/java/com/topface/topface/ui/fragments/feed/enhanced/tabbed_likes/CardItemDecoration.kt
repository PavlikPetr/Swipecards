package com.topface.topface.ui.fragments.feed.enhanced.tabbed_likes

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.topface.topface.R
import com.topface.topface.utils.extensions.getDimen

/**
 * Расставляет отступы как для корневого recycler так и для списка монеток
 * т.е. использует в обоих местах
 */
class CardItemDecoration : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        view?.let {
            getRect(parent, it)?.let {
                outRect?.set(it)
            }
        }
    }

    private fun getRect(parent: RecyclerView?, view: View): Rect? =
            parent?.let {
                val position = (view.layoutParams as RecyclerView.LayoutParams).viewAdapterPosition
                val lm = it.layoutManager as StaggeredGridLayoutManager
                val left = if ((position + lm.spanCount) % lm.spanCount == 0)
                    R.dimen.feed_card_padding_left.getDimen().toInt()
                else
                    R.dimen.feed_card_padding_left.getDimen().toInt() / 2
                val top = if (position < lm.spanCount)
                    R.dimen.feed_card_padding_top.getDimen().toInt()
                else
                    R.dimen.feed_card_padding_top.getDimen().toInt() / 2
                val right = if ((position + 1) % lm.spanCount == 0)
                    R.dimen.feed_card_padding_right.getDimen().toInt()
                else
                    R.dimen.feed_card_padding_right.getDimen().toInt() / 2
//                // TODO НИЖЕ ГОВНО ПОПРАВЬ ПАРЯ
//                // сейчас сознательно уменьшен отступ снизу для карточек в конце списка, т.к
//                // если его оставить, то будем иметь проблему со смещением всего столбца на дозагрузке
                val bottom = R.dimen.feed_card_padding_bottom.getDimen().toInt() / 2
//            val bottom = if (position >= it.adapter.itemCount - lm.spanCount)
//                R.dimen.feed_card_padding_bottom.getDimen().toInt()
//            else
//                R.dimen.feed_card_padding_bottom.getDimen().toInt() / 2
                Rect(left, top, right, bottom)
            }
}