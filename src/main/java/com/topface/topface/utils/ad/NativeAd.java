package com.topface.topface.utils.ad;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * Abstract ad data
 */
public abstract class NativeAd implements Parcelable {

    public abstract void show(View view);

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
