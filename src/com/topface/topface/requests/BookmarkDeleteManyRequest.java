package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookmarkDeleteManyRequest extends ApiRequest {
    public static String SERVICE_NAME = "bookmark.delete";

    private final List<Integer> userIds;

    public BookmarkDeleteManyRequest(Context context, List<Integer> uids) {
        super(context);
        this.userIds = uids;
    }

    public BookmarkDeleteManyRequest(Context context, int uid) {
        super(context);
        List<Integer> list = new ArrayList<Integer>();
        list.add(uid);
        this.userIds = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userids", new JSONArray(this.userIds));
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}