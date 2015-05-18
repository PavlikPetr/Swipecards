package com.topface.topface.requests;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by onikitin on 11.02.15.
 * Базовый класс для авторизации и регистрации
 */
public abstract class PrimalAuthRequest extends ApiRequest {

    private String mClientVersion;// версия клиента
    private String mClientosVersion;// версия операционной системы
    private String mClientDevice;// тип устройства клиента
    private boolean mTablet;// является ли данное устройство планшетом
    private double mAndroidApiVersion;// версия апи
    private int mCodeVersion;// версия кода
    private CharSequence mAdId;// ad id from google play services
    private Integer mGooglePlayServicesVersion; // версия google play services
    private String mClientType; // тип клиента
    private String mLocale; // локаль обращающегося клиента
    private int mDpi; // локаль обращающегося клиента


    public PrimalAuthRequest(Context context) {
        super(context);
        mClientVersion = BuildConfig.VERSION_NAME;
        mClientosVersion = Utils.getClientOsVersion();
        mClientDevice = Utils.getClientDeviceName();
        mTablet = App.getContext().getResources().getBoolean(R.bool.is_tablet);
        mAndroidApiVersion = Build.VERSION.SDK_INT;
        mCodeVersion = BuildConfig.VERSION_CODE;
        mAdId = App.getAppConfig().getAdId();
        mGooglePlayServicesVersion = Utils.getGooglePlayServicesVersion();
        mClientType = BuildConfig.MARKET_API_TYPE.getClientType();
        mLocale = getClientLocale();
        mDpi = getDpi();
    }

    protected abstract String getClientLocale();

    public void setLocale(String locale) {
        this.mLocale = locale;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("clientVersion", mClientVersion)
                .put("clientOsVersion", mClientosVersion)
                .put("clientDevice", mClientDevice)
                .put("tablet", mTablet)
                .put("androidApiVersion", mAndroidApiVersion)
                .put("codeVersion", mCodeVersion)
                .put("clientType", mClientType)
                .put("locale", mLocale)
                .put("clientCarrier", Utils.getCarrierName())
                .put("screenDensity", Utils.getCarrierName());

        if (!TextUtils.isEmpty(mAdId)) {
            data.put("adId", mAdId);
        }
        if (mGooglePlayServicesVersion != null) {
            data.put("googlePlayServicesVersion", mGooglePlayServicesVersion);
        }
        //Устанавливаем clientDeviceId
        try {
            String androidId = Settings.Secure.getString(
                    getContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );

            if (!TextUtils.isEmpty(androidId)) {
                data.put("clientDeviceId", androidId);
            }
        } catch (Exception e) {
            Debug.error(e);
        }
        return data;
    }

    public int getDpi() {
        return App.getContext().getResources().getDisplayMetrics().densityDpi;
    }
}
