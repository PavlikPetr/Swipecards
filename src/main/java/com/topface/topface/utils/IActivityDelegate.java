package com.topface.topface.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.support.v4.app.FragmentManager;

/**
 * Created by tiberal on 10.03.16.
 */
public interface IActivityDelegate {

    ContentResolver getContentResolver();

    FragmentManager getSupportFragmentManager();

    Context getApplicationContext();
}
