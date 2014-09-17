package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.loadcontollers.LoadController;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ilya on 12.09.14.
 */
public abstract class LimitedApiRequest extends ApiRequest {
    protected int mLimit;

    public LimitedApiRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject object = new JSONObject();
        mLimit = getLoadController().getItemsLimitByConnectionType();
        return object.put("limit", mLimit);
    }

    protected abstract LoadController getLoadController();
}
