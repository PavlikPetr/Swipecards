package com.topface.topface.utils.rx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import com.topface.framework.utils.Debug
import rx.Emitter
import rx.Observable
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

fun Context.observeBroabcast(filter: IntentFilter, broadcastPermission: String? = null,
                             scheduler: Handler? = null): Observable<Intent>
        = Observable.fromEmitter<Intent>({ emitter ->
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                emitter.onNext(it)
            }
        }
    }
    emitter.setCancellation {
        applicationContext.unregisterReceiver(receiver)
    }
    if (broadcastPermission != null || scheduler != null) {
        applicationContext.registerReceiver(receiver, filter, broadcastPermission, scheduler)
    } else {
        applicationContext.registerReceiver(receiver, filter)
    }
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


