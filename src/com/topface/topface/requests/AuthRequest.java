package com.topface.topface.requests;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class AuthRequest extends AbstractApiRequest {
    // Data
    public static final String SERVICE_NAME = "auth";
    public static final String FALLBACK_CLIENT_VERSION = "unknown_client_version";
    public static final String FALLBACK_LOCALE = "en_US";
    public String sid; // id пользователя в социальной сети
    public String token; // токен авторизации в соц сети
    public String platform; // код социальной сети
    private String locale; // локаль обращающегося клиента
    private String clienttype; // тип клиента
    private String clientversion; // версия клиента
    private String clientdevice; // тип устройства клиента
    private String clientid; // уникальный идентификатор клиентского устройства
    public String login;  // логин для нашей авторизации
    public String password; // пароль для нашей авторизации
    private static String mDeviceId;

    public AuthRequest(Context context) {
        super(context);
        doNeedAuthorize(false);
        doNeedAlert(false);
        clienttype = Utils.getBuildType();
        locale = getClientLocale(context);
        clientversion = Utils.getClientVersion(context);
        clientdevice = getClientDeviceName();
        clientid = getClientId(context);
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

    private String getClientDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL + " " + Build.PRODUCT +
                " (Android " + Build.VERSION.RELEASE + ", build " + Build.ID + ")";
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
        if (uniqueID == null) {
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
                .put("clientdevice", clientdevice)
                .put("clientid", clientid)
                .put("login", login)
                .put("password", password);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
