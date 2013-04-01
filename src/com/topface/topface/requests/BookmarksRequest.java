package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 01.04.13
 * Time: 18:28
 * To change this template use File | Settings | File Templates.
 */
public class BookmarksRequest extends ApiRequest {
    public static String SERVICE_NAME = "bookmarks";

    private int limit;
    private String from;
    private String to;
    private int type = 1;

    public BookmarksRequest(Context context, int limit) {
        super(context);
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo (String to) {
        this.to = to;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("limit", limit);
        data.put("type", type);

        if (from != null) {
            data.put("from", from);
        }

        if (to != null) {
            data.put("to", to);
        }

        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
