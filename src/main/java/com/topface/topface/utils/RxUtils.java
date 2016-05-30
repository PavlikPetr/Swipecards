package com.topface.topface.utils;

import rx.Subscriber;
import rx.Subscription;

/**
 * Ништяки для упрощения работы с rx
 * Created by tiberal on 25.05.16.
 */
public class RxUtils {

    public static void safeUnsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public static class ShortSubscription<T> extends Subscriber<T> {

        @Override
        public void onCompleted() {
            if (isUnsubscribed()) {
                unsubscribe();
            }
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(T type) {

        }
    }

}
