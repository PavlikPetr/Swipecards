package com.topface.topface.ui;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.topface.topface.App;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.IFreeProductsRequest;

/**
 * Created by ppetr on 20.07.15.
 * Base application for using multidex in i-free flavour
 */
public abstract class ApplicationBase extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static ApiRequest getProductsRequest() {
        return new IFreeProductsRequest(App.getContext());
    }
}