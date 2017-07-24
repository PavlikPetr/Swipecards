package com.topface.topface.ui.new_adapter.enhanced

import android.os.Bundle
import rx.Observable

interface IAdapter {
    fun releaseComponents()

    fun notifyDataSetChanged()

    fun notifyItemChanged(position: Int)

    fun notifyItemChanged(position: Int, payload: Any)

    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int)

    fun notifyItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any)

    fun notifyItemInserted(position: Int)

    fun notifyItemMoved(fromPosition: Int, toPosition: Int)

    fun notifyItemRangeInserted(positionStart: Int, itemCount: Int)

    fun notifyItemRemoved(position: Int)

    fun notifyItemRangeRemoved(positionStart: Int, itemCount: Int)

    var data: MutableList<Any>

    val updateObservable: Observable<Bundle>
}