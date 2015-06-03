package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReadLikeRequest extends ApiRequest {

    private int mId;
    public final static String SERVICE_NAME = "like.read";
    private boolean mInterstitialShown;

    public ReadLikeRequest(Context context, int id, boolean interstitialShown) {
        super(context);
        mId = id;
        mInterstitialShown = interstitialShown;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonArray.put(mId);
        jsonObject.put("ids", jsonArray);
        jsonObject.put("interstitialShown", mInterstitialShown);
        return jsonObject;
    }

    @Override
    public void exec() {
        if (mId > 0) {
            super.exec();
        } else {
            handleFail(ErrorCodes.ERRORS_PROCESSED, "Invalid id");
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
