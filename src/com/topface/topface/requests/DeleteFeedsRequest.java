package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.requests.handlers.ErrorCodes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class DeleteFeedsRequest extends ApiRequest {

    protected List<String> mIds;

    public DeleteFeedsRequest(List<String> ids, Context context) {
        super(context);
        mIds = ids;
    }

    public DeleteFeedsRequest(String id, Context context) {
        super(context);
        List<String> list = new ArrayList<String>();
        list.add(id);
        mIds = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put(getKeyForItems(), new JSONArray(mIds));
        return data;
    }

    protected String getKeyForItems() {
        return "items";
    }

    @Override
    public abstract String getServiceName();

    @Override
    public void exec() {
        if (mIds != null && mIds.size() > 0) {
            super.exec();
            EasyTracker.getTracker().sendEvent("Feed", "Delete", getFeedType(), 1L);
        } else {
            handleFail(ErrorCodes.ERRORS_PROCCESED, "User list for delete from black list is empty");
        }
    }

    protected abstract String getFeedType();
}
