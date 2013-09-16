package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends ApiRequest {
    // Data
    public static final String PROFILE_UPDATE_ACTION = "com.topface.topface.UPDATE_PROFILE";
    public static final String SERVICE_NAME = "user.getOwnProfile";
    public int part; // часть профиля, необходимая для загрузки
    //public String  fields;  //массив интересующих полей профиля
    // Constants
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

        JSONArray fields;
        switch (part) {
            case P_ALL:
                fields = null;
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
                fields = new JSONArray();
                break;
        }
        if (fields != null) {
            return new JSONObject()
                    .put("fields", fields)
                            //При запросе профиля считаем текущего пользователя "гостем"
                    .put("visitor", true);
        }
        return new JSONObject()
                //При запросе профиля считаем текущего пользователя "гостем"
                .put("visitor", true);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
