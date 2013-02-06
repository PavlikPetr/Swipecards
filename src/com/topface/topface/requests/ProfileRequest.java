package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileRequest extends AbstractApiRequest {
    // Data
    public static final String PROFILE_UPDATE_ACTION = "com.topface.topface.UPDATE_PROFILE";
    public static final String SERVICE_NAME = "profile";
    public int part; // часть профиля, необходимая для загрузки
    //public String  fields;  //массив интересующих полей профиля
    // Constants
    public static final int P_ALL = 0;

    public ProfileRequest(Context context) {
        super(context);
        doNeedAlert(false); //чтобы не предупреждать пользователя алертом о пропаже инета
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {

        JSONArray fields;
        switch (part) {
            case P_ALL:
                fields = null;
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
