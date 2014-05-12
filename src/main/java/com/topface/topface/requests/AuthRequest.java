package com.topface.topface.requests;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.data.AppsFlyerData;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

public class AuthRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "auth.login";
    public static final String FALLBACK_CLIENT_VERSION = "2.9.0.1";
    public static final String FALLBACK_LOCALE = "en_US";
    /**
     * Временная зона девайса по умолчанию, отправляем каждый раз на сервер при авторизации
     */
    public static final String timezone = TimeZone.getDefault().getID();
    private String sid; // id пользователя в социальной сети
    private String token; // токен авторизации в соц сети
    private String platform; // код социальной сети
    private String locale; // локаль обращающегося клиента
    private String clienttype; // тип клиента
    private String clientversion; // версия клиента
    private String clientosversion; // версия операционной системы
    private String clientdevice; // тип устройства клиента
    private String login;  // логин для нашей авторизации
    private String password; // пароль для нашей авторизации
    private String refresh; // еще один токен для одноклассников
    private AppsFlyerData appsflyer; //ID пользователя в appsflyer
    private boolean tablet; // является ли данное устройство планшетом

    private AuthRequest(Context context) {
        super(context);
        doNeedAlert(false);
        clienttype = BuildConfig.BILLING_TYPE.getClientType();
        locale = getClientLocale(context);
        clientversion = Utils.getClientVersion();
        clientosversion = Utils.getClientOsVersion();
        clientdevice = Utils.getClientDeviceName();
        tablet = context.getResources().getBoolean(R.bool.is_tablet);
        try {
            appsflyer = new AppsFlyerData(context);
        } catch (Exception e) {
            Debug.error("AppsFlyer exception", e);
        }
    }

    public AuthRequest(AuthToken.TokenInfo authTokenInfo, Context context) {
        this(context);
        platform = authTokenInfo.getSocialNet();
        if (TextUtils.equals(platform, AuthToken.SN_TOPFACE)) {
            login = authTokenInfo.getLogin();
            password = authTokenInfo.getPassword();
        } else if (TextUtils.equals(platform, AuthToken.SN_ODNOKLASSNIKI)) {
            sid = authTokenInfo.getUserSocialId();
            token = authTokenInfo.getTokenKey();
            refresh = authTokenInfo.getExpiresIn();
        } else {
            sid = authTokenInfo.getUserSocialId();
            token = authTokenInfo.getTokenKey();
        }
    }

    private String getClientLocale(Context context) {
        String locale;
        //На всякий случай проверяем возможность получить локаль
        try {
            locale = context.getResources().getString(R.string.app_locale);
        } catch (Exception e) {
            locale = FALLBACK_LOCALE;
        }

        return locale;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject()
                .put("sid", sid)
                .put("token", token)
                .put("platform", platform)
                .put("locale", locale)
                .put("clientType", clienttype)
                .put("clientVersion", clientversion)
                .put("clientOsVersion", clientosversion)
                .put("clientDevice", clientdevice)
                .put("login", login)
                .put("password", password)
                .put("refresh", refresh)
                .put("timezone", timezone)
                .put("android_api_level", Build.VERSION.SDK_INT)
                .put("tablet", tablet);

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

        if (appsflyer != null) {
            data.put("appsflyer", appsflyer.toJson());
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public void exec() {
        if (TextUtils.isEmpty(platform)) {
            handleFail(ErrorCodes.UNVERIFIED_TOKEN, "Key params are empty");
            return;
        } else {
            if (TextUtils.equals(platform, AuthToken.SN_TOPFACE)) {
                if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
                    handleFail(ErrorCodes.UNVERIFIED_TOKEN, "Key params are empty");
                    return;
                }
            } else if (TextUtils.isEmpty(sid) || TextUtils.isEmpty(token)) {
                handleFail(ErrorCodes.UNVERIFIED_TOKEN, "Key params are empty");
                return;
            }
        }
        super.exec();
    }
}
