package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;


public class VirusLikesRequest extends AbstractApiRequest {
    public static final String VIRUS_FEED_ITEM = "item";
    public static final String SERVICE_NAME = "VirusLikes";
    private final int mFeedId;

    public VirusLikesRequest(Context context) {
        this(0, context);
    }

    public VirusLikesRequest(int feedId, Context context) {
        super(context);
        mFeedId = feedId;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = null;
        if (mFeedId > 0) {
            data = new JSONObject().put(VIRUS_FEED_ITEM, mFeedId);
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
