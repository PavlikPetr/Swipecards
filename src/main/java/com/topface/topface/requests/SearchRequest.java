package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.DatingLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequest extends LimitedApiRequest {
    public static final int SEARCH_LIMIT = 30;
    // Data
    public static final String SERVICE_NAME = "search.getList";
    private boolean onlyOnline = false; // необходимость выборки только онлайн-пользователей

    public SearchRequest(int limit, boolean onlyOnline, Context context) {
        super(context);
        this.onlyOnline = onlyOnline;
    }

    public SearchRequest(boolean onlyOnline, Context context) {
        this(SEARCH_LIMIT, onlyOnline, context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return super.getRequestData()
                .put("online", onlyOnline);
    }

    @Override
    protected LoadController getLoadController() {
        return new DatingLoadController();
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
