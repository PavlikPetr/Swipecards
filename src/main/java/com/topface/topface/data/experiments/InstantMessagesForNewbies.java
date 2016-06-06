package com.topface.topface.data.experiments;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

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
    public String getText() {
        String superString = super.getText();
        return superString.equals(Utils.EMPTY)
                ? App.getContext().getString(R.string.chat_block_not_mutual)
                : superString;
    }

    @Override
    protected String getOptionsKey() {
        return "instantMessagesForNewbies";
    }
}
