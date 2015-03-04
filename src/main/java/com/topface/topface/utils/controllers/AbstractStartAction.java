package com.topface.topface.utils.controllers;

import com.topface.topface.Static;

import java.util.ArrayList;

/**
 * Created by kirussell on 08.01.14.
 * Use this class to create Start Actions with implemented toString() method for debug purposes
 */
public abstract class AbstractStartAction implements IStartAction {

    protected OnNextPopupStart mOnNextPopupStart;

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
    public boolean hasMoreActions() {
        return false;
    }

    @Override
    public void addAction(IStartAction action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ArrayList<IStartAction> getActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStartActionCulback(OnNextPopupStart startActionCallback) {
        mOnNextPopupStart = startActionCallback;
    }

    @Override
    public String toString() {
        return getActionName() + Static.SEMICOLON +
                getPriority() + Static.SEMICOLON +
                isApplicable();
    }

    public interface OnNextPopupStart {

        void onStart();

    }

}
