package com.topface.topface.ui.fragments.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.data.Options;
import com.topface.topface.ui.dialogs.DatingLockPopup;
import com.topface.topface.ui.dialogs.RateAppDialog;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.controllers.AbstractStartAction;

/**
 * Created by onikitin on 26.11.14.
 */
public class DatingLockPopupAction extends AbstractStartAction {


    private FragmentManager fragmentManager;
    private int priority;

    public DatingLockPopupAction(FragmentManager fragmentManager, int priority) {
        this.fragmentManager = fragmentManager;
        this.priority = priority;
    }

    private boolean isTimeoutEnded() {
        Options options = CacheProfile.getOptions();
        if (options.enabledDatingLockPopup) {
            SharedPreferences preferences = App.getContext().getSharedPreferences(
                    Static.PREFERENCES_TAG_SHARED,
                    Context.MODE_PRIVATE);
            if (preferences == null) {
                return false;
            }
            long lastTime = preferences.getLong(DatingLockPopup.DATING_LOCK_POPUP, -1);
            if (lastTime == -1) {
                //показываем попап первый раз
                return true;
            }
            long currentTime = System.currentTimeMillis();
            return ((currentTime - lastTime) >= options.datingLockPopupTimeout);
        } else {
            return false;
        }
    }

    @Override
    public void callInBackground() {
        SharedPreferences preferences = App.getContext().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED,
                Context.MODE_PRIVATE
        );
        preferences.edit()
                .putLong(DatingLockPopup.DATING_LOCK_POPUP, System.currentTimeMillis())
                .apply();
    }

    @Override
    public void callOnUi() {
        DatingLockPopup datingLockPopup = new DatingLockPopup();
        datingLockPopup.show(fragmentManager, RateAppDialog.TAG);
    }

    @Override
    public boolean isApplicable() {
        return (CacheProfile.getOptions().enabledDatingLockPopup && isTimeoutEnded());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getActionName() {
        return "DatingLockPopup";
    }
}
