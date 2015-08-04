package com.topface.topface.ui;

import android.app.Application;

import com.topface.topface.App;
import com.topface.topface.data.Products;
import com.topface.topface.requests.AmazonProductsRequest;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;

/**
 * Created by ppetr on 22.07.15.
 * empty parrent for App
 */
public class ApplicationBase extends Application {

    public static ApiRequest getProductsRequest() {
        AmazonProductsRequest request = new AmazonProductsRequest(App.getContext());
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
}