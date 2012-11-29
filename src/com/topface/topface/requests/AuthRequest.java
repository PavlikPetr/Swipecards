package com.topface.topface.requests;

import android.content.Context;
import android.os.Build;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthRequest extends AbstractApiRequest {
    // Data
    public static final String SERVICE_NAME = "auth";
    public String sid; // id пользователя в социальной сети
    public String token; // токен авторизации в соц сети
    public String platform; // код социальной сети
    public String locale; // локаль обращающегося клиента
    public String clienttype; // тип клиента
    public String clientversion; // версия клиента
    public String clientdevice; // тип устройства клиента
    public String clientid; // уникальный идентификатор клиентского устройства
    public Boolean sandbox; // параметр использования тестовых аккаунтов для уведомлений APNS и C2DM

    public AuthRequest(Context context) {
        super(context);
        try {
            doNeedAuthorize(false);
            doNeedAlert(false);
            //locale = context.getResources().getConfiguration().locale.toString();
            locale = context.getResources().getString(R.string.app_locale);
            clienttype = Static.CLIENT_TYPE;
            clientversion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            clientdevice = Build.BRAND + " " + Build.MANUFACTURER;
            clientid = Build.ID;
        } catch (Exception e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }
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
