package com.topface.topface.unittests.startactionscontroller.utils;

import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

/**
 * Created by kirussell on 23/07/15.
 * Dummy action that applies given results on calls (background or onUi)
 */
public class DummyAction implements IStartAction {

    public static final int UNPROCESSED = -1;

    int mResult = UNPROCESSED;
    int mBackgroundResult = UNPROCESSED;
    int mOnUiResult = UNPROCESSED;
    private boolean mIsApplicable = true;
    private IResultListener mResultListener;
    private int mPriority = 0;

    public DummyAction() {
    }

    public DummyAction(int backgroundResult, int onUiResult, boolean isApplicable) {
        mBackgroundResult = backgroundResult;
        mOnUiResult = onUiResult;
        mIsApplicable = isApplicable;
    }

    public DummyAction(int onUiResult, boolean isApplicable) {
        mOnUiResult = onUiResult;
        mIsApplicable = isApplicable;
    }

    public DummyAction setProiority(int priority) {
        mPriority = priority;
        return this;
    }

    @Override public void callInBackground() {
        mResult = mBackgroundResult != UNPROCESSED ? mBackgroundResult : mResult;
    }

    @Override public void callOnUi() {
        mResult = mOnUiResult != UNPROCESSED ? mOnUiResult : mResult;
        mResultListener.checkResult(mResult);
    }

    public DummyAction setResultListener(IResultListener listener) {
        mResultListener = listener;
        return this;
    }

    @Override public boolean isApplicable() {
        return mIsApplicable;
    }

    @Override public int getPriority() {
        return mPriority;
    }

    @Override public String getActionName() {
        return null;
    }

    @Override public void setStartActionCallback(OnNextActionListener startActionCallback) {

    }

    public interface IResultListener {
        void checkResult(int result);
    }
}
