package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.Http;

public class MainActivity extends BaseFragmentActivity {
    // Data
    //---------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_main);

        if (!Http.isOnline(this))
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT).show();

        if (!Data.isSSID()) {
            startActivity(new Intent(getApplicationContext(), AuthActivity.class));
            finish();
        } else {
            getProfile();
        }
    }

    //---------------------------------------------------------------------------
    private void getProfile() {
        ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
        registerRequest(profileRequest);
        profileRequest.part = ProfileRequest.P_DASHBOARD;
        profileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                CacheProfile.setData(Profile.parse(response));
                Http.avatarOwnerPreloading();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), NavigationActivity.class));
                        finish();
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------
}
