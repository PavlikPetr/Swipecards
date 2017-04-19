package com.topface.topface.ui.edit.filter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.topface.data.City;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.edit.filter.viewModel.DatingFilterViewModel;
import com.topface.topface.ui.edit.filter.viewModel.FilterViewModel;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

public class FilterData implements Cloneable, Parcelable {

    public City city;
    public boolean isOnlineOnly;
    public int ageStart;
    public int ageEnd;
    public boolean isPrettyOnly;
    public int sex;

    public FilterData(@NotNull DatingFilter filter) {
        if (filter != null) {
            city = filter.city;
            isOnlineOnly = DatingFilter.getOnlyOnlineField();
            ageStart = filter.ageStart;
            ageEnd = filter.ageEnd;
            isPrettyOnly = filter.beautiful;
            sex = filter.sex;
        } else {
            fillFakeFilter();
        }
    }

    public FilterData(@NotNull FilterViewModel model) {
        if (model != null) {
            city = model.city.get();
            isOnlineOnly = model.onlineOnly.get();
            ageStart = model.ageStart.get();
            ageEnd = model.ageEnd.get();
            isPrettyOnly = model.prettyOnly.get();
            sex = model.isMaleSelected.get() ? Profile.BOY : Profile.GIRL;
        } else {
            fillFakeFilter();
        }
    }

    public FilterData(@NotNull DatingFilterViewModel model) {
        if (model != null) {
            city = model.getCity().get();
            isOnlineOnly = model.getOnlineOnly().get();
            ageStart = model.getAgeStart().get();
            ageEnd = model.getAgeEnd().get();
            sex = model.isMaleSelected().get() ? Profile.BOY : Profile.GIRL;
        } else {
            fillFakeFilter();
        }
    }

    private void fillFakeFilter() {
        city = City.createCity(City.ALL_CITIES, Utils.EMPTY, Utils.EMPTY);
        isOnlineOnly = false;
        ageStart = DatingFilter.MIN_AGE;
        ageEnd = DatingFilter.MAX_AGE;
        isPrettyOnly = false;
        sex = Profile.GIRL;
    }

    protected FilterData(Parcel in) {
        city = in.readParcelable(City.class.getClassLoader());
        isOnlineOnly = in.readByte() != 0;
        ageStart = in.readInt();
        ageEnd = in.readInt();
        isPrettyOnly = in.readByte() != 0;
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
        if (isPrettyOnly != that.isPrettyOnly) return false;
        if (sex != that.sex) return false;
        return city != null ? city.equals(that.city) : that.city == null;

    }

    @Override
    public int hashCode() {
        int result = city != null ? city.hashCode() : 0;
        result = 31 * result + (isOnlineOnly ? 1 : 0);
        result = 31 * result + ageStart;
        result = 31 * result + ageEnd;
        result = 31 * result + (isPrettyOnly ? 1 : 0);
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
        dest.writeByte((byte) (isPrettyOnly ? 1 : 0));
        dest.writeInt(sex);
    }
}
