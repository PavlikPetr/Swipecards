package com.topface.topface.ui;

import android.app.Application;

import com.topface.topface.App;
import com.topface.topface.data.Products;
import com.topface.topface.modules.TopfaceModule;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GooglePlayProductsRequest;
import com.topface.topface.requests.IApiResponse;

/**
 * Created by ppetr on 22.07.15.
 * empty parent for App
 */
public class ApplicationBase extends Application {

    public static ApiRequest getProductsRequest() {
        GooglePlayProductsRequest request = new GooglePlayProductsRequest(App.getContext());
        request.callback(new DataApiHandler<Products>() {
            @Override
            protected void success(Products data, IApiResponse response) {

            }

            @Override
            protected Products parseResponse(ApiResponse response) {
                return new Products(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {

            }
        });
        return request;
    }

    public Object[] getDaggerModules() {
        return new Object[]{new TopfaceModule()};
    }
}