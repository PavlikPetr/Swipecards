package com.topface.topface.utils

/**
 * Register/unregister state saver delegate
 * Created by tiberal on 21.10.16.
 */
interface IStateSaverRegistrator {
    fun registerLifeCycleDelegate(vararg stateSaver: ILifeCycle)
    fun unregisterLifeCycleDelegate(vararg stateSaver: ILifeCycle)
}

fun <T> Any.registerLifeCycleDelegate(someAny: T): T {
    if (someAny is ILifeCycle && this is IStateSaverRegistrator) {
        registerLifeCycleDelegate(someAny)
    }
    return someAny
}

fun <T> Any.unregisterLifeCycleDelegate(someAny: T) {
    if (someAny is ILifeCycle && this is IStateSaverRegistrator) {
        unregisterLifeCycleDelegate(someAny)
    }
}

fun <T> Any.unregisterLifeCycleDelegate(someAnyList: Collection<T>) {
    if (this is IStateSaverRegistrator) {
        someAnyList.forEach {
            if (it is ILifeCycle) {
                this.unregisterLifeCycleDelegate(it)
            }
        }
    }
}
