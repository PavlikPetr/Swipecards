package com.topface.topface.data.leftMenu;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ppavlik on 13.05.16.
 * Hold observable of left menu selection state and switching navigation fragments
 * send emmits to it
 */
public class NavigationState {

    private static Subscriber<? super WrappedNavigationData> mLeftMenuSelectionSubscriber;
    private static Observable<WrappedNavigationData> mLeftMenuSelectionObservable;

    private static Subscriber<? super WrappedNavigationData> mNavigationSwitcherSubscriber;
    private static Observable<WrappedNavigationData> mNavigationSwitcherObservable;

    public NavigationState() {
        mLeftMenuSelectionObservable = Observable.create(new Observable.OnSubscribe<WrappedNavigationData>() {
            @Override
            public void call(Subscriber<? super WrappedNavigationData> subscriber) {
                mLeftMenuSelectionSubscriber = subscriber;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).share();
        mNavigationSwitcherObservable = Observable.create(new Observable.OnSubscribe<WrappedNavigationData>() {
            @Override
            public void call(Subscriber<? super WrappedNavigationData> subscriber) {
                mNavigationSwitcherSubscriber = subscriber;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).share();
    }

    /**
     * Report left menu item selected
     *
     * @param data selection item info
     */
    public void leftMenuItemSelected(WrappedNavigationData data) {
        if (mLeftMenuSelectionSubscriber != null && !mLeftMenuSelectionSubscriber.isUnsubscribed()) {
            mLeftMenuSelectionSubscriber.onNext(data);
        }
    }

    /**
     * Get left menu item selection observable
     *
     * @return observable
     */
    public Observable<WrappedNavigationData> getSelectionObservable() {
        return mLeftMenuSelectionObservable;
    }

    /**
     * Report about navigation fragment switched
     *
     * @param data switched fragment info
     */
    public void navigationFragmentSwitched(WrappedNavigationData data) {
        if (mNavigationSwitcherSubscriber != null && !mNavigationSwitcherSubscriber.isUnsubscribed()) {
            mNavigationSwitcherSubscriber.onNext(data);
        }
    }

    /**
     * Get navigation fragments observable
     *
     * @return observable
     */
    public Observable<WrappedNavigationData> getSwitchObservable() {
        return mNavigationSwitcherObservable;
    }
}
