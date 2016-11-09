package com.topface.topface.ui.fragments.form

import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView

/**
 * Created by tiberal on 31.10.16.
 */
class ExpandableListCallback(private val mAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>) :
        ListUpdateCallback {
    override fun onChanged(position: Int, count: Int, payload: Any?) = mAdapter.notifyItemRangeChanged(position, count)
    override fun onInserted(position: Int, count: Int) = mAdapter.notifyItemRangeInserted(position, count)
    override fun onRemoved(position: Int, count: Int) = mAdapter.notifyItemRangeRemoved(position, count)
    override fun onMoved(fromPosition: Int, toPosition: Int) = mAdapter.notifyItemMoved(fromPosition, toPosition)
}