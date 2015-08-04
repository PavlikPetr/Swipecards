package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.data.Products;
import com.topface.topface.requests.handlers.ApiHandler;

import org.json.JSONException;
import org.json.JSONObject;

public class AmazonProductsRequest extends ApiRequest {
    private static final String SERVICE = "amazon.getProducts";

    public AmazonProductsRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public ApiRequest callback(ApiHandler handler) {
        return super.callback(new ApiHandlerWrapper<Products>(handler) {
            @Override
            protected void success(Products data, IApiResponse response) {
                App.getOpenIabHelperManager().updateInventory();
                super.success(data, response);
            }
        });
    }
}
