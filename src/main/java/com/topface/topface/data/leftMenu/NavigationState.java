package com.topface.topface.data.leftMenu;

import com.topface.framework.utils.Debug;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
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
