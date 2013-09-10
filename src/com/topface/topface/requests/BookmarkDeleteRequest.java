package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookmarkDeleteRequest extends ApiRequest {
    public static String SERVICE_NAME = "bookmarkdelete";

    private final List<Integer> uids;

    public BookmarkDeleteRequest(Context context, List<Integer> uids) {
        super(context);
        this.uids = uids;
    }

    public BookmarkDeleteRequest(Context context, int uid) {
        super(context);
        List<Integer> list = new ArrayList<Integer>();
        list.add(uid);
        this.uids = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userid", new JSONArray(this.uids));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}