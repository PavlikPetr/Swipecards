package com.topface.topface.utils;

import android.content.Context;

public abstract class BaseMarketApiManager {
    public abstract void onResume();

    public abstract int getResultCode();

    public abstract void onProblemResolve(Context context);

    public abstract int getButtonTextId();

    public abstract boolean isButtonVisible();

    public abstract boolean isTitleVisible();

    public abstract int getTitleTextId();

    public abstract boolean isMarketApiAvailable();

    public abstract boolean isMarketApiSupportByUs();
}
