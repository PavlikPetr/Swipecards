package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.requests.handlers.ErrorCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ReadLikeRequest extends ApiRequest {

    private ArrayList<Integer> mIdArray;
    public final static String SERVICE_NAME = "like.read";
    private boolean mInterstitialShown;
    private int mSenderId;

    public ReadLikeRequest(Context context, int senderId, boolean interstitialShown) {
        super(context);
        mSenderId = senderId;
        mInterstitialShown = interstitialShown;
    }

    public ReadLikeRequest(Context context, ArrayList<Integer> idArray, boolean interstitialShown) {
        super(context);
        mIdArray = idArray;
        mInterstitialShown = interstitialShown;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        if (mIdArray == null && mSenderId > 0) {
            jsonObject.put("senderId", mSenderId);
        } else {
            JSONArray jsonArray = new JSONArray();
            for (int id : mIdArray) {
                jsonArray.put(id);
            }
            jsonObject.put("ids", jsonArray);
            jsonObject.put("interstitialShown", mInterstitialShown);
        }
        return jsonObject;
    }

    @Override
    public void exec() {
        if (!isContainEmptyId() || mSenderId != 0) {
            super.exec();
        } else {
            handleFail(ErrorCodes.ERRORS_PROCESSED, "Invalid id");
        }
    }

    private boolean isContainEmptyId() {
        if (mIdArray == null || mIdArray.size() == 0) {
            return true;
        }
        for (int id : mIdArray) {
            if (id <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
