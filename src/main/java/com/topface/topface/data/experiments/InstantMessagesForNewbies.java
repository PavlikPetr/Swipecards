package com.topface.topface.data.experiments;

import android.os.Parcel;
import android.os.Parcelable;

public class InstantMessagesForNewbies extends BaseExperimentWithText {

    public static final Parcelable.Creator<InstantMessagesForNewbies> CREATOR
            = new Parcelable.Creator<InstantMessagesForNewbies>() {
        public InstantMessagesForNewbies createFromParcel(Parcel in) {
            return new InstantMessagesForNewbies(in);
        }

        public InstantMessagesForNewbies[] newArray(int size) {
            return new InstantMessagesForNewbies[size];
        }
    };

    public InstantMessagesForNewbies(Parcel in) {
        super(in);
    }

    public InstantMessagesForNewbies() {

    }

    @Override
    protected String getOptionsKey() {
        return "instantMessagesForNewbies";
    }
}
