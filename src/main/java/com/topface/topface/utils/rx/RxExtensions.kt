package com.topface.topface.utils.rx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.topface.topface.App
import rx.Emitter
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

fun observeBroadcast(filter: IntentFilter): Observable<Intent>
        = Observable.fromEmitter<Intent>({ emitter ->
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                emitter.onNext(it)
            }
        }
    }
    emitter.setCancellation {
        LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(receiver)
    }
    LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(receiver, filter)
}, Emitter.BackpressureMode.LATEST)

fun Subscription?.safeUnsubscribe() {
    if (this != null && !isUnsubscribed) {
        unsubscribe()
    }
}

fun Array<Subscription?>.safeUnsubscribe() = forEach(Subscription?::safeUnsubscribe)

fun <T> Observable<T>.applySchedulers(): Observable<T> = compose<T> {
    subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}

val onErrorDefault: (Throwable) -> Unit = {
    System.out.println("U must implement onError")
    it.printStackTrace()
}

fun <T> shortSubscription(error: (Throwable) -> Unit = onErrorDefault, next: (T) -> Unit) = object : Subscriber<T>() {

    override fun onCompleted() {
        if (isUnsubscribed) {
            unsubscribe()
        }
    }

    override fun onError(e: Throwable) = error(e)

    override fun onNext(type: T) = next(type)
}

inline fun <T> Observable<T>.shortSubscribe(crossinline next: (T) -> Unit): Subscription
        = subscribe(shortSubscription { next(it) })

inline fun <T> Observable<T>.shortSubscribe(crossinline next: (T) -> Unit, crossinline error: (Throwable) -> Unit): Subscription
        = subscribe(shortSubscription({ error(it) }, { next(it) }))


