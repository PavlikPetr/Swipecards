package com.topface.topface.data.experiments;

import org.json.JSONObject;

/**
 * base fields for experiments
 * get, set and init
 */
public abstract class BaseExperiment {
    protected static final String KEY_ENABLED = "enabled";
    protected static final String KEY_GROUP = "group";
    private boolean mEnabled = false;
    private String mGroup;

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

    public JSONObject init(JSONObject response) {
        JSONObject source = response.optJSONObject(getOptionsKey());
        if (source != null) {
            setKeys(source);
        }
        return source;
    }

    protected void setKeys(JSONObject source) {
        setEnabled(source.optBoolean(KEY_ENABLED));
        setGroup(source.optString(KEY_GROUP));
    }
}
