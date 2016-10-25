package com.topface.topface.utils.extensions

/**
 *
 * Created by siberia87 on 25.10.16.
 */

fun IntRange.toList(): MutableList<Int> {
    val arr = mutableListOf<Int>()
    for (i in this) arr.add(i)
    return arr
}