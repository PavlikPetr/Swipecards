package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Abstract class for starting different types of authorization
 */
public abstract class Authorizer {

    public static final String AUTH_TOKEN_READY_ACTION = "com.topface.topface.auth.token.ready";

    public static final String TOKEN_STATUS = "token_status";
    public static final int TOKEN_READY = 0;
    public static final int TOKEN_NOT_READY = -1;

    private Activity mActivity;

    public Authorizer(Activity activity) {
        mActivity = activity;
    }

    public abstract void authorize();

    public void onCreate(Bundle savedInstanceState) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
    }

    public Activity getActivity() {
        return mActivity;
    }

    public abstract void logout();

    public boolean refreshToken() {
        return false;
    }
}
