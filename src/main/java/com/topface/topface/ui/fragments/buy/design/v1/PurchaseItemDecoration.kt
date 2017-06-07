package com.topface.topface.ui.fragments.buy.design.v1

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import com.topface.topface.R
import com.topface.topface.ui.new_adapter.enhanced.CompositeAdapter
import com.topface.topface.utils.extensions.getDimen

/**
 * Расставляет отступы как для корневого recycler так и для списка монеток
 * т.е. использует в обоих местах
 */
class PurchaseItemDecoration: RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        if (view != null && parent != null) {
            val position = parent.getChildAdapterPosition(view)
            if (position != RecyclerView.NO_POSITION) {
                (parent.adapter as? CompositeAdapter)?.data?.let {
                    when(it[position]) {
                        is LikeItem, is TestPurchaseSwitchItem -> {
                            outRect?.set(
                                    R.dimen.base_indent_double.getDimen().toInt(),
                                    R.dimen.base_indent_double.getDimen().toInt(),
                                    R.dimen.base_indent_double.getDimen().toInt(),
                                    0
                            )
                        }
                        else -> {
                            // list item with coins and all coins have another margins
                            outRect?.set(
                                    R.dimen.base_indent.getDimen().toInt(),
                                    R.dimen.base_indent.getDimen().toInt(),
                                    R.dimen.base_indent.getDimen().toInt(),
                                    R.dimen.base_indent.getDimen().toInt()
                            )
                        }
                    }
                }
            }
        }
    }
}