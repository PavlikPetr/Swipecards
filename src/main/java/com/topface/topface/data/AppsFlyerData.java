package com.topface.topface.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.appsflyer.AppsFlyerLib;

import org.json.JSONException;
import org.json.JSONObject;

public class AppsFlyerData implements SerializableToJson {
    private final String mHash;
    private final String mReferrer;
    private Context mContext;

    public AppsFlyerData(Context context) {
        mContext = context;
        mHash = AppsFlyerLib.getAppsFlyerUID(context);
        mReferrer = getReferrer();
    }

    private String getReferrer() {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("appsflyer-data", Context.MODE_PRIVATE);
        return sharedPreferences.getString("referrer", "");
    }

    @Override
    public JSONObject toJson() throws JSONException {
        return new JSONObject()
                .put("hash", mHash)
                .put("referrer", mReferrer);
    }

    @Override
    public void fromJSON(String json) {

    }

}
