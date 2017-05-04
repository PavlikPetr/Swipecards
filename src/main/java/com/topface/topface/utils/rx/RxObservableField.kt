package com.topface.topface.utils.rx

import android.databinding.ObservableField
import rx.Emitter
import rx.Observable
import rx.Subscription

class RxObservableField<T>(val data: T? = null) : ObservableField<T>(data) {

    val asRx: Observable<T> by lazy {
        Observable.fromEmitter<T>({
            val callback = object : android.databinding.Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(bindingObservable: android.databinding.Observable, p1: Int) = it.onNext(get())
            }
            it.setCancellation {
                removeOnPropertyChangedCallback(callback)
            }
            addOnPropertyChangedCallback(callback)
        }, Emitter.BackpressureMode.LATEST)
    }

    fun subscribe(onNext: (T) -> Unit, onError: (Throwable) -> Unit = { it.printStackTrace() }):
            Subscription = asRx.subscribe(onNext, onError)

}