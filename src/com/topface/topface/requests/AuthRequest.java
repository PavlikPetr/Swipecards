package com.topface.topface.requests;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class AuthRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "auth";
    public static final String FALLBACK_CLIENT_VERSION = "2.3.7.1";
    public static final String FALLBACK_LOCALE = "en_US";

    private String sid; // id пользователя в социальной сети
    private String token; // токен авторизации в соц сети
    private String platform; // код социальной сети
    private String locale; // локаль обращающегося клиента
    private String clienttype; // тип клиента
    private String clientversion; // версия клиента
    private String clientosversion; // версия операционной системы
    private String clientdevice; // тип устройства клиента
    private String clientid; // уникальный идентификатор клиентского устройства
    private String login;  // логин для нашей авторизации
    private String password; // пароль для нашей авторизации
    private String refresh; // еще один токен для одноклассников

    private AuthToken mAuthToken;

    private AuthRequest(Context context) {
        super(context);
        doNeedAlert(false);
        clienttype = Utils.getBuildType();
        locale = getClientLocale(context);
        clientversion = Utils.getClientVersion();
        clientosversion = Utils.getClientOsVersion();
        clientdevice = Utils.getClientDeviceName();
        clientid = getClientId(context);
    }

    public AuthRequest(AuthToken authToken, Context context) {
        this(context);
        mAuthToken = authToken;
        platform = authToken.getSocialNet();
        if (TextUtils.equals(platform, AuthToken.SN_TOPFACE)) {
            login = authToken.getLogin();
            password = authToken.getPassword();
        } else if (TextUtils.equals(platform, AuthToken.SN_ODNOKLASSNIKI)) {
            sid = authToken.getUserId();
            token = authToken.getTokenKey();
            refresh = mAuthToken.getmExpiresIn();
        } else {
            sid = authToken.getUserId();
            token = authToken.getTokenKey();
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

    private static String uniqueID = null;

    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    /**
     * Возвращает уникальный id устройства (на самом деле id установки, т.к. он генерится при первом запросе)
     *
     * @param context контекст
     * @return id устройства в виде строки UUID
     */
    public synchronized static String getClientId(Context context) {
        if (uniqueID == null && context != null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);

            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }

        return uniqueID;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("sid", sid)
                .put("token", token)
                .put("platform", platform)
                .put("locale", locale)
                .put("clienttype", clienttype)
                .put("clientversion", clientversion)
                .put("clientosversion", clientosversion)
                .put("clientdevice", clientdevice)
                .put("clientid", clientid)
                .put("login", login)
                .put("password", password)
                .put("refresh", refresh);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public boolean setSsid(String ssid) {
        //В AuthRequest у нас нет ssid
        this.ssid = null;
        return true;
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public AuthToken getAuthToken() {
        return mAuthToken;
    }

    @Override
    public void exec() {
        if (TextUtils.isEmpty(platform) || TextUtils.isEmpty(sid) || TextUtils.isEmpty(token)) {
            handleFail(ApiResponse.UNVERIFIED_TOKEN, "Key params are empty");
        } else {
            super.exec();
        }
    }
}
