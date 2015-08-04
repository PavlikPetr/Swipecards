package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.data.Products;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayProductsRequest extends ApiRequest {
    private static final String SERVICE = "googleplay.getProducts";

    public GooglePlayProductsRequest(Context context) {
        super(context);
        doNeedAlert(false);
        callback(new DataApiHandler<Products>() {
            @Override
            public void fail(int codeError, IApiResponse response) {

            }

            @Override
            protected void success(Products data, IApiResponse response) {
                App.getOpenIabHelperManager().updateInventory();
            }

            @Override
            protected Products parseResponse(ApiResponse response) {
                return new Products(response);
            }
        });
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }
}
