package com.topface.topface.utils.databinding

import java.util.*

/**
 * Created by petrp on 15.01.2017.
 */

interface IArrayListChange<T> {
    fun onChange(list: ArrayList<T>)
}