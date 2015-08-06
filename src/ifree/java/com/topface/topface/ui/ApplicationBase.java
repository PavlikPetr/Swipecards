package com.topface.topface.ui;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.topface.topface.App;
import com.topface.topface.data.Products;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.IFreeProductsRequest;

/**
 * Created by ppetr on 20.07.15.
 * Base application for using multidex in i-free flavour
 */
public abstract class ApplicationBase extends Application {

    public static ApiRequest getProductsRequest() {
        IFreeProductsRequest request = new IFreeProductsRequest(App.getContext());
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