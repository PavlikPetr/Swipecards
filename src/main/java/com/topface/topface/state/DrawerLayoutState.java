package com.topface.topface.state;

import com.topface.topface.data.leftMenu.DrawerLayoutStateData;

import rx.subjects.BehaviorSubject;

import static com.topface.topface.data.leftMenu.DrawerLayoutStateData.*;

public class DrawerLayoutState {

    private BehaviorSubject<DrawerLayoutStateData> mDrawerLayoutStateObservable;

    public DrawerLayoutState() {
        mDrawerLayoutStateObservable = BehaviorSubject.create(new DrawerLayoutStateData(UNDEFINED));
    }

    /**
     * Get BehaviorSubject for catching DrawerLayout state
     *
     * @return observable of DrawerLayout states
     */
    public BehaviorSubject<DrawerLayoutStateData> getObservable() {
        return mDrawerLayoutStateObservable;
    }

    /**
     * send new state of DrawerLayout
     *
     * @param data new state
     */
    public void newState(DrawerLayoutStateData data) {
        mDrawerLayoutStateObservable.onNext(data);
    }

    /**
     * Notify that DrawerLayout is closed
     */
    public void onClose() {
        newState(new DrawerLayoutStateData(CLOSED));
    }

    /**
     * Notify that DrawerLayout is opened
     */
    public void onOpen() {
        newState(new DrawerLayoutStateData(OPENED));
    }

    /**
     * Notify that DrawerLayout is slide
     */
    public void onSlide() {
        newState(new DrawerLayoutStateData(SLIDE));
    }

    /**
     * Notify that DrawerLayout is change state
     */
    public void onStateChanged() {
        newState(new DrawerLayoutStateData(STATE_CHANGED));
    }

}
