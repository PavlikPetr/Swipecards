package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class AlbumRequest extends ApiRequest {

    public static final String SERVICE_NAME = "photo.getList";

    public static final String MODE_ALBUM = "album";  // Фотографии в обратном порядке добавление
    public static final String MODE_SEARCH = "search"; // Первая фотка главная
    public static final String MODE_LEADER = "leader"; // Фотографии отсортированы по популярности

    public static final int DEFAULT_PHOTOS_LIMIT = 50;

    private int uid;
    private int limit;
    private int from;
    private String mode;

    public AlbumRequest(Context context, int uid, int limit, String mode) {
        super(context);
        this.uid = uid;
        this.limit = limit;
        this.mode = mode;
    }

    public AlbumRequest(Context context, int uid, int limit, int from, String mode) {
        super(context);
        this.uid = uid;
        this.limit = limit;
        this.from = from;
        this.mode = mode;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject response =  new JSONObject().put("limit", limit)
                .put("userId", uid);
        if (from != 0) {
            response.put("from", from);
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
