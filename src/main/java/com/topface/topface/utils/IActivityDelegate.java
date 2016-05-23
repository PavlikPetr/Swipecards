package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.view.Window;

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

    Window getWindow();

}
