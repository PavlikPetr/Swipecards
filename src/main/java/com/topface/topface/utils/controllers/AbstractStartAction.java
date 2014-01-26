package com.topface.topface.utils.controllers;

import com.topface.topface.Static;

/**
 * Created by kirussell on 08.01.14.
 * Use this class to create Start Actions with implemented toString() method for debug purposes
 */
public abstract class AbstractStartAction implements IStartAction {
    @Override
    public abstract void callInBackground();

    @Override
    public abstract void callOnUi();

    @Override
    public abstract boolean isApplicable();

    @Override
    public abstract int getPriority();

    @Override
    public abstract String getActionName();

    @Override
    public final String toString() {
        return getActionName() + Static.SEMICOLON +
                getPriority() + Static.SEMICOLON +
                isApplicable();
    }
}
