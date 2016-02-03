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
    public static final int TOKEN_NOT_READY = 1;
    public static final int TOKEN_PREPARING = 2;
    public static final int TOKEN_FAILED = 3;

    public abstract void authorize(Activity activity);

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

    public abstract void logout();
}
