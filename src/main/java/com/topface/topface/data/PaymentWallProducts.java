package com.topface.topface.data;

import com.topface.topface.requests.IApiResponse;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

public class PaymentWallProducts extends Products {
    public enum TYPE {DIRECT, MOBILE}
    private TYPE mType;

    public PaymentWallProducts(@Nullable JSONObject data) {
        super(data);
    }

    public PaymentWallProducts(@NotNull IApiResponse data, TYPE type) {
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
