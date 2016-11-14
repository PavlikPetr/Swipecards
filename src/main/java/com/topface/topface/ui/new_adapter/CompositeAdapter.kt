package com.topface.topface.ui.new_adapter

import android.databinding.ViewDataBinding
import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Адаптер позволяющий менять типы вьюх на лету, стоит только захотеть
 * Created by tiberal on 30.10.16.
 *
 * @param T тип данных адаптера
 */
class CompositeAdapter<T : IType>() : RecyclerView.Adapter<CompositeAdapter.RecyclerViewHolder<ViewDataBinding>>() {


    //todo заменить на какойнить интерфейс, чтоб можно было работать с любыми коллекциями
    val data = ExpandableArrayList<T>(ExpandableListCallback(this))
    private val mDelegates = SparseArrayCompat<ExpandableItemDelegate<*, *>>()

    fun addAdapterItemDelegate(type: Int, delegate: ExpandableItemDelegate<*, *>) = with(type) {
        if (mDelegates[this] == null) {
            delegate.expandableList = data
            mDelegates.put(this, delegate)
        } else {
            throw CompositeAdapterException("Delegate type must be unique")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerViewHolder<ViewDataBinding> {
        if (mDelegates.size() != 0) {
            return mDelegates[viewType].onCreateViewHolder(parent)
        } else {
            throw CompositeAdapterException("Composite adapter must have at least one delegate")
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder<*>?, position: Int) {
        val data = data[position]
        if (holder != null) {
            @Suppress("UNCHECKED_CAST")
            (mDelegates[getItemViewType(position)] as ExpandableItemDelegate<*, T>)
                    .onBindViewHolder(holder, data, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].data?.getType() ?: -1
    }

    override fun getItemCount() = data.getSize()

    class RecyclerViewHolder<out T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root)

}