package com.topface.topface.utils.extensions

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Хелперы для биндингов
 * Created by tiberal on 01.08.16.
 */

fun ViewDataBinding.appContext() = root.context.applicationContext!!

fun <V : ViewDataBinding> Context.inflateBinding(@LayoutRes res: Int): V {
    return DataBindingUtil.inflate<V>((this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater),
            res, null, false)
}

fun <V : ViewDataBinding> Activity.inflateBinding(@LayoutRes res: Int): ReadOnlyProperty<Activity, V> {
    return DataBindingLazyInflater(res) { it.applicationContext.inflateBinding(res) }
}

fun <V : ViewDataBinding> Fragment.inflateBinding(res: Int): ReadOnlyProperty<Fragment, V> {
    return DataBindingLazyInflater(res) { it.context.inflateBinding(res) }
}

class DataBindingLazyInflater<in T, out V : ViewDataBinding>(val res: Int, val inflate: (T) -> V) :
        ReadOnlyProperty<T, V> {

    private var mBinding: ViewDataBinding? = null

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (mBinding == null) {
            mBinding = inflate(thisRef)
        }
        @Suppress("UNCHECKED_CAST")
        return mBinding as V
    }

}


