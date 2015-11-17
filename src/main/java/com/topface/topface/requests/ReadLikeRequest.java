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

    public ReadLikeRequest(Context context, int id, boolean interstitialShown) {
        super(context);
        ArrayList<Integer> array = new ArrayList<>();
        array.add(id);
        mIdArray = array;
        mInterstitialShown = interstitialShown;
    }

    public ReadLikeRequest(Context context, ArrayList<Integer> idArray, boolean interstitialShown) {
        super(context);
        mIdArray = idArray;
        mInterstitialShown = interstitialShown;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        for (int id : mIdArray) {
            jsonArray.put(id);
        }
        jsonObject.put("ids", jsonArray);
        jsonObject.put("interstitialShown", mInterstitialShown);
        return jsonObject;
    }

    @Override
    public void exec() {
        if (!isContainEmptuId()) {
            super.exec();
        } else {
            handleFail(ErrorCodes.ERRORS_PROCESSED, "Invalid id");
        }
    }

    private boolean isContainEmptuId() {
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
