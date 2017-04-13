package com.topface.topface.utils.rx

import com.topface.framework.utils.Debug
import rx.Observable
import rx.Subscriber
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
    subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
})

fun <T> shortSubscription(next: (T) -> Unit) = object : Subscriber<T>() {

    override fun onCompleted() {
        if (isUnsubscribed) {
            unsubscribe()
        }
    }

    override fun onError(e: Throwable) {
        Debug.log("ShortSubscription " + e.toString())
        e.printStackTrace()
    }

    override fun onNext(type: T) = next(type)
}

inline fun <T> Observable<T>.shortSubscribe(crossinline next: (T) -> Unit): Subscription
        = subscribe(shortSubscription { next(it) })


