package com.topface.topface.data.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.data.FeedUser;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SerializableToJson;

import org.json.JSONException;
import org.json.JSONObject;

public class SearchUser extends FeedUser implements SerializableToJson, Parcelable {
    /**
     * статус пользователя
     */
    protected String status;
    /**
     * флаг возможности отправки взаимной симпатии
     */
    public boolean isMutualPossible;

    // Flags
    public boolean skipped;
    public boolean rated;

    public SearchUser(JSONObject user) {
        super(user);
    }

    @Override
    public void fillData(JSONObject user) {
        super.fillData(user);

        status = Profile.normilizeStatus(user.optString("status"));
        isMutualPossible = user.optBoolean("isMutualPossible");
        rated = user.optBoolean("rated", false);
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject json = super.toJson();
        json.put("status", status);
        json.put("isMutualPossible", isMutualPossible);
        json.put("photos", photos.toJson());
        json.put("photosCount", photosCount);
        json.put("rated", rated);
        return json;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(status);
        dest.writeByte((byte) (isMutualPossible ? 1 : 0));
        dest.writeByte((byte) (skipped ? 1 : 0));
        dest.writeByte((byte) (rated ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public SearchUser createFromParcel(Parcel in) {
                    return new SearchUser(in);
                }

                public SearchUser[] newArray(int size) {
                    return new SearchUser[size];
                }
            };

    protected SearchUser(Parcel in) {
        super(in);
        this.status = in.readString();
        this.isMutualPossible = in.readByte() != 0;
        this.skipped = in.readByte() != 0;
        this.rated = in.readByte() != 0;
    }
}
