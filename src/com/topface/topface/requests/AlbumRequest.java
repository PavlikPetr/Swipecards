package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class AlbumRequest extends AbstractApiRequest{

    public static final String SERVICE_NAME = "album";

    public static final String MODE_ALBUM = "album";  // Фотографии в обратном порядке добавление
    public static final String MODE_SEARCH = "search"; // Первая фотка главная
    public static final String MODE_LEADER = "leader"; // Фотографии отсортированы по популярности

    public static final int DEFAULT_PHOTOS_LIMIT = 50;

    private int uid;
    private int limit;
    private int to;
    private String mode;

    public AlbumRequest(Context context, int uid, int limit, String mode) {
        super(context);
        this.uid = uid;
        this.limit = limit;
        this.mode = mode;
    }

    public AlbumRequest(Context context, int uid, int limit) {
        super(context);
        this.uid = uid;
        this.limit = limit;
    }

    public AlbumRequest(Context context, int uid, int limit, int to, String mode) {
        super(context);
        this.uid = uid;
        this.limit = limit;
        this.to = to;
        this.mode = mode;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject response =  new JSONObject().put("limit", limit)
                .put("userid", uid);
        if (to != 0) {
            response.put("to", to);
        }
        if (mode != null) {
            response.put("mode", mode);
        }

        return response;

    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
