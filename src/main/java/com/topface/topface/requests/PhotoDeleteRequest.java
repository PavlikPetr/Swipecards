package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.EasyTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PhotoDeleteRequest extends ApiRequest {
    // Data
    public static final String service = "photo.delete";
    //TODO передалть на объекты Photo
    public int[] photos;

    public PhotoDeleteRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("photoIds", getPhotosJson());
    }

    @Override
    public String getServiceName() {
        return service;
    }

    private JSONArray getPhotosJson() throws JSONException {
        JSONArray photosJson = new JSONArray();
        for (int photo : photos) {
            photosJson.put(photo);
        }
        return photosJson;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.sendEvent("Profile", "PhotoDelete", "", 1L);
    }
}
