package com.topface.topface.utils.extensions

fun <T> List<T>?.isNotEmpty() = !(this?.isEmpty() ?: true)

fun <T> List<T>.isEntry(position: Int) =
        this.isNotEmpty() && position >= 0 && position < this.size;

fun <T> Array<T>?.isNotEmpty() = !(this?.isEmpty() ?: true)

fun <T> Array<T>.isEntry(position: Int) =
        this.isNotEmpty() && position >= 0 && position < this.size;