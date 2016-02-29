package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;

/**
 * Делегат для актиити. Можно дописывать лдругие методы активити
 * Created by tiberal on 25.02.16.
 */
public interface IActivityDelegate {

    void startActivityForResult(Intent intent, int requestCode);

    Context getApplicationContext();

}
