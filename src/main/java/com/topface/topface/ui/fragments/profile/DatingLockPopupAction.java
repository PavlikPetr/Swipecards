package com.topface.topface.ui.fragments.profile;

import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.controllers.AbstractStartAction;


public class DatingLockPopupAction extends AbstractStartAction {


    private DatingLockPopup.DatingLockPopupRedirectListener mDatingLockPopupRedirect;
    private FragmentManager mFragmentManager;
    private int mPriority;

    public DatingLockPopupAction(FragmentManager fragmentManager, int priority, DatingLockPopup.DatingLockPopupRedirectListener mDatingLockPopupRedirect) {
        this.mFragmentManager = fragmentManager;
        this.mPriority = priority;
        this.mDatingLockPopupRedirect = mDatingLockPopupRedirect;
    }

    private boolean isTimeoutEnded() {
        Options options = CacheProfile.getOptions();
        if (options.notShown.enabledDatingLockPopup) {
            long lastTime = App.getUserConfig().getDatingLockPopupShow();
            if (lastTime == 0) {
                //показываем попап первый раз
                return true;
            }
            long currentTime = System.currentTimeMillis();
            return ((currentTime - lastTime) >= options.notShown.datingLockPopupTimeout);
        } else {
            return false;
        }
    }

    @Override
    public void callInBackground() {
        App.getUserConfig().setDatingLockPopupShow(System.currentTimeMillis());
    }

    @Override
    public void callOnUi() {
        DatingLockPopup datingLockPopup = new DatingLockPopup();
        datingLockPopup.setDatingLockPopupRedirectListener(mDatingLockPopupRedirect);
        datingLockPopup.show(mFragmentManager, DatingLockPopup.TAG);
    }

    @Override
    public boolean isApplicable() {
        return (CacheProfile.getOptions().notShown.enabledDatingLockPopup && isTimeoutEnded());
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return "DatingLockPopup";
    }
}
