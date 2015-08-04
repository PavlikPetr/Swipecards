package com.topface.topface.ui;

import android.app.Application;

import com.topface.topface.App;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.GooglePlayProductsRequest;

/**
 * Created by ppetr on 22.07.15.
 * empty parent for App
 */
public class ApplicationBase extends Application {

    public static ApiRequest getProductsRequest() {
        return new GooglePlayProductsRequest(App.getContext());
    }
}