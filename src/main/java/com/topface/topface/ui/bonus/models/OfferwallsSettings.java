package com.topface.topface.ui.bonus.models;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppavlik on 17.06.16.
 * Common offerwals settings
 */

public class OfferwallsSettings {
    @SerializedName("coinsForVideoAd")
    private int mCoinsForVideoAd;
    @SerializedName("offerwalls")
    private List<String> mOfferwalls = new ArrayList<>();
    @SerializedName("videoOfferwalls")
    private List<String> mVideoOfferwalls = new ArrayList<>();

    public OfferwallsSettings() {
    }

    public int getForVideoAdCoinsCount() {
        return mCoinsForVideoAd;
    }

    @NotNull
    public List<String> getOfferwallsList() {
        return mOfferwalls;
    }

    @NotNull
    public List<String> getVideoOfferwallsList() {
        return mVideoOfferwalls;
    }

    public boolean isEnable() {
        /**
         * TODO
         *    до ввода поодержки rewarded video считаем, что эта настройка всегда выключена.
         *    с добавлением видео-функционала добавить в условие mCoinsForVideoAd>0
         *    && mVideoOfferwalls.size() > 0
         */
        return mOfferwalls.size() > 0;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OfferwallsSettings)) return false;
        OfferwallsSettings data = (OfferwallsSettings) o;
        if (mCoinsForVideoAd != data.getForVideoAdCoinsCount()) return false;
        if (!mOfferwalls.equals(data.getOfferwallsList())) return false;
        return mVideoOfferwalls.equals(data.getVideoOfferwallsList());
    }

    @Override
    public int hashCode() {
        int res = mCoinsForVideoAd;
        res = (res * 31) + mOfferwalls.hashCode();
        return (res * 31) + mVideoOfferwalls.hashCode();
    }
}