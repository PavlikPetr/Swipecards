package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.App;
import com.topface.topface.data.Products;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.fragments.buy.PurchaseButtonList;

import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlayProductsRequest extends ApiRequest {
    private static final String SERVICE = "googleplay.getProducts";

    public GooglePlayProductsRequest(Context context) {
        super(context);
        doNeedAlert(false);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("acceptViews", PurchaseButtonList.getSupportedViews());
        return result;
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
