package com.topface.topface.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.topface.framework.utils.Debug;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.utils.DateUtils;

import org.json.JSONObject;


public class History extends FeedDialog implements Parcelable {

    private boolean mEmptyRepeatItem = false;
    private boolean mEmptyWaitingItem = false;
    public String blockText;
    public String dialogTitle;

    private String mJsonForParse;
    /**
     * Форматированное время создания элемента. Форматируется на этапе парсинга данных и затем не изменяется
     */
    public String createdFormatted;

    public History(ItemType type) {
        super((JSONObject) null);
        setLoaderTypeFlags(type);
    }

    public History(JSONObject data) {
        super(data);
        mJsonForParse = data.toString();
    }

    public History(ApiResponse response) {
        super(response.jsonResult);
        mJsonForParse = response.jsonResult.toString();
    }

    public History() {
        super((JSONObject) null);
    }

    @Override
    public void fillData(JSONObject item) {
        super.fillData(item);
        createdRelative = getRelativeCreatedDate(created);
        createdFormatted = DateUtils.getFormattedTime(created);
        blockText = item.optString("blockText");
        dialogTitle = item.optString("dialogTitle");
    }

    public boolean isWaitingItem() {
        return mEmptyWaitingItem;
    }

    public boolean isRepeatItem() {
        return mEmptyRepeatItem;
    }

    @Override
    public void setLoaderTypeFlags(ItemType type) {
        super.setLoaderTypeFlags(type);
        if (type == ItemType.WAITING) {
            mEmptyWaitingItem = true;
            mEmptyRepeatItem = false;
        } else if (type == ItemType.REPEAT) {
            mEmptyWaitingItem = false;
            mEmptyRepeatItem = true;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //Внимание! Мы пишем данные даже если mJsonForParse = null, иначе будет ошибка парсинга при восстановлении Activity
        dest.writeString(mJsonForParse);
    }

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public History createFromParcel(Parcel in) {
                    History result = null;
                    String jsonResult = in.readString();

                    if (jsonResult != null) {
                        try {
                            result = new History(new JSONObject(jsonResult));
                        } catch (Exception ex) {
                            Debug.error(ex);
                        }

                    }
                    return result;
                }

                public History[] newArray(int size) {
                    return new History[size];
                }
            };

    public boolean isFake() {
        try {
            int numId = Integer.parseInt(id);
            if (numId <= 0) return true;
        } catch (Exception ex) {
            Debug.error(ex);
        }

        return false;
    }

    @Override
    protected String getRelativeCreatedDate(long date) {
        return DateUtils.getRelativeDate(date, false).toUpperCase();
    }
}
