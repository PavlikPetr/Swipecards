package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends ApiRequest {
    public static final String SERVICE = "user.getOwnProfile";
    public int part; // часть профиля, необходимая для загрузки

    public static final int P_ALL = 0;
    public static final int P_EMAIL_CONFIRMED = 1;
    public static final int P_BALANCE_COUNTERS = 2;
    public static final int P_NECESSARY_DATA = 3;

    public ProfileRequest(Context context) {
        super(context);
        doNeedAlert(false); //чтобы не предупреждать пользователя алертом о пропаже инета
    }

    public ProfileRequest(int part, Context context) {
        this(context);
        this.part = part;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {

        JSONArray fields = null;
        switch (part) {
            case P_ALL:
                break;
            case P_EMAIL_CONFIRMED:
                fields = new JSONArray();
                fields.put("emailConfirmed");
                break;
            case P_BALANCE_COUNTERS:
                fields = new JSONArray();
                fields.put("likes");
                fields.put("money");
                break;
            case P_NECESSARY_DATA:
                fields = new JSONArray();
                fields.put("likes");
                fields.put("money");
                fields.put("canBecomeLeader");
                fields.put("gifts");
                fields.put("invisible");
                fields.put("premium");
                fields.put("showAd");
                fields.put("photo");
                fields.put("photos");
                break;
            default:
                break;
        }
        if (fields != null) {
            return new JSONObject().put("fields", fields);
        } else {
            return null;
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
