package com.topface.topface.ui.external_libs.offers.Fyber;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class FyberOffersResponse {
    @SuppressWarnings("unused")
    @SerializedName("code")
    private String mCode;

    @SuppressWarnings("unused")
    @SerializedName("message")
    private String mMessage;

    @SuppressWarnings("unused")
    @SerializedName("count")
    private int mCount;

    @SuppressWarnings("unused")
    @SerializedName("pages")
    private int mPages;

    @SuppressWarnings("unused")
    @SerializedName("information")
    private Information mInformation;

    @SuppressWarnings("unused")
    @SerializedName("offers")
    private ArrayList<FyberOfferwallModel> mOffers;

    public String getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
    }

    public int getCount() {
        return mCount;
    }

    public int getPages() {
        return mPages;
    }

    public Information getInformation() {
        return mInformation;
    }

    public ArrayList<FyberOfferwallModel> getOffers() {
        return mOffers;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FyberOffersResponse that = (FyberOffersResponse) o;

        if (mCount != that.mCount) return false;
        if (mPages != that.mPages) return false;
        if (mCode != null ? !mCode.equals(that.mCode) : that.mCode != null) return false;
        if (mMessage != null ? !mMessage.equals(that.mMessage) : that.mMessage != null)
            return false;
        if (mInformation != null ? !mInformation.equals(that.mInformation) : that.mInformation != null)
            return false;
        return mOffers != null ? mOffers.equals(that.mOffers) : that.mOffers == null;

    }

    @Override
    public int hashCode() {
        int result = mCode != null ? mCode.hashCode() : 0;
        result = 31 * result + (mMessage != null ? mMessage.hashCode() : 0);
        result = 31 * result + mCount;
        result = 31 * result + mPages;
        result = 31 * result + (mInformation != null ? mInformation.hashCode() : 0);
        result = 31 * result + (mOffers != null ? mOffers.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("unused")
    public static class Information {
        @SerializedName("app_name")
        private String mAppName;

        @SerializedName("virtual_currency")
        private String mVirtualCurrency;

        @SerializedName("appid")
        private int mAppId;

        @SerializedName("country")
        private String mCountry;

        @SerializedName("language")
        private String mLanguage;

        @SerializedName("support_url")
        private String mSupportUrl;

        public String getAppNAme() {
            return mAppName;
        }

        public String getVirtualCurrency() {
            return mVirtualCurrency;
        }

        public String getCountry() {
            return mCountry;
        }

        public String getLanguage() {
            return mLanguage;
        }

        public String getSupportUrl() {
            return mSupportUrl;
        }

        public int getAppID() {
            return mAppId;
        }

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Information that = (Information) o;

            if (mAppId != that.mAppId) return false;
            if (mAppName != null ? !mAppName.equals(that.mAppName) : that.mAppName != null)
                return false;
            if (mVirtualCurrency != null ? !mVirtualCurrency.equals(that.mVirtualCurrency) : that.mVirtualCurrency != null)
                return false;
            if (mCountry != null ? !mCountry.equals(that.mCountry) : that.mCountry != null)
                return false;
            if (mLanguage != null ? !mLanguage.equals(that.mLanguage) : that.mLanguage != null)
                return false;
            return mSupportUrl != null ? mSupportUrl.equals(that.mSupportUrl) : that.mSupportUrl == null;

        }

        @Override
        public int hashCode() {
            int result = mAppName != null ? mAppName.hashCode() : 0;
            result = 31 * result + (mVirtualCurrency != null ? mVirtualCurrency.hashCode() : 0);
            result = 31 * result + mAppId;
            result = 31 * result + (mCountry != null ? mCountry.hashCode() : 0);
            result = 31 * result + (mLanguage != null ? mLanguage.hashCode() : 0);
            result = 31 * result + (mSupportUrl != null ? mSupportUrl.hashCode() : 0);
            return result;
        }
    }
}
