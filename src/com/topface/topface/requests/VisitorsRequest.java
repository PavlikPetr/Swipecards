package com.topface.topface.requests;

import android.content.Context;
import com.topface.topface.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Реализация запроса к сервису visitors
 */
public class VisitorsRequest extends AbstractApiRequest {

    public static final int DEFAULT_LIMIT = 50;
    public static final int DEFAULT_TO = 3600 * 24 * 7;
    public static final boolean DEFAULT_LEAVE = true; //TODO: Убрать значение дебага

    private int mLimit;
    private int mTo;
    private boolean mLeave;

    public VisitorsRequest(Context context) {
        super(context);
        mLimit = DEFAULT_LIMIT;
        mTo = Utils.unixtime() + DEFAULT_TO;
        mLeave = DEFAULT_LEAVE;
    }

    public VisitorsRequest(int limit, int to, boolean leave, Context context) {
        super(context);
        mLimit = limit;
        mTo = to;
        mLeave = leave;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject request = new JSONObject();
        if (mTo > 0) {
            request.put("to", mTo);
        }
        return request
                .put("limit", mLimit)
                .put("leave", mLeave);
    }

    @Override
    protected String getServiceName() {
        return "visitors";
    }
}
