package com.topface.topface.ui.analytics;

import android.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.ui.fragments.AuthFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.social.AuthToken;

public class TrackedFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(new AuthToken(getApplicationContext()).isEmpty() || !CacheProfile.isLoaded()) {
            startAuth();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        if (isTrackable()) {
            EasyTracker.getTracker().trackView(getTrackName());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    public boolean isTrackable() {
        return true;
    }

    protected String getTrackName() {
        return this.getClass().getSimpleName().replace("Activity", "");
    }

    public void startAuth() {
        AuthFragment af = AuthFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.content, af).commit();
    }

    public void close(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        onInit();
    }

    public void onInit() {}
}
