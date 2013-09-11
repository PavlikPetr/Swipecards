package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeleteRequest extends ApiRequest {
    private List<String> mIds;


    public DeleteRequest(List<String> ids, Context context) {
        super(context);
        mIds = ids;
    }

    public DeleteRequest(String id, Context context) {
        super(context);
        List<String> list = new ArrayList<String>();
        list.add(id);
        mIds = list;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        data.put("item", new JSONArray(mIds));
        return data;
    }

    @Override
    public String getServiceName() {
        return "feedDelete";
    }

    @Override
    public void exec() {
        if (mIds != null && mIds.size() > 0) {
            super.exec();
            EasyTracker.getTracker().sendEvent("Feed", "Delete", "", 1L);
        } else {
            handleFail(ApiResponse.ERRORS_PROCCESED, "User list for delete from black list is empty");
        }
    }
}
