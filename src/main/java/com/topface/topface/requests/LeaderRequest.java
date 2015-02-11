package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LeaderRequest extends ApiRequest {
    public static final String SERVICE_NAME = "leader.become";
    private int mPhotoId;
    private int mIdCount;
    private String mStatus;
    private long mPrice;

    public LeaderRequest(int photoId, Context context) {
        super(context);
        setData(photoId, 1, "", (long) CacheProfile.getOptions().priceLeader);
    }

    public LeaderRequest(int photoId, Context context, int count, String status, long price) {
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
        if (!TextUtils.isEmpty(mStatus)) {
            result.put("status", mStatus);
        }
        if (mIdCount == 1) {
            result.put("photo", mPhotoId);
        } else {
            result.put("photo", createIdArray());
        }
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

    private List<Integer> createIdArray() {
        return new ArrayList<>();
    }
}
