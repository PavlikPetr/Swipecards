package com.topface.topface.utils;

import android.os.Parcel;
import android.os.Parcelable;
import com.topface.topface.Static;

public class FormItem implements Parcelable{
    // Data
    public int type;
    public String title;
    public String value;
    public FormItem header;
    public boolean equal;

    public int titleId = NO_RESOURCE_ID;
    public int dataId = NO_RESOURCE_ID;

    // Constants
    public static final int HEADER = 1;
    public static final int DATA = 3;
    public static final int STATUS = 4;
    public static final int DIVIDER = 5;

    public static final int NO_RESOURCE_ID = -1;
    public static final int NOT_SPECIFIED_ID = 0;

    private static FormItem divider = null;

    //private static final long serialVersionUID = 1883262786634798671L;    

    public FormItem(int titleId, int type) {
        this.titleId = titleId;
        this.type = type;
        this.value = Static.EMPTY;
        this.dataId = NO_RESOURCE_ID;
        this.equal = false;
    }

    public FormItem(int titleId, int dataId, int type) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.equal = false;
        this.value = Static.EMPTY;
    }

    public FormItem(int titleId, int dataId, int type, FormItem header) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.equal = false;
        this.header = header;
        this.value = Static.EMPTY;
    }

    public FormItem(int titleId, String data, int type) {
        this.titleId = titleId;
        this.value = data == null ? Static.EMPTY : data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
        this.equal = false;
    }

    public FormItem(int titleId, String data, int type, FormItem header) {
        this.titleId = titleId;
        this.value = data == null ? Static.EMPTY : data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
        this.equal = false;
        this.header = header;
    }

    private FormItem(int type) {
        this.type = type;
        this.value = Static.EMPTY;
        this.dataId = NO_RESOURCE_ID;
        this.title = Static.EMPTY;
        this.titleId = NO_RESOURCE_ID;
        this.equal = false;
    }

    public FormItem() {
    }

    public static FormItem getDivider() {
        if (divider == null) {
            FormItem.divider = new FormItem(DIVIDER);
        }
        return divider;
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
        dest.writeInt(equal ? 1 : 0);
        dest.writeInt(titleId);
        dest.writeInt(dataId);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public FormItem createFromParcel(Parcel in) {
                    FormItem result = new FormItem();
                    result.type = in.readInt();
                    result.title =  in.readString();
                    result.value = in.readString();
                    result.equal = in.readInt() == 1;
                    result.titleId = in.readInt();
                    result.dataId = in.readInt();
                    return result;
                }

                public FormItem[] newArray(int size) {
                    return new FormItem[size];
                }
            };
}
