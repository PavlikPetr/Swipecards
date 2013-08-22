package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookmarkDeleteManyRequest extends ApiRequest {
    public static String SERVICE_NAME = "bookmarkDeleteMany";

    private final List<Integer> uids;

    public BookmarkDeleteManyRequest(Context context, List<Integer> uids) {
        super(context);
        this.uids = uids;
    }

    public BookmarkDeleteManyRequest(Context context, int uid) {
        super(context);
        List<Integer> list = new ArrayList<Integer>();
        list.add(uid);
        this.uids = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userids", new JSONArray(this.uids));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}