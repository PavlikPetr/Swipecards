package com.topface.topface.ui.fragments.profile;

import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.utils.config.UserConfig;


public class DatingLockPopupAction extends DailyPopupAction {

    private DatingLockPopup.DatingLockPopupRedirectListener mDatingLockPopupRedirect;
    private FragmentManager mFragmentManager;
    private Options.NotShown mNotShown;

    public DatingLockPopupAction(FragmentManager fragmentManager, int priority, DatingLockPopup.DatingLockPopupRedirectListener listener) {
        super(App.getContext(), priority);
        mFragmentManager = fragmentManager;
        mDatingLockPopupRedirect = listener;
        mNotShown = mOptions.notShown;
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
        return mOptions.notShown.enabledDatingLockPopup
                && isTimeoutEnded(mNotShown.datingLockPopupTimeout);
    }
}
