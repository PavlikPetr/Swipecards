package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.maps.GeoPoint;
import com.topface.topface.utils.Debug;
import org.json.JSONException;
import org.json.JSONObject;

public class Coordinates extends AbstractData implements Parcelable {

    private double mLatitude;
    private double mLongitude;
    private GeoPoint mGeoPoint;

    public Coordinates(JSONObject jsonObject) throws WrongCoordinatesException, JSONException {
        this(jsonObject.getDouble("longitude"), jsonObject.getDouble("latitude"));
    }

    public Coordinates(double longitude, double latitude) throws WrongCoordinatesException {
        setCoordinates(longitude, latitude);
    }

    private void setCoordinates(double longitude, double latitude) throws WrongCoordinatesException {
        if (isCorrectCoordinates(longitude, latitude)) {
            mLongitude = longitude;
            mLatitude = latitude;
        } else {
            throw new WrongCoordinatesException();
        }
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public static boolean isCorrectCoordinates(double longitude, double latitude) {
        return Math.abs(longitude) <= 180 && Math.abs(latitude) <= 180;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(getLongitude());
        dest.writeDouble(getLatitude());
    }

    public class WrongCoordinatesException extends Exception {
        @Override
        public String getMessage() {
            return "Coordinates value must be => -180 and <= 180";
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Coordinates createFromParcel(Parcel in) {
            try {
                return new Coordinates(in.readDouble(), in.readDouble());
            } catch (WrongCoordinatesException e) {
                Debug.error(e);
                return null;
            }
        }

        @Override
        public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }
    };

    public GeoPoint getGeoPoint() {
        if (mGeoPoint == null) {
            mGeoPoint = new GeoPoint((int) (mLatitude * 1E6), (int) (mLongitude * 1E6));
        }
        return mGeoPoint;
    }

    @Override
    public String toString() {
        return "lon: " + Double.toString(mLongitude) + ", lat: " + Double.toString(mLatitude);
    }
}
