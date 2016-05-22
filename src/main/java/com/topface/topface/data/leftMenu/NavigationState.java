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

    /**
     * Send emmit when left menu item selected
     *
     * @param data       left menu settings data
     * @param senderType sender type
     */
    public void emmitItemSelected(final LeftMenuSettingsData data, @WrappedNavigationData.NavigationEventSenderType final int senderType) {
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                mNavigationPublishSubject.onNext(new WrappedNavigationData(data, senderType, WrappedNavigationData.SELECTED_ITEM));
            }
        });
    }

    /**
     * Send emmit when navigation fragment switched
     *
     * @param data       left menu settings data
     * @param senderType sender type
     */
    public void emmitFragmentSwitched(final LeftMenuSettingsData data, @WrappedNavigationData.NavigationEventSenderType final int senderType) {
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
            @Override
            public void call() {
                mNavigationPublishSubject.onNext(new WrappedNavigationData(data, senderType, WrappedNavigationData.SWITCHED_FRAGMENT));
            }
        });
    }

    /**
     * Get observable of left menu item selected
     *
     * @return observable
     */
    public Observable<WrappedNavigationData> getSelectedItemObservable() {
        return mNavigationPublishSubject.filter(new Func1<WrappedNavigationData, Boolean>() {
            @Override
            public Boolean call(WrappedNavigationData wrappedNavigationData) {
                return wrappedNavigationData != null && wrappedNavigationData.getDataType() == WrappedNavigationData.SELECTED_ITEM;
            }
        }).share();
    }

    /**
     * Get observable of navigation fragment switched
     *
     * @return observable
     */
    public Observable<WrappedNavigationData> getSwitchedFragmentObservable() {
        return mNavigationPublishSubject.filter(new Func1<WrappedNavigationData, Boolean>() {
            @Override
            public Boolean call(WrappedNavigationData wrappedNavigationData) {
                return wrappedNavigationData != null && wrappedNavigationData.getDataType() == WrappedNavigationData.SWITCHED_FRAGMENT;
            }
        }).share();
    }
}
