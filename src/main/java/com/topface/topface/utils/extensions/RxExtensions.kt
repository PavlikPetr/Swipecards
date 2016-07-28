package com.topface.topface.utils.extensions

import rx.Subscription

fun Subscription.safeUnsubscribe() {
    if (!this.isUnsubscribed) {
        this.unsubscribe()
    }
}