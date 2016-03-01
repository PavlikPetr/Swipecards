package com.topface.topface.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.topface.topface.App;

public class FormItem implements Parcelable {
    // Data
    public int type;
    public String title;
    public String value;
    public FormItem header;

    public int titleId = NO_RESOURCE_ID;
    public int dataId = NO_RESOURCE_ID;

    /**
     * Is form item value updating right now.
     */
    public transient boolean isEditing;

    // Constants
    public static final int HEADER = 1;
    public static final int DATA = 3;
    public static final int STATUS = 4;
    public static final int NAME = 6;
    public static final int SEX = 7;
    public static final int AGE = 8;
    public static final int CITY = 9;

    public static final int NO_RESOURCE_ID = -1;

    transient private TextLimitInterface mTextLimitInterface;
    transient private ValueLimitInterface mValueLimitInterface;
    private boolean mOnlyForWomen = false;
    private boolean mIsCanBeEmpty = true;

    //private static final long serialVersionUID = 1883262786634798671L;    

    public FormItem(int titleId, int type) {
        this.titleId = titleId;
        this.type = type;
        this.value = Utils.EMPTY;
        this.dataId = NO_RESOURCE_ID;
    }

    public FormItem(int titleId, int dataId, int type) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.value = Utils.EMPTY;
    }

    public FormItem(int titleId, int dataId, int type, FormItem header) {
        this.titleId = titleId;
        this.dataId = dataId;
        this.type = type;
        this.header = header;
        this.value = Utils.EMPTY;
    }

    public FormItem(int titleId, String data, int type) {
        this.titleId = titleId;
        this.value = data == null ? Utils.EMPTY : data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
    }

    public FormItem(int titleId, String data, int type, FormItem header) {
        this.titleId = titleId;
        this.value = data == null ? Utils.EMPTY : data;
        this.dataId = NO_RESOURCE_ID;
        this.type = type;
        this.header = header;
    }

    @SuppressWarnings("unused")
    private FormItem(int type) {
        this.type = type;
        this.value = Utils.EMPTY;
        this.dataId = NO_RESOURCE_ID;
        this.title = Utils.EMPTY;
        this.titleId = NO_RESOURCE_ID;
    }

    public FormItem(FormItem formItem) {
        dataId = formItem.dataId;
        title = formItem.title;
        type = formItem.type;
        value = formItem.value;
        header = formItem.header;
        titleId = formItem.titleId;
        mTextLimitInterface = formItem.mTextLimitInterface;
        mValueLimitInterface = formItem.mValueLimitInterface;
        mOnlyForWomen = formItem.mOnlyForWomen;
        mIsCanBeEmpty = formItem.mIsCanBeEmpty;
    }

    public FormItem() {
    }

    public void copy(FormItem formItem) {
        dataId = formItem.dataId;
        title = formItem.title;
        type = formItem.type;
        value = formItem.value;
        header = formItem.header;
        titleId = formItem.titleId;
        mTextLimitInterface = formItem.mTextLimitInterface;
        mValueLimitInterface = formItem.mValueLimitInterface;
        isEditing = formItem.isEditing;
        mOnlyForWomen = formItem.mOnlyForWomen;
        mIsCanBeEmpty = formItem.mIsCanBeEmpty;
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
        dest.writeByte((byte) (mOnlyForWomen ? 1 : 0));
        dest.writeByte((byte) (mIsCanBeEmpty ? 1 : 0));
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
                    formItem.dataId == dataId &&
                    formItem.mOnlyForWomen == mOnlyForWomen;
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
        hash = hash * 31 + (mOnlyForWomen ? 1 : 0);
        hash = hash * 31 + (mIsCanBeEmpty ? 1 : 0);
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
                    result.mOnlyForWomen = in.readByte() != 0;
                    result.mIsCanBeEmpty = in.readByte() != 0;
                    return result;
                }

                public FormItem[] newArray(int size) {
                    return new FormItem[size];
                }
            };

    public interface TextLimitInterface {
        int getLimit();

        boolean isVisible();
    }

    public interface ValueLimitInterface {
        int getMinValue();

        int getMaxValue();

        boolean isEmptyValueAvailable();
    }

    public void setTextLimitInterface(TextLimitInterface TextLimitInterface) {
        mTextLimitInterface = TextLimitInterface;
    }

    public TextLimitInterface getTextLimitInterface() {
        return mTextLimitInterface;
    }

    public void setValueLimitInterface(ValueLimitInterface valueLimitInterface) {
        mValueLimitInterface = valueLimitInterface;
    }

    public ValueLimitInterface getValueLimitInterface() {
        return mValueLimitInterface;
    }

    public String getTitle() {
        if (TextUtils.isEmpty(title)) {
            return App.getContext().getString(titleId);
        } else {
            return title;
        }
    }

    public boolean isValueValid() {
        if (mTextLimitInterface != null && value.length() > mTextLimitInterface.getLimit() ||
                !mIsCanBeEmpty && TextUtils.isEmpty(value)) {
            return false;
        } else if (mValueLimitInterface != null) {
            if (TextUtils.isEmpty(value)) {
                return mValueLimitInterface.isEmptyValueAvailable();
            }
            if (TextUtils.isDigitsOnly(value)) {
                int val = Integer.parseInt(value);
                return val >= mValueLimitInterface.getMinValue() && val <= mValueLimitInterface.getMaxValue();
            }
        }
        return true;
    }

    public void setOnlyForWomen(boolean onlyForWomen) {
        mOnlyForWomen = onlyForWomen;
    }

    public boolean isOnlyForWomen() {
        return mOnlyForWomen;
    }

    public void setCanBeEmpty(boolean canBeEmpty) {
        mIsCanBeEmpty = canBeEmpty;
    }

    public static class DefaultTextLimiter implements TextLimitInterface {

        private final int mLimit;

        public DefaultTextLimiter() {
            mLimit = App.getAppOptions().getUserStringSettingMaxLength();
        }

        public DefaultTextLimiter(int limit) {
            mLimit = limit;
        }

        @Override
        public int getLimit() {
            return mLimit;
        }

        @Override
        public boolean isVisible() {
            return true;
        }
    }
}
