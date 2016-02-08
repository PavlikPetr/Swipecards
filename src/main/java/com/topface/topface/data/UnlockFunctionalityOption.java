package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.topface.framework.JsonUtils;
import com.topface.topface.requests.ApiResponse;

import org.json.JSONException;

/**
 * Ulock condition by video ads
 */
public class UnlockFunctionalityOption implements Parcelable {
    @SerializedName("likes")
    private UnlockScreenCondition mLikes;
    @SerializedName("admirations")
    private UnlockScreenCondition mAdmirations;
    @SerializedName("visitors")
    private UnlockScreenCondition mVisitors;
    @SerializedName("fans")
    private UnlockScreenCondition mFans;

    public UnlockFunctionalityOption() {
        mLikes = getDefaultValue();
        mAdmirations = getDefaultValue();
        mVisitors = getDefaultValue();
        mFans = getDefaultValue();
    }

    public static UnlockFunctionalityOption fillData(ApiResponse data) {
        if (data != null) {
            try {
                return JsonUtils.optFromJson(data.getJsonResult().getJSONObject("unlockByViewedAdVideo").toString(), UnlockFunctionalityOption.class, new UnlockFunctionalityOption());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return new UnlockFunctionalityOption();
    }

    protected UnlockFunctionalityOption(Parcel in) {
        mLikes = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
        mAdmirations = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
        mVisitors = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
        mFans = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
    }

    public static final Creator<UnlockFunctionalityOption> CREATOR = new Creator<UnlockFunctionalityOption>() {
        @Override
        public UnlockFunctionalityOption createFromParcel(Parcel in) {
            return new UnlockFunctionalityOption(in);
        }

        @Override
        public UnlockFunctionalityOption[] newArray(int size) {
            return new UnlockFunctionalityOption[size];
        }
    };

    private UnlockScreenCondition getDefaultValue() {
        return new UnlockScreenCondition(false, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mLikes, flags);
        dest.writeParcelable(mAdmirations, flags);
        dest.writeParcelable(mVisitors, flags);
        dest.writeParcelable(mFans, flags);
    }

    public static class UnlockScreenCondition implements Parcelable {
        @SerializedName("enabled")
        private boolean mIsEnabled;
        @SerializedName("seconds")
        private int mDuration;

        public UnlockScreenCondition(boolean isEnabled, int duration) {
            mIsEnabled = isEnabled;
            mDuration = duration;
        }

        protected UnlockScreenCondition(Parcel in) {
            mIsEnabled = in.readByte() != 0;
            mDuration = in.readInt();
        }

        public static final Creator<UnlockScreenCondition> CREATOR = new Creator<UnlockScreenCondition>() {
            @Override
            public UnlockScreenCondition createFromParcel(Parcel in) {
                return new UnlockScreenCondition(in);
            }

            @Override
            public UnlockScreenCondition[] newArray(int size) {
                return new UnlockScreenCondition[size];
            }
        };

        public boolean isEnabled() {
            return mIsEnabled;
        }

        public int getUnlockDuration() {
            return mDuration;
        }

        public void setEnable(boolean isEnable) {
            mIsEnabled = isEnable;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mIsEnabled ? 1 : 0);
            dest.writeInt(mDuration);
        }
    }

    public UnlockScreenCondition getUnlockLikesCondition() {
        return mLikes;
    }

    public UnlockScreenCondition getUnlockAdmirationCondition() {
        return mAdmirations;
    }

    public UnlockScreenCondition getUnlockVisitorsCondition() {
        return mVisitors;
    }

    public UnlockScreenCondition getUnlockFansCondition() {
        return mFans;
    }
}
