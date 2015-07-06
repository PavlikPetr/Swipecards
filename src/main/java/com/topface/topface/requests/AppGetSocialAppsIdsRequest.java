package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kirussell on 30/06/15.
 */
public class AppGetSocialAppsIdsRequest extends ApiRequest {

    public AppGetSocialAppsIdsRequest(Context context) {
        super(context);
    }

    @Override protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("clienttype", BuildConfig.MARKET_API_TYPE.getClientType());
    }

    @Override public String getServiceName() {
        return "app.getSocialApplications";
    }

    @Override
    public boolean isNeedAuth() {
        return false;
    }
}
