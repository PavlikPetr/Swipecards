package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Геоданные пользователя
 */
public class Geo extends AbstractData implements Parcelable {
    private String mAddress;
    private Coordinates mCoordinates;

    public Geo(JSONObject data) {
        super(data);
    }

    public Geo(String address, Coordinates coordinates) {
        mAddress = address;
        mCoordinates = coordinates;
    }

    public Geo(String address, double longitude, double latitude) {
        mAddress = address;
        try {
            mCoordinates = new Coordinates(longitude, latitude);
        } catch (Coordinates.WrongCoordinatesException e) {
            mCoordinates = null;
            Debug.error(e);
        }
    }

    @Override
    protected void fillData(JSONObject data) {
        super.fillData(data);
        this.mAddress = "";
        try {
            this.mCoordinates = new Coordinates(data);
        } catch (Coordinates.WrongCoordinatesException e) {
            this.mCoordinates = null;
            Debug.error(e);
        } catch (JSONException e) {
            this.mCoordinates = null;
            Debug.error(e);
        }
    }

    public String getAddress() {
        return mAddress;
    }

    public Coordinates getCoordinates() {
        return mCoordinates;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getAddress());
        dest.writeParcelable(getCoordinates(), flags);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Geo createFromParcel(Parcel in) {
            return new Geo(in.readString(), (Coordinates) in.readParcelable(Coordinates.class.getClassLoader()));
        }

        @Override
        public Geo[] newArray(int size) {
            return new Geo[size];
        }
    };

}
