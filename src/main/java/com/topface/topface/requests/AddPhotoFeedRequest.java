package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.EasyTracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddPhotoFeedRequest extends ApiRequest {
    public static final String SERVICE_NAME = "photofeed.add";
    private int mPhotoId;
    private int mIdCount;
    private String mStatus;
    private long mPrice;

    public AddPhotoFeedRequest(int photoId, Context context, int count, String status, long price) {
        super(context);
        setData(photoId, count, status, price);
    }

    private void setData(int photoId, int count, String status, long price) {
        mPhotoId = photoId;
        mIdCount = count;
        mStatus = status;
        mPrice = price;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("status", mStatus);
        result.put("photos", createIdArray());
        return result;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.sendEvent("Leaders", "Buy", "", mPrice);
    }

    private JSONArray createIdArray() {
        JSONArray photoArray = new JSONArray();
        for (int i = 0; i < mIdCount; i++) {
            photoArray.put(mPhotoId);
        }
        return photoArray;
    }
}
