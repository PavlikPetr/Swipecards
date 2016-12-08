package com.topface.topface.ui.new_adapter.enhanced

import android.databinding.ViewDataBinding

/** Интерфейс компонента для адаптера
 * @param T item binding
 * @param D item data
 * Created by tiberal on 28.11.16.
 */
abstract class AdapterComponent<T : ViewDataBinding, in D> {
    abstract val itemLayout: Int
    abstract val bindingClass: Class<T>

    abstract fun bind(binding: T, data: D?, position: Int): Unit

    fun onBindViewHolder(holder: ViewHolder<*>?, data: Any?, position: Int) {
        bind(bindingClass.cast(holder?.binding), data as D, position)
    }

    open fun release() {
    }
}