package com.topface.topface.data.leftMenu;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.subjects.PublishSubject;

/**
 * Created by ppavlik on 13.05.16.
 * Hold observable of left menu selection state and switching navigation fragments
 * send emmits to it
 */
public class NavigationState {

    private PublishSubject<WrappedNavigationData> mNavigationPublishSubject;

    public NavigationState() {
        mNavigationPublishSubject = PublishSubject.create();
    }

    public Observable<WrappedNavigationData> getNavigationObservable() {
        return mNavigationPublishSubject;
    }

    public void emmitNavigationState(final WrappedNavigationData data) {
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                mNavigationPublishSubject.onNext(data);
            }
        });
    }
}
