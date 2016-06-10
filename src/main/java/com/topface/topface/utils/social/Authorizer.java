package com.topface.topface.utils.social;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Abstract class for starting different types of authorization
 */
public abstract class Authorizer {

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
