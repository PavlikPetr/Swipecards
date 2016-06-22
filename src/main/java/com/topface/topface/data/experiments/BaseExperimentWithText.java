package com.topface.topface.data.experiments;

import android.os.Parcel;

import org.json.JSONObject;

public abstract class BaseExperimentWithText extends BaseExperiment {
    protected static final String KEY_TEXT = "text";
    private String mText;

    public BaseExperimentWithText() {
    }

    protected BaseExperimentWithText(Parcel in) {
        super(in);
        mText = in.readString();
    }

    public void setText(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    @Override
    public JSONObject init(JSONObject response) {
        JSONObject source = super.init(response);
        if (source != null) {
            setText(source.optString(KEY_TEXT, ""));
        }
        return source;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mText);
    }

}
