package com.topface.topface.ui.new_adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.ViewGroup
import org.jetbrains.anko.layoutInflater

/**
 * New behavior for adapter
 * @param T item binding
 * @param D item data
 */
interface IAdapterItemDelegate<T : ViewDataBinding, in D> {

    val itemLayout: Int
    val bindingClass: Class<T>

    fun onCreateViewHolder(parent: ViewGroup?): CompositeAdapter.RecyclerViewHolder<T> {
        return CompositeAdapter.RecyclerViewHolder(DataBindingUtil.inflate<T>(parent?.context?.layoutInflater,
                itemLayout, parent, false))
    }

    fun onBindViewHolder(holder: CompositeAdapter.RecyclerViewHolder<*>?, data: D?, position: Int) {
        bind(bindingClass.cast(holder?.binding), data, position)
    }

    fun bind(binding: T, data: D?, position: Int): Unit?
}