package com.topface.topface.utils.controllers.startactions;

import com.topface.topface.Static;

/**
 * Created by onikitin on 17.03.15.
 */
public abstract class LinkedStartAction implements IStartAction {

    protected OnNextActionListener mOnNextActionListener;

    @Override
    public String toString() {
        return getActionName() + Static.SEMICOLON +
                getPriority() + Static.SEMICOLON +
                isApplicable();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }
}
