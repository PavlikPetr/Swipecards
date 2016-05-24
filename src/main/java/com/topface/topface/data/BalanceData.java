package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ppetr on 16.06.15.
 * data class for balance object from requests
 */
public class BalanceData implements Cloneable, Parcelable {
    public boolean premium;
    public int likes = 0;
    public int money = 0;

    public BalanceData(boolean premium, int likes, int money) {
        this.premium = premium;
        this.likes = likes;
        this.money = money;
    }

    public BalanceData(BalanceData balanceData) {
        this.premium = balanceData.premium;
        this.likes = balanceData.likes;
        this.money = balanceData.money;
    }

    public BalanceData() {
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BalanceData)) return false;
        BalanceData data = (BalanceData) o;
        return premium == data.premium && likes == data.likes && money == data.money;
    }

    @Override
    public int hashCode() {
        int result = premium ? 1 : 0;
        result = 31 * result + likes;
        return 31 * result + money;
    }

    public static final Parcelable.Creator<BalanceData> CREATOR = new Parcelable.Creator<BalanceData>() {
        @Override
        public BalanceData createFromParcel(Parcel in) {
            return new BalanceData(in);
        }

        @Override
        public BalanceData[] newArray(int size) {
            return new BalanceData[size];
        }
    };

    protected BalanceData(Parcel in) {
        premium = in.readInt() == 1;
        likes = in.readInt();
        money = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(premium ? 1 : 0);
        dest.writeInt(likes);
        dest.writeInt(money);
    }
}
