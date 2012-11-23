package com.topface.topface.ui;

import android.content.Intent;
import android.preference.PreferenceActivity;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.IRequestClient;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.LinkedList;

public class BasePreferenceActivity extends PreferenceActivity implements IRequestClient {

    protected AuthorizationManager mAuthorizationManager;

    private LinkedList<ApiRequest> mRequests = new LinkedList<ApiRequest>();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthorizationManager != null) {
            mAuthorizationManager.onActivityResult(requestCode, resultCode, data);
        }
        Debug.log(this, "onActivityResult");
    }

    @Override
    public void registerRequest(ApiRequest request) {
        if (!mRequests.contains(request)) {
            mRequests.add(request);
        }
    }

    @Override
    public void removeRequest(ApiRequest request) {
        mRequests.remove(request);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (ApiRequest request : mRequests) {
            request.cancel();
        }
        mRequests.clear();
    }
}