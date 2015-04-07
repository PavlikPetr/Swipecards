package com.topface.topface.data.experiments;

import org.json.JSONObject;

public class InstantMessageFromSearch {

    protected static final String KEY_TEXT = "text";
    protected static final String INSTANT_MSG = "instantMessageFromSearch";
    private String mText;

    public void setText(String text) {
        mText = text;
    }

    public String getText() {
        return mText;
    }

    public JSONObject init(JSONObject response) {
        JSONObject source = response.optJSONObject(INSTANT_MSG);;
        if (source != null) {
            setText(source.optString(KEY_TEXT, ""));
        }
        return source;
    }
}
