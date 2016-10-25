package com.topface.topface.utils

/**
 * Register/unregister state saver delegate
 * Created by tiberal on 21.10.16.
 */
interface IStateSaverRegistrator {
    fun registerStateDelegate(vararg stateSaver: IStateSaver)
    fun unregisterStateDelegate(vararg stateSaver: IStateSaver)
}