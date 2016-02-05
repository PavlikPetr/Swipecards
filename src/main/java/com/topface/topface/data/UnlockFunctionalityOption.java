package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.JsonObject;
import com.topface.framework.JsonUtils;
import com.topface.topface.requests.ApiResponse;

import org.json.JSONException;

/**
 * Ulock condition by video ads
 */
public class UnlockFunctionalityOption implements Parcelable {
    private UnlockScreenCondition likes;
    private UnlockScreenCondition admirations;
    private UnlockScreenCondition visitors;
    private UnlockScreenCondition fans;

    public UnlockFunctionalityOption() {
        likes = getDefaultValue();
        admirations = getDefaultValue();
        visitors = getDefaultValue();
        fans = getDefaultValue();
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
        likes = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
        admirations = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
        visitors = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
        fans = in.readParcelable(UnlockScreenCondition.class.getClassLoader());
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
        dest.writeParcelable(likes, flags);
        dest.writeParcelable(admirations, flags);
        dest.writeParcelable(visitors, flags);
        dest.writeParcelable(fans, flags);
    }

    public static class UnlockScreenCondition implements Parcelable {
        private boolean enabled;
        private int seconds;

        public UnlockScreenCondition(boolean isEnabled, int duration) {
            enabled = isEnabled;
            seconds = duration;
        }

        protected UnlockScreenCondition(Parcel in) {
            enabled = in.readByte() != 0;
            seconds = in.readInt();
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
            return enabled;
        }

        public int getUnlockDuration() {
            return seconds;
        }

        public void setEnable(boolean isEnable) {
            enabled = isEnable;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(enabled ? 1 : 0);
            dest.writeInt(seconds);
        }
    }

    public UnlockScreenCondition getUnlockLikesCondition() {
        return likes;
    }

    public UnlockScreenCondition getUnlockAdmirationCondition() {
        return admirations;
    }

    public UnlockScreenCondition getUnlockVisitorsCondition() {
        return visitors;
    }

    public UnlockScreenCondition getUnlockFansCondition() {
        return fans;
    }
}
