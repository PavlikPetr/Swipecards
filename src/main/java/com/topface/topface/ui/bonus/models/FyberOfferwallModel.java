package com.topface.topface.ui.bonus.models;


import com.google.gson.annotations.SerializedName;

public class FyberOfferwallModel extends OfferwallBaseModel {
    @SerializedName("title")
    private String mTitle;

    @SerializedName("teaser")
    private String mDescription;

    @SerializedName("payout")
    private int mPayout;

    @SerializedName("thumbnail")
    private IconLinks mIconLinks;

    @SerializedName("link")
    private String mLink;

    @Override
    String getTitle() {
        return mTitle;
    }

    @Override
    String getDescription() {
        return mDescription;
    }

    @Override
    int getRewardValue() {
        return mPayout;
    }

    @Override
    String getIconUrl() {
        return mIconLinks.mLowRes;
    }

    @Override
    String getLink() {
        return mLink;
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
        @SerializedName("lowres")
        private String mLowRes;

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
