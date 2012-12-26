package com.topface.billing;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

/**
 * Класс возвращающий платежный драйвер согласно окружению и настройкам сборки
 */
public class BillingDriverManager {
    private static BillingDriverManager mInstance;
    private final String mBuildType;

    protected BillingDriverManager() {
        mBuildType = Utils.getBuildType();
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
    public BillingDriver createMainBillingDriver(Activity activity, BillingListener listener) {
        //TODO: тестовый режим, пока только Google Play
        return new GooglePlayV2BillingDriver(activity, listener);
    }

    public BillingDriver createMainBillingDriver(Activity activity, BillingListener listener, BillingSupportListener supportListener) {
        //TODO: тестовый режим, пока только Google Play
        BillingDriver driver = new GooglePlayV2BillingDriver(activity, listener);
        driver.setBillingSupportListener(supportListener);
        return driver;
    }

    private String getBuildType() {
        String type;
        Context context = App.getContext();

        try {
            //Получаем мета данные из информации приложения
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            //Получаем тип сборки
            type = info.metaData.getString(
                    context.getString(R.string.build_type_key));
        } catch (PackageManager.NameNotFoundException e) {
            Debug.error("BuildType error", e);
            type = context.getString(R.string.build_default);
        }

        return type;
    }
}
