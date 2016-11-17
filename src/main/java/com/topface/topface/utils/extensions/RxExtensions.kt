package com.topface.topface.utils.extensions

import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

fun Subscription?.safeUnsubscribe() {
    if (this != null && !this.isUnsubscribed) {
        this.unsubscribe()
    }
}

fun Array<Subscription?>.safeUnsubscribe() {
    forEach(Subscription?::safeUnsubscribe)
}

fun <T> Observable<T>.applySchedulers(): Observable<T> = compose<T>(Observable.Transformer<T, T> {
    subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
})
