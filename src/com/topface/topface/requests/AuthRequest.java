package com.topface.topface.requests;

import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.Static;
import com.topface.topface.utils.Debug;
import android.content.Context;
import android.os.Build;

public class AuthRequest extends ApiRequest {
    // Data
    private String service = "auth";
    public String sid; // id пользователя в социальной сети
    public String token; // токен авторизации в соц сети
    public String platform; // код социальной сети
    public String locale; // локаль обращающегося клиента
    public String clienttype; // тип клиента
    public String clientversion; // версия клиента
    public String clientdevice; // тип устройства клиента
    public String clientid; // уникальныц идентификатор клиентского устройства
    public Boolean sandbox; // параметр использования тестовых аккаунтов для уведомлений APNS и C2DM
    //---------------------------------------------------------------------------
    public AuthRequest(Context context) {
        super(context);
        try {
            locale = context.getResources().getConfiguration().locale.getLanguage();
            clienttype = Static.CLIENT_TYPE;
            clientversion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            clientdevice = Build.BRAND + " " + Build.MANUFACTURER;
            clientid = Build.ID;
        } catch(Exception e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }
    }
    //---------------------------------------------------------------------------
    public String toString() {
        JSONObject root = new JSONObject();

        try {
            root.put("service", service);
            root.put("ssid", ssid);
            root.put("data", new JSONObject().put("sid", sid).put("token", token).put("platform", platform).put("locale", locale).put("clienttype", clienttype).put("clientversion", clientversion).put("clientdevice", clientdevice).put("clientid", clientid).put("sandbox", sandbox));
        } catch(JSONException e) {
            Debug.log(this, "Wrong request compiling: " + e);
        }

        return root.toString();
    }
    //---------------------------------------------------------------------------
}
