package com.topface.topface.utils;

import android.content.Context;
import android.os.Build;
import com.topface.topface.R;

public class ClientUtils {
    public static final String FALLBACK_LOCALE = "en_US";


    public static String getClientDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL + " " + Build.PRODUCT +
                " (Android " + Build.VERSION.RELEASE + ", build " + Build.ID + ")";
    }

    public static String getClientLocale(Context context) {
        String locale;
        //На всякий случай проверяем возможность получить локаль
        try {
            locale = context.getResources().getString(R.string.app_locale);
        } catch (Exception e) {
            locale = FALLBACK_LOCALE;
        }

        return locale;
    }
}
