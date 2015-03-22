package com.topface.topface.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.Static;

public class FormItem implements Parcelable {
    // Data
    public int type;
    public String title;
    public String value;
    public FormItem header;

    public int titleId = NO_RESOURCE_ID;
    public int dataId = NO_RESOURCE_ID;

    // Constants
    public static final int HEADER = 1;
    public static final int DATA = 3;
    public static final int STATUS = 4;
    public static final int DIVIDER = 5;
    public static final int NAME = 6;
    public static final int SEX = 7;
    public static final int AGE = 8;
    public static final int CITY = 9;

    public static final int NO_RESOURCE_ID = -1;
    public static final int NOT_SPECIFIED_ID = 0;

    private LimitInterface mLimitInterface;

    //private static final long serialVersionUID = 1883262786634798671L;    

    public FormItem(int titleId, int type) {
        this.titleId = titleId;
        this.type = type;
        this.value = Static.EMPTY;
        this.dataId = NO_RESOURCE_ID;
    }

    public FormItem(int titleId, int dataId, int type) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.value = Static.EMPTY;
    }

    public FormItem(int titleId, int dataId, int type, FormItem header) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.header = header;
        this.value = Static.EMPTY;
    }

    public FormItem(int titleId, String data, int type) {
        this.titleId = titleId;
        this.value = data == null ? Static.EMPTY : data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
    }

    public FormItem(int titleId, String data, int type, FormItem header) {
        this.titleId = titleId;
        this.value = data == null ? Static.EMPTY : data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
        this.header = header;
    }

    @SuppressWarnings("unused")
    private FormItem(int type) {
        this.type = type;
        this.value = Static.EMPTY;
        this.dataId = NO_RESOURCE_ID;
        this.title = Static.EMPTY;
        this.titleId = NO_RESOURCE_ID;
    }

    public FormItem() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(title);
        dest.writeString(value);
        dest.writeInt(titleId);
        dest.writeInt(dataId);
        dest.writeParcelable(header, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof FormItem) {
            FormItem formItem = (FormItem) o;
            return formItem.type == type &&
                    (formItem.title == null ? title == null : formItem.title.equals(title)) &&
                    (formItem.value == null ? value == null : formItem.value.equals(value)) &&
                    (formItem.header == null ? header == null : formItem.header.equals(header)) &&
                    formItem.titleId == titleId &&
                    formItem.dataId == dataId;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + type;
        hash = hash * 31 + (title == null ? 0 : title.hashCode());
        hash = hash * 31 + (value == null ? 0 : value.hashCode());
        hash = hash * 31 + (header == null ? 0 : header.hashCode());
        hash = hash * 31 + titleId;
        hash = hash * 31 + dataId;
        return hash;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public FormItem createFromParcel(Parcel in) {
                    FormItem result = new FormItem();
                    result.type = in.readInt();
                    result.title = in.readString();
                    result.value = in.readString();
                    result.titleId = in.readInt();
                    result.dataId = in.readInt();
                    result.header = in.readParcelable(FormItem.class.getClassLoader());
                    return result;
                }

                public FormItem[] newArray(int size) {
                    return new FormItem[size];
                }
            };

    public interface LimitInterface {
        int getLimit();
    }

    public void setLimitInterface(LimitInterface LimitInterface) {
        mLimitInterface = LimitInterface;
    }

    public LimitInterface getLimitInterface() {
        return mLimitInterface;
    }

    public String getTitle() {
        if (TextUtils.isEmpty(title)) {
            return App.getContext().getString(titleId);
        } else {
            return title;
        }
    }
}
