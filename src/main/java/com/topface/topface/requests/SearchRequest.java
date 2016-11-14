package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.DatingLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchRequest extends LimitedApiRequest {
    private static final int SEARCH_LIMIT = 30;
    // Data
    public static final String SERVICE_NAME = "search.getList";
    private boolean mWithGifts;
    private boolean mIsNeedRefresh;
    private boolean mOnlyOnline = false; // необходимость выборки только онлайн-пользователей
    private boolean mWithForm = false; // полная анкета

    private SearchRequest(int limit, boolean onlyOnline, Context context, boolean isNeedRefresh,
                          boolean withForm, boolean withGifts) {
        super(context);
        mOnlyOnline = onlyOnline;
        mIsNeedRefresh = isNeedRefresh;
        mWithForm = withForm;
        mWithGifts = withGifts;
    }

    public SearchRequest(boolean onlyOnline, Context context, boolean isRefresh, boolean withForm, boolean withGifts) {
        this(SEARCH_LIMIT, onlyOnline, context, isRefresh, withForm, withGifts);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return super.getRequestData()
                .put("online", mOnlyOnline)
                .put("refresh", mIsNeedRefresh)
                .put("withForm", mWithForm)
                .put("withGifts", mWithGifts);
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
