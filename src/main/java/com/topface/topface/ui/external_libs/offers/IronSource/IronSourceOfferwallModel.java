package com.topface.topface.ui.external_libs.offers.IronSource;

import com.google.gson.annotations.SerializedName;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class IronSourceOfferwallModel implements IOfferwallBaseModel {
    @SuppressWarnings("unused")
    @SerializedName("offerId")
    private int mOfferId;

    @SuppressWarnings("unused")
    @SerializedName("bundleId")
    private String mBundleId;

    @SuppressWarnings("unused")
    @SerializedName("rewards")
    private int mRewards;

    @SuppressWarnings("unused")
    @SerializedName("rewardsText")
    private String mRewardsText;

    @SuppressWarnings("unused")
    @SerializedName("disclaimer")
    private String mDisclaimer;

    @SuppressWarnings("unused")
    @SerializedName("userFlow")
    private String mUserFlow;

    @SuppressWarnings("unused")
    @SerializedName("callToAction")
    private String mCallToAction;

    @SuppressWarnings("unused")
    @SerializedName("description")
    private String mDescription;

    @SuppressWarnings("unused")
    @SerializedName("title")
    private String mTitle;

    @SuppressWarnings("unused")
    @SerializedName("creatives")
    private Creatives mCreatives;

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
        return mRewards;
    }

    @NotNull
    @Override
    public String getIconUrl() {
        return mCreatives != null
                && mCreatives.mAssets != null
                && mCreatives.mAssets.mIcon != null
                ? mCreatives.mAssets.mIcon.mUrl
                : Utils.EMPTY;
    }

    @NotNull
    @Override
    public String getLink() {
        return mCreatives != null ? mCreatives.mClickUrl : Utils.EMPTY;
    }

    @NotNull
    @OfferwallType
    @Override
    public String getOfferwallsType() {
        return IRON_SOURCE;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IronSourceOfferwallModel that = (IronSourceOfferwallModel) o;

        if (mOfferId != that.mOfferId) return false;
        if (mRewards != that.mRewards) return false;
        if (mBundleId != null ? !mBundleId.equals(that.mBundleId) : that.mBundleId != null)
            return false;
        if (mRewardsText != null ? !mRewardsText.equals(that.mRewardsText) : that.mRewardsText != null)
            return false;
        if (mDisclaimer != null ? !mDisclaimer.equals(that.mDisclaimer) : that.mDisclaimer != null)
            return false;
        if (mUserFlow != null ? !mUserFlow.equals(that.mUserFlow) : that.mUserFlow != null)
            return false;
        if (mCallToAction != null ? !mCallToAction.equals(that.mCallToAction) : that.mCallToAction != null)
            return false;
        if (mDescription != null ? !mDescription.equals(that.mDescription) : that.mDescription != null)
            return false;
        if (mTitle != null ? !mTitle.equals(that.mTitle) : that.mTitle != null) return false;
        return mCreatives != null ? mCreatives.equals(that.mCreatives) : that.mCreatives == null;

    }

    @Override
    public int hashCode() {
        int result = mOfferId;
        result = 31 * result + (mBundleId != null ? mBundleId.hashCode() : 0);
        result = 31 * result + mRewards;
        result = 31 * result + (mRewardsText != null ? mRewardsText.hashCode() : 0);
        result = 31 * result + (mDisclaimer != null ? mDisclaimer.hashCode() : 0);
        result = 31 * result + (mUserFlow != null ? mUserFlow.hashCode() : 0);
        result = 31 * result + (mCallToAction != null ? mCallToAction.hashCode() : 0);
        result = 31 * result + (mDescription != null ? mDescription.hashCode() : 0);
        result = 31 * result + (mTitle != null ? mTitle.hashCode() : 0);
        result = 31 * result + (mCreatives != null ? mCreatives.hashCode() : 0);
        return result;
    }

    @SuppressWarnings("unused")
    private class Creatives {
        @SerializedName("creativeId")
        private String mCreativeId;
        @SerializedName("clickUrl")
        private String mClickUrl;
        @SerializedName("Assets")
        private Assets mAssets;

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Creatives creatives = (Creatives) o;

            if (mCreativeId != null ? !mCreativeId.equals(creatives.mCreativeId) : creatives.mCreativeId != null)
                return false;
            if (mClickUrl != null ? !mClickUrl.equals(creatives.mClickUrl) : creatives.mClickUrl != null)
                return false;
            return mAssets != null ? mAssets.equals(creatives.mAssets) : creatives.mAssets == null;

        }

        @Override
        public int hashCode() {
            int result = mCreativeId != null ? mCreativeId.hashCode() : 0;
            result = 31 * result + (mClickUrl != null ? mClickUrl.hashCode() : 0);
            result = 31 * result + (mAssets != null ? mAssets.hashCode() : 0);
            return result;
        }
    }

    @SuppressWarnings("unused")
    private class Assets {
        @SerializedName("Icon")
        private Image mIcon;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Assets assets = (Assets) o;

            return mIcon != null ? mIcon.equals(assets.mIcon) : assets.mIcon == null;

        }

        @Override
        public int hashCode() {
            return mIcon != null ? mIcon.hashCode() : 0;
        }
    }

    @SuppressWarnings("unused")
    private class Image {
        @SerializedName("url")
        private String mUrl;

        @SerializedName("width")
        private int mWidth;

        @SerializedName("height")
        private int mHeight;

        @SuppressWarnings("SimplifiableIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Image image = (Image) o;

            if (mWidth != image.mWidth) return false;
            if (mHeight != image.mHeight) return false;
            return mUrl != null ? mUrl.equals(image.mUrl) : image.mUrl == null;

        }

        @Override
        public int hashCode() {
            int result = mUrl != null ? mUrl.hashCode() : 0;
            result = 31 * result + mWidth;
            result = 31 * result + mHeight;
            return result;
        }
    }
}
