package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthRequest extends ApiRequest {
    // Data
    public static final String SERVICE_NAME = "auth.login";
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
    private boolean tablet; // является ли данное устройство планшетом

    private AuthRequest(Context context) {
        super(context);
        doNeedAlert(false);
        clienttype = Utils.getBuildType();
        locale = getClientLocale(context);
        clientversion = Utils.getClientVersion();
        clientosversion = Utils.getClientOsVersion();
        clientdevice = Utils.getClientDeviceName();
        clientid = App.getConfig().getAppConfig().getAppUniqueId();
        tablet = context.getResources().getBoolean(R.bool.is_tablet);
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
        return new JSONObject()
                .put("sid", sid)
                .put("token", token)
                .put("platform", platform)
                .put("locale", locale)
                .put("clientType", clienttype)
                .put("clientVersion", clientversion)
                .put("clientosVersion", clientosversion)
                .put("clientDevice", clientdevice)
                .put("clientId", clientid)
                .put("login", login)
                .put("password", password)
                .put("refresh", refresh)
                .put("tablet", tablet);
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
