package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserRequest extends ApiRequest {
    private ArrayList<Integer> uids; // массив id пользователя в топфейсе
    //private ArrayList<String> fields; // массив интересующих полей профиля
    public Boolean visitor = true; // флаг определения текущего пользователя посетителем профилей запрошенных пользователей

    public UserRequest(int uid, Context context) {
        super(context);
        ArrayList<Integer> data = new ArrayList<Integer>();
        data.add(uid);
        uids = data;
        if (uids.size() < 1) {
            throw new NullPointerException();
        }
        doNeedAlert(false);

    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("uids", new JSONArray(uids))
                .put("visitor", visitor);
    }

    @Override
    public String getServiceName() {
        return "profiles";
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Profile", "LoadUser", "", (long) (uids != null ? uids.size() : 0));
    }
}
