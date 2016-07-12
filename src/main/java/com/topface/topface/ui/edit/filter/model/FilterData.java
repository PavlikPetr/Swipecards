package com.topface.topface.ui.edit.filter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.edit.filter.viewModel.FilterViewModel;

import org.jetbrains.annotations.NotNull;

public class FilterData implements Cloneable, Parcelable {

    public City city;
    public boolean isOnlineOnly;
    public int ageStart;
    public int ageEnd;
    public boolean isPreetyOnly;
    public int sex;

    public FilterData(@NotNull DatingFilter filter) {
        city = filter.city;
        isOnlineOnly = DatingFilter.getOnlyOnlineField();
        ageStart = filter.ageStart;
        ageEnd = filter.ageEnd;
        isPreetyOnly = filter.beautiful;
        sex = filter.sex;
    }

    public FilterData(@NotNull FilterViewModel model) {
        city = model.city.get();
        isOnlineOnly = model.onlineOnly.get();
        ageStart = model.ageStart.get();
        ageEnd = model.ageEnd.get();
        isPreetyOnly = model.preetyOnly.get();
        sex = model.isMaleSelected.get() ? Profile.BOY : Profile.GIRL;
    }

    protected FilterData(Parcel in) {
        city = in.readParcelable(City.class.getClassLoader());
        isOnlineOnly = in.readByte() != 0;
        ageStart = in.readInt();
        ageEnd = in.readInt();
        isPreetyOnly = in.readByte() != 0;
        sex = in.readInt();
    }

    public static final Creator<FilterData> CREATOR = new Creator<FilterData>() {
        @Override
        public FilterData createFromParcel(Parcel in) {
            return new FilterData(in);
        }

        @Override
        public FilterData[] newArray(int size) {
            return new FilterData[size];
        }
    };

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FilterData that = (FilterData) o;

        if (isOnlineOnly != that.isOnlineOnly) return false;
        if (ageStart != that.ageStart) return false;
        if (ageEnd != that.ageEnd) return false;
        if (isPreetyOnly != that.isPreetyOnly) return false;
        if (sex != that.sex) return false;
        return city != null ? city.equals(that.city) : that.city == null;

    }

    @Override
    public int hashCode() {
        int result = city != null ? city.hashCode() : 0;
        result = 31 * result + (isOnlineOnly ? 1 : 0);
        result = 31 * result + ageStart;
        result = 31 * result + ageEnd;
        result = 31 * result + (isPreetyOnly ? 1 : 0);
        result = 31 * result + sex;
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(city, flags);
        dest.writeByte((byte) (isOnlineOnly ? 1 : 0));
        dest.writeInt(ageStart);
        dest.writeInt(ageEnd);
        dest.writeByte((byte) (isPreetyOnly ? 1 : 0));
        dest.writeInt(sex);
    }
}
