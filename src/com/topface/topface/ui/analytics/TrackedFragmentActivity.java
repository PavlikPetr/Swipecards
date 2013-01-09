package com.topface.topface.ui.analytics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.social.AuthToken;

public class TrackedFragmentActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Вот такой вот костыль, чтобы AuthActivity не открывалось бесконечное колличество раз
        if(!getClass().getName().equals(AuthActivity.class.getName())) {
            if(new AuthToken(getApplicationContext()).isEmpty() || !CacheProfile.isLoaded()) {
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            }
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
}
