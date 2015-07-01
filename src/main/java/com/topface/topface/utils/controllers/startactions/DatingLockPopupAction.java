package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.ui.dialogs.DatingLockPopup;


public class DatingLockPopupAction extends DailyPopupAction {

    private int mPriority;
    private DatingLockPopup.DatingLockPopupRedirectListener mDatingLockPopupRedirect;
    private FragmentManager mFragmentManager;

    public DatingLockPopupAction(FragmentManager fragmentManager, int priority, DatingLockPopup.DatingLockPopupRedirectListener listener) {
        super(App.getContext());
        mFragmentManager = fragmentManager;
        mDatingLockPopupRedirect = listener;
        mPriority = priority;
    }

    @Override
    protected boolean firstStartShow() {
        return true;
    }

    @Override
    public void callInBackground() {
    }

    @Override
    public void callOnUi() {
        DatingLockPopup datingLockPopup = new DatingLockPopup();
        datingLockPopup.setDatingLockPopupRedirectListener(mDatingLockPopupRedirect);
        datingLockPopup.show(mFragmentManager, DatingLockPopup.TAG);
    }

    @Override
    public boolean isApplicable() {
//        return CacheProfile.getOptions().notShown.enabledDatingLockPopup
//                && isTimeoutEnded(CacheProfile.getOptions().notShown.datingLockPopupTimeout,
//                getUserConfig().getDatingLockPopupRedirect());
        return false;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {

    }
}
