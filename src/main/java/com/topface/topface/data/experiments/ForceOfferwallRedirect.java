package com.topface.topface.data.experiments;

import android.os.Parcel;
import android.os.Parcelable;

public class ForceOfferwallRedirect extends BaseExperimentWithText {

    public static final Parcelable.Creator<ForceOfferwallRedirect> CREATOR
            = new Parcelable.Creator<ForceOfferwallRedirect>() {
        public ForceOfferwallRedirect createFromParcel(Parcel in) {
            return new ForceOfferwallRedirect(in);
        }

        public ForceOfferwallRedirect[] newArray(int size) {
            return new ForceOfferwallRedirect[size];
        }
    };

    public ForceOfferwallRedirect(Parcel in) {
        super(in);
    }

    public ForceOfferwallRedirect() {

    }

    @Override
    protected String getOptionsKey() {
        return "forceOfferwallRedirect";
    }
}
