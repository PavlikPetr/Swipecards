package com.topface.topface.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentManager;

/**
 * Делегат для актиити. Можно дописывать лдругие методы активити
 * Created by tiberal on 25.02.16.
 */
public interface IActivityDelegate {

    void startActivityForResult(Intent intent, int requestCode);

    Context getApplicationContext();

    void startActivity(Intent intent);

    void runOnUiThread(Runnable runnable);

    boolean isFinishing();

    PackageManager getPackageManager();

    FragmentManager getSupportFragmentManager();

    ContentResolver getContentResolver();

    boolean isActivityRestoredState();

    Intent getIntent();

    void finish();
}
