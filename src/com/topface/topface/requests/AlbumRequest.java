package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumRequest extends AbstractApiRequest{

    public static final String SERVICE_NAME = "album";

    public static final int DEFAULT_PHOTOS_LIMIT = 50;

    private int uid;
    private int limit;
    private int to;
    private boolean firstmain;

    public AlbumRequest(Context context, int uid, int limit) {
        super(context);
        this.uid = uid;
        this.limit = limit;
    }

    public AlbumRequest(Context context, int uid, int limit, int to, boolean  firstmain) {
        super(context);
        this.uid = uid;
        this.limit = limit;
        this.to = to;
        this.firstmain = firstmain;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {

        return new JSONObject().put("limit", limit)
                .put("userid", uid)
                .put("to", to)
                .put("firstmain", firstmain);

    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
