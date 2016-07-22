package com.topface.topface.ui.external_libs.offers.IronSource;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class IronSourceOffersResponse {
    @SerializedName("response")
    public Response data;

    public Response getResponse() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IronSourceOffersResponse that = (IronSourceOffersResponse) o;

        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    public static class Response {
        @SuppressWarnings("unused")
        @SerializedName("errorCode")
        private int mErrorCode;

        @SuppressWarnings("unused")
        @SerializedName("page")
        private int mPagesCount;

        @SuppressWarnings("unused")
        @SerializedName("items")
        private int mItemsCount;

        @SuppressWarnings("unused")
        @SerializedName("total")
        private int mTotalCount;

        @SuppressWarnings("unused")
        @SerializedName("errorMessage")
        private String mErrorMessage;

        @SuppressWarnings("unused")
        @SerializedName("offers")
        private ArrayList<IronSourceOfferwallModel> mOffers;

        @SuppressWarnings("unused")
        @SerializedName("generalInformation")
        private Info mGeneralInfo;

        public ArrayList<IronSourceOfferwallModel> getOffers() {
            return mOffers;
        }

        public static class Info {
            public String applicationName;
            public String currencyName;
            public int totalEarnedRewards;
            public String totalEarnedRewardsText;
            public String statusPageUrl;
            public String countryCode;
            public String readingOrder;
            public String languageCode;

            @SuppressWarnings("SimplifiableIfStatement")
            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Info info = (Info) o;

                if (totalEarnedRewards != info.totalEarnedRewards) return false;
                if (applicationName != null ? !applicationName.equals(info.applicationName) : info.applicationName != null)
                    return false;
                if (currencyName != null ? !currencyName.equals(info.currencyName) : info.currencyName != null)
                    return false;
                if (totalEarnedRewardsText != null ? !totalEarnedRewardsText.equals(info.totalEarnedRewardsText) : info.totalEarnedRewardsText != null)
                    return false;
                if (statusPageUrl != null ? !statusPageUrl.equals(info.statusPageUrl) : info.statusPageUrl != null)
                    return false;
                if (countryCode != null ? !countryCode.equals(info.countryCode) : info.countryCode != null)
                    return false;
                if (readingOrder != null ? !readingOrder.equals(info.readingOrder) : info.readingOrder != null)
                    return false;
                return languageCode != null ? languageCode.equals(info.languageCode) : info.languageCode == null;

            }

            @Override
            public int hashCode() {
                int result = applicationName != null ? applicationName.hashCode() : 0;
                result = 31 * result + (currencyName != null ? currencyName.hashCode() : 0);
                result = 31 * result + totalEarnedRewards;
                result = 31 * result + (totalEarnedRewardsText != null ? totalEarnedRewardsText.hashCode() : 0);
                result = 31 * result + (statusPageUrl != null ? statusPageUrl.hashCode() : 0);
                result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
                result = 31 * result + (readingOrder != null ? readingOrder.hashCode() : 0);
                result = 31 * result + (languageCode != null ? languageCode.hashCode() : 0);
                return result;
            }
        }
    }
}
