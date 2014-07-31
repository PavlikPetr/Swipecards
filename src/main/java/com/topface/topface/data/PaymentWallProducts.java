package com.topface.topface.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;

import org.json.JSONObject;

public class PaymentWallProducts extends Products {
    public enum TYPE {DIRECT, MOBILE}
    private TYPE mType;

    public PaymentWallProducts(@Nullable JSONObject data, TYPE type) {
        super();
        mType = type;
        fillData(data);
    }

    public PaymentWallProducts(@NonNull IApiResponse data, TYPE type) {
        super();
        mType = type;
        fillData(data.getJsonResult());
    }

    @Override
    protected void fillData(JSONObject data) {
        JSONObject object = null;
        switch (mType) {
            case DIRECT:
                object = data.optJSONObject("direct");
                break;
            case MOBILE:
                object = data.optJSONObject("mobile");
                break;
        }
        super.fillData(object);
    }

    @Override
    protected void updateCache(JSONObject data) {
        CacheProfile.setPaymentwallProducts(this, data, mType);
    }
}
