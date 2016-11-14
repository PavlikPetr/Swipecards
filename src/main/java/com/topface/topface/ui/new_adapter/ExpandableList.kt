package com.topface.topface.ui.new_adapter

/**
 * Интерфейс выпадающей коллекции
 * Created by tiberal on 31.10.16.
 */
interface ExpandableList<T> {

    fun addExpandableItem(data: T, dataSequence: MutableList<T> = mutableListOf(), expandNow: Boolean = true): Unit?

    operator fun get(position: Int): ExpandableItem<T>

    fun expandItem(position: Int)

    fun constrictItem(position: Int)

    fun getSize(): Int
}