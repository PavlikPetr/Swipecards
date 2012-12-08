package com.topface.topface.requests;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthRequest extends AbstractApiRequest {
    // Data
    public static final String SERVICE_NAME = "auth";
    public static final String FALLBACK_CLIENT_VERSION = "fallback_client_version";
    public static final String FALLBACK_LOCALE = "en_US";
    public String sid; // id пользователя в социальной сети
    public String token; // токен авторизации в соц сети
    public String platform; // код социальной сети
    private String locale; // локаль обращающегося клиента
    private String clienttype; // тип клиента
    private String clientversion; // версия клиента
    private String clientdevice; // тип устройства клиента
    private String clientid; // уникальный идентификатор клиентского устройства
    public Boolean sandbox; // параметр использования тестовых аккаунтов для уведомлений APNS и C2DM

    public AuthRequest(Context context) {
        super(context);
        doNeedAuthorize(false);
        doNeedAlert(false);
        clienttype = Static.CLIENT_TYPE;
        locale = getClientLocale(context);
        clientversion = getClientVersion(context);
        clientdevice = getClientDeviceName();
        clientid = getClientId();
    }

    private String getClientLocale(Context context) {
        String locale;
        //На всякий случай проверяем возможность получить локаль
        try {
            locale = context.getResources().getString(R.string.app_locale);
        }
        catch (Exception e) {
            locale = FALLBACK_LOCALE;
        }

        return locale;
    }

    private String getClientVersion(Context context) {
        String version;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.error(e);
            version = FALLBACK_CLIENT_VERSION;
        }
        return version;
    }

    private String getClientDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL + " " + Build.PRODUCT;
    }

    private String getClientId() {
        return "Android " + Build.VERSION.RELEASE + " (" + Build.ID + ")";
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
                .put("clientdevice", clientdevice)
                .put("clientid", clientid)
                .put("sandbox", sandbox);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
