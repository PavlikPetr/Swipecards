package com.topface.topface.state

import com.topface.topface.utils.rx.applySchedulers
import rx.Emitter
import rx.Observable
import rx.functions.Action1

class NoBackpreasureObservable<T>(data: T) : DataAndObservable<T, Observable<T>>(data) {
    var mEmmitter: Emitter<T>? = null
    override fun createObservable(data: T) = Observable.fromEmitter<T>({
        mEmmitter = it
        val callback = object : Action1<T> {
            override fun call(t: T) = Unit
        }
        it.setCancellation {
        }
    }, Emitter.BackpressureMode.LATEST).share().applySchedulers()

    override fun emmitData(data: T) {
        mEmmitter?.onNext(data)
    }
}