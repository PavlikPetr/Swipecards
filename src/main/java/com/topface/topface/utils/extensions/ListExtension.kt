package com.topface.topface.utils.extensions

fun <T> List<T>.isNotEmpty() = !this?.isEmpty() ?: false

fun <T> List<T>.isEntry(position: Int) =
        this.isNotEmpty() && position >= 0 && position < this.size;