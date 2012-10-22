package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UserRequest extends AbstractApiRequest {
    private ArrayList<Integer> uids; // массив id пользователя в топфейсе
    private ArrayList<String> fields; // массив интересующих полей профиля
    public Boolean visitor = true; // флаг определения текущего пользователя посетителем профилей запрошенных пользователей

    public UserRequest(ArrayList<Integer> uids, ArrayList<String> fields, Context context) {
        super(context);
        if (uids == null || uids.size() < 1) {
            throw new NullPointerException();
        }
        this.uids = uids;

        if (fields != null && fields.size() > 0) {
            this.fields = fields;
        }
        doNeedAlert(false);
    }

    public UserRequest(ArrayList<Integer> uids, Context context) {
        this(uids, null, context);

    }

    public UserRequest(int uid, Context context) {
        super(context);
        ArrayList<Integer> data = new ArrayList<Integer>();
        data.add(uid);
        this.uids = data;
        doNeedAlert(false);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject()
                .put("uids", new JSONArray(uids))
                .put("visitor", visitor);

        if (fields != null) {
            data.put("fields", fields);
        }

        return data;
    }

    @Override
    protected String getServiceName() {
        return "profiles";
    }
}
