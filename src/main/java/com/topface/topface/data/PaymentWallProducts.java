package com.topface.topface.data;

import com.topface.topface.requests.IApiResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class PaymentWallProducts extends Products {
    public PaymentWallProducts(@NotNull IApiResponse data) {
        super(data);
    }

    @Override
    protected void updateCache(JSONObject data) {
        super.updateCache(data);

    }
}
