package com.topface.topface.data.experiments;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * base fields for experiments
 * get, set and init
 */
public abstract class BaseExperiment implements Parcelable {
    protected static final String KEY_ENABLED = "enabled";
    protected static final String KEY_GROUP = "group";
    private boolean mEnabled = false;
    private String mGroup;

    public BaseExperiment() {
    }

    protected BaseExperiment(Parcel in) {
        mEnabled = in.readByte() == 1;
        mGroup = in.readString();
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    protected void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public String getGroup() {
        return mGroup;
    }

    protected void setGroup(String group) {
        this.mGroup = group;
    }

    protected abstract String getOptionsKey();

    protected boolean isEnabledByDefault() {
        return false;
    }

    public JSONObject init(JSONObject response) {
        JSONObject source = response.optJSONObject(getOptionsKey());
        if (source != null) {
            setKeys(source);
        } else {
            // возможно этот объект уже был использован с другими настройками, надо их сбрасывать
            setEnabled(isEnabledByDefault());
        }
        return source;
    }

    protected void setKeys(JSONObject source) {
        setEnabled(source.optBoolean(KEY_ENABLED));
        setGroup(source.optString(KEY_GROUP));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mEnabled ? 1 : 0));
        dest.writeString(mGroup);
    }
}
