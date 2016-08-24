package com.topface.topface.utils

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Врапер для того, что  Gson мог десереализовать лист по классу
 * Created by tiberal on 15.08.16.
 */
class ListOfJson<T>(private val clazz: Class<T>) : ParameterizedType {
    override fun getRawType() = List::class.java
    override fun getOwnerType() = null
    override fun getActualTypeArguments() = arrayOf<Type>(clazz)
}