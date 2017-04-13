package com.topface.topface.ui.fragments.feed.feed_base

import android.view.View
import android.widget.Toast
import com.topface.topface.R

class MultiselectionController<T>(val mSelectionListener: IMultiSelectionListener) {

    val mSelected = mutableListOf<T>()
    val mSelectedItemsPositions = mutableListOf<Int>()
    private var mSelectionLimit = 0

    private companion object {
        val MAX_SELECTED_ITEMS_COUNT = 99
    }

    fun handleSelected(item: T, view: View, itemPosition: Int) {
        val isContain = mSelected.contains(item)
        if (isOverlimit() && !isContain) {
            Toast.makeText(view.context.applicationContext, R.string.maximum_number_of_users, Toast.LENGTH_LONG).show()
        } else {
            if (mSelected.contains(item)) {
                mSelectedItemsPositions.remove(itemPosition)
                view.isSelected = false
                removeSelection(item)
            } else {
                mSelectedItemsPositions.add(itemPosition)
                view.isSelected = true
                addSelection(item)
            }
            mSelectionListener.onSelected(mSelected.count())
        }
    }

    fun startMultiSelection(selectionLimit: Int = MAX_SELECTED_ITEMS_COUNT) {
        if (!mSelected.isEmpty()) {
            mSelected.clear()
        }
        mSelectionLimit = selectionLimit
    }

    private fun addSelection(item: T) {
        if (!isOverlimit()) {
            mSelected.add(item)
        }
    }

    private fun isOverlimit() = selectedCount() + 1 > mSelectionLimit

    private fun removeSelection(item: T) = mSelected.remove(item)

    fun selectedCount() = mSelected.count()

    fun stopMultiSelection() {
        if (!mSelected.isEmpty()) {
            mSelected.clear()
        }
    }

    interface IMultiSelectionListener {
        fun onSelected(size: Int)
    }
}