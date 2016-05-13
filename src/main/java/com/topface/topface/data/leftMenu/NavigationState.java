package com.topface.topface.data.leftMenu;

import com.topface.framework.utils.Debug;

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

    private static Subscriber<? super LeftMenuSettingsData> mLeftMenuSelectionSubscriber;
    private static Observable<LeftMenuSettingsData> mLeftMenuSelectionObservable;

    private static Subscriber<? super LeftMenuSettingsData> mNavigationSwitcherSubscriber;
    private static Observable<LeftMenuSettingsData> mNavigationSwitcherObservable;

    public NavigationState() {
        mLeftMenuSelectionObservable = Observable.create(new Observable.OnSubscribe<LeftMenuSettingsData>() {
            @Override
            public void call(Subscriber<? super LeftMenuSettingsData> subscriber) {
                mLeftMenuSelectionSubscriber = subscriber;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).share();
        mNavigationSwitcherObservable = Observable.create(new Observable.OnSubscribe<LeftMenuSettingsData>() {
            @Override
            public void call(Subscriber<? super LeftMenuSettingsData> subscriber) {
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
    public void leftMenuItemSelected(LeftMenuSettingsData data) {
        Debug.showChunkedLogError("NewMenuFragment", "leftMenuItemSelected " + (data != null ? data.getUniqueKey() : "null"));
        if (mLeftMenuSelectionSubscriber != null && !mLeftMenuSelectionSubscriber.isUnsubscribed()) {
            mLeftMenuSelectionSubscriber.onNext(data);
        }
    }

    /**
     * Get left menu item selection observable
     *
     * @return observable
     */
    public Observable<LeftMenuSettingsData> getSelectionObservable() {
        return mLeftMenuSelectionObservable;
    }

    /**
     * Report about navigation fragment switched
     *
     * @param data switched fragment info
     */
    public void navigationFragmentSwitched(LeftMenuSettingsData data) {
        if (mNavigationSwitcherSubscriber != null && !mNavigationSwitcherSubscriber.isUnsubscribed()) {
            mNavigationSwitcherSubscriber.onNext(data);
        }
    }

    /**
     * Get navigation fragments observable
     *
     * @return observable
     */
    public Observable<LeftMenuSettingsData> getSwitchObservable() {
        return mNavigationSwitcherObservable;
    }
}
