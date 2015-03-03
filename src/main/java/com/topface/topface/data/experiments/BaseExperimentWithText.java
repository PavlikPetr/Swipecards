package com.topface.topface.data.experiments;

import org.json.JSONObject;

public abstract class BaseExperimentWithText extends BaseExperiment {
    protected static final String KEY_TEXT = "text";
    private String mText;

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
}
