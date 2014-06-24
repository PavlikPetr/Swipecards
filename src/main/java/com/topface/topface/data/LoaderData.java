package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.ui.adapters.IListLoader;

public class LoaderData extends AbstractData implements IListLoader, Parcelable {

    //Loader indicators
    private boolean mIsListLoader = false;
    private boolean mIsListRetrier = false;

    public static final Parcelable.Creator<LoaderData> CREATOR
            = new Parcelable.Creator<LoaderData>() {
        public LoaderData createFromParcel(Parcel in) {
            return new LoaderData(in);
        }

        public LoaderData[] newArray(int size) {
            return new LoaderData[size];
        }
    };


    public LoaderData(IListLoader.ItemType type) {
        setLoaderTypeFlags(type);
    }

    public LoaderData(Parcel in) {
        mIsListLoader = in.readInt() == 1;
        mIsListRetrier = in.readInt() == 1;
    }

    public void setLoaderTypeFlags(ItemType type) {
        switch (type) {
            case LOADER:
                mIsListLoader = true;
                break;
            case RETRY:
                mIsListRetrier = true;
                break;
            case NONE:
            default:
                mIsListLoader = false;
                mIsListRetrier = false;
                break;
        }
    }

    @Override
    public boolean isLoader() {
        return mIsListLoader;
    }

    @Override
    public boolean isRetrier() {
        return mIsListRetrier;
    }

    public boolean isLoaderOrRetrier() {
        return mIsListLoader || mIsListRetrier;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mIsListLoader ? 1 : 0);
        dest.writeInt(mIsListRetrier ? 1 : 0);
    }
}
