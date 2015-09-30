package com.topface.topface.utils.controllers.startactions;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.utils.CacheProfile;


public class DatingLockPopupAction extends DailyPopupAction {

    private int mPriority;
    private DatingLockPopup.DatingLockPopupRedirectListener mDatingLockPopupRedirect;
    private FragmentManager mFragmentManager;
    private OnNextActionListener mStartActionCallback;

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
        final DatingLockPopup datingLockPopup = new DatingLockPopup();
        datingLockPopup.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mStartActionCallback != null) {
                    mStartActionCallback.onNextAction();
                }
            }
        });
        datingLockPopup.setDatingLockPopupRedirectListener(new DatingLockPopup.DatingLockPopupRedirectListener() {
            @Override
            public void onRedirect() {
                datingLockPopup.setOnDismissListener(null);
                if (mDatingLockPopupRedirect != null) {
                    mDatingLockPopupRedirect.onRedirect();
                }
            }
        });
        datingLockPopup.show(mFragmentManager, DatingLockPopup.TAG);
    }

    @Override
    public boolean isApplicable() {
        return CacheProfile.getOptions().notShown.enabledDatingLockPopup
                && isTimeoutEnded(CacheProfile.getOptions().notShown.datingLockPopupTimeout,
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
        mStartActionCallback = startActionCallback;
    }
}
