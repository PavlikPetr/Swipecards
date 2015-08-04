package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.data.Products;

import org.json.JSONException;
import org.json.JSONObject;

public class IFreeProductsRequest extends ApiRequest {
    private static final String SERVICE = "ifree.getProducts";

    public IFreeProductsRequest(Context context) {
        super(context);
        doNeedAlert(false);
        callback(new DataApiHandler<Products>() {
            @Override
            public void fail(int codeError, IApiResponse response) {

            }

            @Override
            protected void success(Products data, IApiResponse response) {
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