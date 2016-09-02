package com.topface.topface.utils.controllers.startactions;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.data.Options;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.utils.popups.PopupManager;

public class DatingLockPopupAction extends DailyPopupAction {

    private int mPriority;
    private FragmentManager mFragmentManager;
    private String mFrom;


    public DatingLockPopupAction(FragmentManager fragmentManager, int priority, String from) {
        super(App.getContext());
        mFragmentManager = fragmentManager;
        mPriority = priority;
        mFrom = from;
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
        datingLockPopup.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                PopupManager.INSTANCE.informManager(mFrom);
            }
        });
        datingLockPopup.show(mFragmentManager, DatingLockPopup.TAG);
    }

    @Override
    public boolean isApplicable() {
        Options options = App.get().getOptions();
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

}
