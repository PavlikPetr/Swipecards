package com.topface.topface.ui;

import android.app.Application;

import com.topface.topface.App;
import com.topface.topface.requests.AmazonProductsRequest;
import com.topface.topface.requests.ApiRequest;

/**
 * Created by ppetr on 22.07.15.
 * empty parrent for App
 */
public class ApplicationBase extends Application {

    public static ApiRequest getProductsRequest() {
        return new AmazonProductsRequest(App.getContext());
    }
}