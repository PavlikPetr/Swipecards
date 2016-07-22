package com.topface.topface.ui.external_libs.offers;

import com.topface.topface.utils.Utils;

public class GoogleAdParams {
    private String mId = Utils.EMPTY;
    private boolean mIsLimitAdTrackingEnabled = true;

    @SuppressWarnings("unused")
    public GoogleAdParams() {
    }

    public GoogleAdParams(String id, boolean isLimitAdTrackingEnabled) {
        mId = id;
        mIsLimitAdTrackingEnabled = isLimitAdTrackingEnabled;
    }

    public String getId() {
        return mId;
    }

    public boolean isLimitAdTrackingEnabled() {
        return mIsLimitAdTrackingEnabled;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GoogleAdParams that = (GoogleAdParams) o;

        if (mIsLimitAdTrackingEnabled != that.mIsLimitAdTrackingEnabled) return false;
        return mId.equals(that.mId);

    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + (mIsLimitAdTrackingEnabled ? 1 : 0);
        return result;
    }
}