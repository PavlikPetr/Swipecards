package com.topface.topface.ui;

import android.app.Application;

import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.data.Products;
import com.topface.topface.requests.AmazonProductsRequest;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GooglePlayProductsRequest;
import com.topface.topface.requests.IApiResponse;

/**
 * Created by ppetr on 22.07.15.
 * empty parrent for App
 */
public class ApplicationBase extends Application {

    public static ApiRequest getProductsRequest() {
        ApiRequest request;
        switch (BuildConfig.MARKET_API_TYPE) {
            case AMAZON:
                request = new AmazonProductsRequest(App.getContext());
                break;
            case GOOGLE_PLAY:
                request = new GooglePlayProductsRequest(App.getContext());
                break;
            case NOKIA_STORE:
            default:
                request = null;
                break;
        }

        if (request != null) {
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
        }

        return request;
    }
}