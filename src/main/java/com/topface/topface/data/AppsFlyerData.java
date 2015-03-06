package com.topface.topface.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    public JSONObject toJsonWithConversions(ConversionHolder holder) throws JSONException {
        JSONObject obj = toJson();
        if (holder != null) {
            for (String key : holder.keySet()) {
                obj.put(key, holder.get(key));
            }
        }
        return obj;
    }

    public static class ConversionHolder extends HashMap<String, String> {

    }

    public static class ConversionListener implements AppsFlyerConversionListener {

        ConversionHolder holder;

        public ConversionListener(ConversionHolder conversionHolder) {
            holder = conversionHolder;
        }

        @Override
        public void onInstallConversionDataLoaded(Map<String, String> stringStringMap) {
            holder.putAll(stringStringMap);
        }

        @Override
        public void onInstallConversionFailure(String s) {
        }

        @Override
        public void onAppOpenAttribution(Map<String, String> stringStringMap) {
        }
    }

}
