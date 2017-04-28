package com.topface.topface.ui.new_adapter.enhanced

/**
 * Интерфейс для определения типа итема списка
 * Created by tiberal on 29.11.16.
 */
interface ITypeProvider {
    fun getType(java: Class<*>): Int
    fun getTypeByData(data: Any): Int = getType(data.javaClass)
}