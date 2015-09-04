package com.topface.topface.utils.controllers.startactions;

import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.DatingLockPopup;

import java.lang.ref.WeakReference;


public class DatingLockPopupAction extends DailyPopupAction {

    private int mPriority;
    private DatingLockPopup.DatingLockPopupRedirectListener mDatingLockPopupRedirect;
    private FragmentManager mFragmentManager;
    private WeakReference<BaseFragmentActivity> mActivity;


    public DatingLockPopupAction(FragmentManager fragmentManager, int priority
            , DatingLockPopup.DatingLockPopupRedirectListener listener, BaseFragmentActivity activity) {
        super(App.getContext());
        mFragmentManager = fragmentManager;
        mDatingLockPopupRedirect = listener;
        mPriority = priority;
        mActivity = new WeakReference<>(activity);
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
        Options options = App.from(mActivity.get()).getOptions();
        return options.notShown.enabledDatingLockPopup
                && isTimeoutEnded(options.notShown.datingLockPopupTimeout,
                getUserConfig().getDatingLockPopupRedirect());
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
