package com.topface.topface.ui.external_libs.offers.Fyber;


import com.google.gson.annotations.SerializedName;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;

import org.jetbrains.annotations.NotNull;

public class FyberOfferwallModel implements IOfferwallBaseModel {
    @SuppressWarnings("unused")
    @SerializedName("title")
    private String mTitle;

    @SuppressWarnings("unused")
    @SerializedName("teaser")
    private String mDescription;

    @SuppressWarnings("unused")
    @SerializedName("payout")
    private int mPayout;

    @SuppressWarnings("unused")
    @SerializedName("thumbnail")
    private IconLinks mIconLinks;

    @SuppressWarnings("unused")
    @SerializedName("link")
    private String mLink;

    @NotNull
    @Override
    public String getTitle() {
        return mTitle;
    }

    @NotNull
    @Override
    public String getDescription() {
        return mDescription;
    }

    @Override
    public int getRewardValue() {
        return mPayout;
    }

    @NotNull
    @Override
    public String getIconUrl() {
        return mIconLinks.mLowRes;
    }

    @NotNull
    @Override
    public String getLink() {
        return mLink;
    }

    @NotNull
    @OfferwallType
    @Override
    public String getOfferwallsType() {
        return FYBER;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FyberOfferwallModel)) return false;
        FyberOfferwallModel data = (FyberOfferwallModel) o;
        if (mTitle == null || !mTitle.equals(data.mTitle)) return false;
        if (mDescription == null || !mDescription.equals(data.mDescription)) return false;
        if (mPayout != data.mPayout) return false;
        if (mIconLinks == null || !mIconLinks.equals(data.mIconLinks)) return false;
        return mLink != null && mLink.equals(data.mLink);
    }

    @Override
    public int hashCode() {
        int res = 0;
        res = (res * 31) + (mTitle != null ? mTitle.hashCode() : 0);
        res = (res * 31) + (mDescription != null ? mDescription.hashCode() : 0);
        res = (res * 31) + mPayout;
        res = (res * 31) + (mIconLinks != null ? mIconLinks.hashCode() : 0);
        res = (res * 31) + (mLink != null ? mLink.hashCode() : 0);
        return res;
    }

    private class IconLinks {
        @SuppressWarnings("unused")
        @SerializedName("lowres")
        private String mLowRes;

        @SuppressWarnings("unused")
        @SerializedName("hires")
        private String mHiRes;

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof IconLinks)) return false;
            IconLinks data = (IconLinks) o;
            if (mLowRes == null || !mLowRes.equals(data.mLowRes)) return false;
            return mHiRes != null && mHiRes.equals(data.mHiRes);
        }

        @Override
        public int hashCode() {
            int res = 0;
            res = (res * 31) + (mLowRes != null ? mLowRes.hashCode() : 0);
            res = (res * 31) + (mHiRes != null ? mHiRes.hashCode() : 0);
            return res;
        }
    }
}
