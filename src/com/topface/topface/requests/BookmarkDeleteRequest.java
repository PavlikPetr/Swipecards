package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 08.04.13
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class BookmarkDeleteRequest extends ApiRequest {
    public static String SERVICE_NAME = "bookmarkdelete";

    int uid;

    public BookmarkDeleteRequest(Context context, int uid) {
        super(context);
        this.uid = uid;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("userid", uid);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
