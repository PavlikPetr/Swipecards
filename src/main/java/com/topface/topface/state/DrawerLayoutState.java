package com.topface.topface.state;

import com.topface.topface.data.leftMenu.DrawerLayoutStateData;

import rx.subjects.BehaviorSubject;

/**
 * Created by ppavlik on 17.05.16.
 */
public class DrawerLayoutState {

    private BehaviorSubject<DrawerLayoutStateData> mDrawerLayoutStateObservable;

    public DrawerLayoutState(){
        mDrawerLayoutStateObservable =  BehaviorSubject.create();
    }

}
