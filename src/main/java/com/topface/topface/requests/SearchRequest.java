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
    private boolean mIsNeedRefresh;
    private boolean mOnlyOnline = false; // необходимость выборки только онлайн-пользователей

    public SearchRequest(int limit, boolean onlyOnline, Context context, boolean isNeedRefresh) {
        super(context);
        mOnlyOnline = onlyOnline;
        mIsNeedRefresh = isNeedRefresh;
    }

    public SearchRequest(boolean onlyOnline, Context context, boolean isRefresh) {
        this(SEARCH_LIMIT, onlyOnline, context, isRefresh);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return super.getRequestData()
                .put("online", mOnlyOnline)
                .put("refresh", mIsNeedRefresh);
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
