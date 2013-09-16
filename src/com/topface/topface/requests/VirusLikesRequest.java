package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class VirusLikesRequest extends ConfirmedApiRequest {
    public static final String SERVICE = "virus.sendLikes";
    /**
     * идентификатор сообщения из ленты для получения лайков. Если параметр указан, данное сообщение будет удалено
     */
    public static final String VIRUS_FEED_ITEM = "item";
    /**
     * перечень социальных идентификаторов пользвоателей, которым были отпралвены приглашения на установку приложения
     */
    private static final String VIRUS_REQUEST_IDS = "socialids";

    private String mFeedId;
    private ArrayList<Long> mSocialids;

    public VirusLikesRequest(Context context) {
        this("", context);
    }

    public VirusLikesRequest(String feedId, Context context) {
        super(context);
        mFeedId = feedId;
    }

    public VirusLikesRequest(ArrayList<Long> socialids, Context context) {
        super(context);
        mSocialids = socialids;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject data = new JSONObject();
        if (!TextUtils.isEmpty(mFeedId)) {
            data.put(VIRUS_FEED_ITEM, mFeedId);
        } else if (mSocialids != null) {
            data.put(VIRUS_REQUEST_IDS, new JSONArray(mSocialids));
        }
        return data;
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
