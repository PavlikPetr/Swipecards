package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;

public class SendGiftAnswer extends AbstractData implements Parcelable {
    public History history;

    public static final Parcelable.Creator<SendGiftAnswer> CREATOR
            = new Parcelable.Creator<SendGiftAnswer>() {
        public SendGiftAnswer createFromParcel(Parcel in) {
            return new SendGiftAnswer(in);
        }

        public SendGiftAnswer[] newArray(int size) {
            return new SendGiftAnswer[size];
        }
    };

    public SendGiftAnswer() {
    }

    protected SendGiftAnswer(Parcel in) {
        history = (History) in.readParcelable(getClass().getClassLoader());
    }

    public static SendGiftAnswer parse(ApiResponse response) {
        SendGiftAnswer sendGift = new SendGiftAnswer();

        try {
            sendGift.history = new History(response);
        } catch (Exception e) {
            Debug.error("SendGift.class: Wrong response parsing", e);
        }

        return sendGift;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(history, flags);
    }
}
