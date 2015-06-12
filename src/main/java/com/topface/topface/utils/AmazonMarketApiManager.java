package com.topface.topface.utils;

import android.app.Activity;

import com.topface.topface.R;

public class AmazonMarketApiManager extends BaseMarketApiManager {

    public AmazonMarketApiManager() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public boolean isMarketApiAvailable() {
        return false;
    }

    @Override
    public boolean isMarketApiSupportByUs() {
        return false;
    }

    @Override
    public int getResultCode() {
        return 0;
    }

    @Override
    public void onProblemResolve(Activity activity) {
    }

    @Override
    public int getButtonTextId() {
        return 0;
    }

    @Override
    public boolean isButtonVisible() {
        return false;
    }

    @Override
    public boolean isTitleVisible() {
        return true;
    }

    @Override
    public int getTitleTextId() {
        return R.string.google_unavailable_services_title;
    }
}

