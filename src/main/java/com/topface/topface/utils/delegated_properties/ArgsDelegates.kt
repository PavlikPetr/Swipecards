package com.topface.topface.utils.delegated_properties

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.topface.topface.ui.fragments.feed.enhanced.base.IViewModelLifeCycle
import kotlin.reflect.KProperty

class BundleGetDelegate<T> {

    constructor(bundle: Bundle?, obtainValue: Bundle.() -> T) {
        mObtainValue = obtainValue
        mArgs = bundle
    }

    constructor(argsProvider: IViewModelLifeCycle?, obtainValue: Bundle.() -> T) {
        mObtainValue = obtainValue
        mArgsProvider = argsProvider
    }

    private val mObtainValue: Bundle.() -> T
    private var mArgs: Bundle? = null
    private var mArgsProvider: IViewModelLifeCycle? = null

    var value: T? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = when {
        mArgs != null && mArgsProvider == null -> mArgs?.let { mObtainValue(it) }
        mArgs == null && mArgsProvider != null -> mArgsProvider?.args?.let { mObtainValue(it) }
        else -> value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value = value
    }
}

fun Bundle?.stringArg(key: String, defaultValue: String? = null) =
        BundleGetDelegate(this) { getString(key, defaultValue) }

fun Bundle?.intArg(key: String, defaultValue: Int? = null) = BundleGetDelegate(this) {
    if (defaultValue == null) {
        getInt(key)
    } else {
        getInt(key, defaultValue)
    }
}

fun Bundle?.boolArg(key: String, defaultValue: Boolean = false) =
        BundleGetDelegate(this) { getBoolean(key, defaultValue) }

fun Bundle?.longArg(key: String, defaultValue: Long? = null) = BundleGetDelegate(this) {
    if (defaultValue == null) {
        getLong(key)
    } else {
        getLong(key, defaultValue)
    }
}

fun <T : Parcelable> Bundle?.objectArg(key: String, defaultValue: T? = null) =
        BundleGetDelegate(this) { getParcelable<T>(key) }


fun IViewModelLifeCycle?.stringArg(key: String, defaultValue: String? = null) = BundleGetDelegate(this) { getString(key, defaultValue) }

fun IViewModelLifeCycle?.intArg(key: String, defaultValue: Int? = null) = BundleGetDelegate(this) {
    if (defaultValue == null) {
        getInt(key)
    } else {
        getInt(key, defaultValue)
    }
}

fun IViewModelLifeCycle?.boolArg(key: String, defaultValue: Boolean = false) = BundleGetDelegate(this) { getBoolean(key, defaultValue) }

fun IViewModelLifeCycle?.longArg(key: String, defaultValue: Long? = null) = BundleGetDelegate(this) {
    if (defaultValue == null) {
        getLong(key)
    } else {
        getLong(key, defaultValue)
    }
}

fun Fragment.stringArg(key: String, defaultValue: String? = null) = arguments.stringArg(key, defaultValue)

fun Fragment.boolArg(key: String, defaultValue: Boolean = false) = arguments.boolArg(key, defaultValue)

fun Fragment.longArg(key: String, defaultValue: Long? = null) = arguments.longArg(key, defaultValue)

fun Fragment.intArg(key: String, defaultValue: Int? = null) = arguments.intArg(key, defaultValue)

fun <T : Parcelable> Fragment.objectArg(key: String, defaultValue: T? = null) = arguments.objectArg(key, defaultValue)

fun AppCompatActivity.stringArg(key: String, defaultValue: String? = null) = intent.extras.stringArg(key, defaultValue)

fun AppCompatActivity.boolArg(key: String, defaultValue: Boolean = false) = intent.extras.boolArg(key, defaultValue)

fun AppCompatActivity.longArg(key: String, defaultValue: Long? = null) = intent.extras.longArg(key, defaultValue)

fun AppCompatActivity.intArg(key: String, defaultValue: Int? = null) = intent.extras.intArg(key, defaultValue)

fun <T : Parcelable> AppCompatActivity.objectArg(key: String, defaultValue: T? = null) = intent.extras.objectArg(key, defaultValue)









