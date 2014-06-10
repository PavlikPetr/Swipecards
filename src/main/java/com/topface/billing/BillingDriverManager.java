package com.topface.billing;

import android.app.Activity;

import com.topface.billing.googleplay.GooglePlayV2BillingDriver;
import com.topface.framework.utils.Debug;
import com.topface.topface.BuildConfig;

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
    public BillingDriver createMainBillingDriver(Activity activity, BillingListener listener, BillingSupportListener supportListener) {
        BillingDriver driver = null;

        switch (BuildConfig.BILLING_TYPE) {
            case AMAZON:
                try {
                    //мы получаем амазоновский драйвер динамически
                    Class driverClass = Class.forName("com.topface.billing.amazon.AmazonBillingDriver");
                    //noinspection unchecked
                    driver = (BillingDriver) driverClass
                            .getConstructor(Activity.class, BillingListener.class)
                            .newInstance(activity, listener);
                } catch (Exception e) {
                    Debug.error("Amazon library not found", e);
                }
                break;
            case GOOGLE_PLAY:
                driver = new GooglePlayV2BillingDriver(activity, listener);
                break;
            default:
                driver = new PaymentwallBillingDriver(activity, listener);

        }

        if (driver != null) {
            driver.setBillingSupportListener(supportListener);
        } else {
            throw new RuntimeException("BillingDriver not found");
        }

        return driver;
    }
}
