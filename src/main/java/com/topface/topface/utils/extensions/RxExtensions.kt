package com.topface.topface.utils.extensions

import rx.Subscription

fun Subscription?.safeUnsubscribe() {
    if (this != null && !this.isUnsubscribed) {
        this.unsubscribe()
    }
}

fun Array<Subscription?>.safeUnsubscribe() {
    forEach(Subscription?::safeUnsubscribe)
}