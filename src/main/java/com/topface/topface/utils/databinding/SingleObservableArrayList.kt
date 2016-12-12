package com.topface.topface.utils.databinding

import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import com.topface.framework.utils.Debug

/**
 * Observable только с одним подписчиком. Чтобы при бинде через адаптеры не сетить листенер каждый раз например
 * Created by tiberal on 30.11.16.
 */
class SingleObservableArrayList<T> {

    val observableList = ObservableArrayList<T>()
    var listener: ObservableList.OnListChangedCallback<out ObservableList<*>>? = null
    var onCallbackBinded: IOnListChangedCallbackBinded? = null

    fun addOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<*>>?) {
        if (this.listener == null) {
            observableList.addOnListChangedCallback(listener)
            this.listener = listener
            onCallbackBinded?.onCallbackBinded()
        } else {
            Debug.log("SingleObservableArrayList observable already have listener")
        }
    }

    fun removeOnListChangedCallback(listener: ObservableList.OnListChangedCallback<out ObservableList<*>>?) {
        if (this.listener != null) {
            observableList.removeOnListChangedCallback(listener)
            this.listener = null
        }
    }

    fun isListenerAdded() = listener != null

    fun removeListener() = removeOnListChangedCallback(listener)

    fun addAll(items: Collection<T>) {
        observableList.addAll(items)
    }

}

interface IOnListChangedCallbackBinded {
    fun onCallbackBinded()
}