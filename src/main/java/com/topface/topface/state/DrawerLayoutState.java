package com.topface.topface.state;

import com.topface.topface.data.leftMenu.DrawerLayoutStateData;

import rx.subjects.BehaviorSubject;

import static com.topface.topface.data.leftMenu.DrawerLayoutStateData.*;

/**
 * Created by ppavlik on 17.05.16.
 */
public class DrawerLayoutState {

    private BehaviorSubject<DrawerLayoutStateData> mDrawerLayoutStateObservable;

    public DrawerLayoutState() {
        mDrawerLayoutStateObservable = BehaviorSubject.create(new DrawerLayoutStateData(UNDEFINED));
    }

    public BehaviorSubject<DrawerLayoutStateData> getObservable() {
        return mDrawerLayoutStateObservable;
    }

    public void newState(DrawerLayoutStateData data) {
        mDrawerLayoutStateObservable.onNext(data);
    }

    public void onClose() {
        newState(new DrawerLayoutStateData(CLOSED));
    }

    public void onOpen() {
        newState(new DrawerLayoutStateData(OPENED));
    }

    public void onSlide() {
        newState(new DrawerLayoutStateData(SLIDE));
    }

    public void onStateChanged() {
        newState(new DrawerLayoutStateData(STATE_CHANGED));
    }

}
