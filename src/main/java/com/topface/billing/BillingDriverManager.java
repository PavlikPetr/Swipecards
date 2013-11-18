package com.topface.billing;

import android.app.Activity;
import android.text.TextUtils;

import com.topface.billing.amazon.AmazonBillingDriver;
import com.topface.billing.googleplay.GooglePlayV2BillingDriver;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

/**
 * Класс возвращающий платежный драйвер согласно окружению и настройкам сборки
 */
public class BillingDriverManager {
    private static BillingDriverManager mInstance;
    //private final String mBuildType;

    protected BillingDriverManager() {
        //mBuildType = Utils.getBuildType();
    }

    public static BillingDriverManager getInstance() {
        if (mInstance == null) {
            mInstance = new BillingDriverManager();
        }

        return mInstance;
    }

    /**
     * Создает новый инстанс основного драйвера платежей, подходящий по типу окружения (Google Play, Amazon и т.п.)
     * (кроме осноного у нас будет еще и дополнительный, с оплатой по смс например)
     */
    /*public BillingDriver createMainBillingDriver(Activity activity, BillingListener listener) {
        //TODO: тестовый режим, пока только Google Play
        return new GooglePlayV2BillingDriver(activity, listener);
    }*/
    public BillingDriver createMainBillingDriver(Activity activity, BillingListener listener, BillingSupportListener supportListener) {
        BillingDriver driver;

        if (TextUtils.equals(Utils.getBuildType(), activity.getString(R.string.build_amazon))) {
            driver = new AmazonBillingDriver(activity, listener);
        } else {
            driver = new GooglePlayV2BillingDriver(activity, listener);
        }

        driver.setBillingSupportListener(supportListener);

        return driver;
    }
}
