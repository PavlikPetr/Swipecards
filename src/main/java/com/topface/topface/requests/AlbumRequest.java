package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.AlbumLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

public class AlbumRequest extends LimitedApiRequest {

    public static final String SERVICE_NAME = "photo.getList";

    public static final String MODE_ALBUM = "album";  // Фотографии в обратном порядке добавление
    public static final String MODE_SEARCH = "search"; // Первая фотка главная
    public static final String MODE_LEADER = "leader"; // Фотографии отсортированы по популярности

    public static final int DEFAULT_PHOTOS_LIMIT = 50;

    private int mUid;
    private int mFrom;
    private String mMode;
    private int mType;


    public AlbumRequest(Context context, int uid, String mode, int type) {
        super(context);
        mUid = uid;
        mMode = mode;
        mType = type;
    }

    public AlbumRequest(Context context, int uid, int from, String mode, int type) {
        super(context);
        mUid = uid;
        mFrom = from;
        mMode = mode;
        mType = type;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject response = super.getRequestData()
                .put("userId", mUid);
        if (mFrom != 0) {
            response.put("from", mFrom);
        }
        if (mMode != null) {
            response.put("mode", mMode);
        }

        return response;

    }

    @Override
    protected LoadController getLoadController() {
        return new AlbumLoadController(mType);
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
