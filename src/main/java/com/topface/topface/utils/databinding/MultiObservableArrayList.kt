package com.topface.topface.utils.databinding

import android.annotation.SuppressLint
import com.topface.topface.utils.extensions.isEntry
import java.util.*
import java.util.function.Predicate

class MultiObservableArrayList<T> : ArrayList<T>() {

    private var mListeners = mutableListOf<IArrayListChange<T>>()

    fun addOnListChangeListener(listener: IArrayListChange<T>) {
        mListeners.add(listener)
    }

    fun removeOnListChangeListener(listener: IArrayListChange<T>) {
        mListeners.remove(listener)
    }

    override fun add(element: T): Boolean {
        return super.add(element).apply { informSubscribers() }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        informSubscribers()
    }

    override fun addAll(elements: Collection<T>): Boolean {
        return super.addAll(elements).apply { informSubscribers() }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements).apply { informSubscribers() }
    }

    override fun remove(element: T): Boolean {
        return super.remove(element).apply { informSubscribers() }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return super.removeAll(elements).apply { informSubscribers() }
    }

    override fun removeAt(index: Int): T {
        return super.removeAt(index).apply { informSubscribers() }
    }

    @SuppressLint("NewApi")
    override fun removeIf(filter: Predicate<in T>?): Boolean {
        return super.removeIf(filter).apply { informSubscribers() }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        super.removeRange(fromIndex, toIndex).apply { informSubscribers() }
    }

    override fun set(index: Int, element: T): T {
        return super.set(index, element).apply { informSubscribers() }
    }

    override fun clear() {
        super.clear().apply { informSubscribers() }
    }

    fun replaceData(list: ArrayList<T>) {
        with(this@MultiObservableArrayList) {
            var i: Int = 0
            while (i < list.size) {
                if (isEntry(i)) {
                    this[i] = list.get(i)
                } else {
                    this.addAll(i, list.subList(i, list.size))
                    break
                }
                i++
            }
            if (size > list.size) {
                removeRange(list.size, size)
            }
        }
        informSubscribers()
    }

    private fun informSubscribers() = mListeners.forEach { it.onChange(getList()) }

    fun getList() = arrayListOf<T>().apply { addAll(this@MultiObservableArrayList) }
}
