package com.topface.topface.requests;

import android.content.Context;
import android.support.annotation.IntDef;

import org.json.JSONException;
import org.json.JSONObject;

public class SendLikeRequest extends ConfirmedApiRequest {
    private static final String SERVICE = "like.send";

    public static final int DEFAULT_MUTUAL = 1;
    public static final int DEFAULT_NO_MUTUAL = 0;
    public static final int FROM_SEARCH = 0;
    public static final int FROM_PROFILE = 1;
    public static final int FROM_FEED = 2;

    @IntDef({FROM_SEARCH, FROM_PROFILE, FROM_FEED})
    public @interface Place {
    }

    public int getUserid() {
        return userid;
    }

    public int getMutualid() {
        return mutualid;
    }

    @Place
    public int getPlace() {
        return place;
    }

    // Data
    private int userid; // идентификатор пользователя для оценки
    private int mutualid; // идентификатор сообщения из ленты, на который отправляется взаимная симпатия
    @Place
    private int place; //TODO место отправки лайка

    public SendLikeRequest(Context context, int userId, int mutualId, @Place int place, boolean blockUnconfirmed) {
        super(context, blockUnconfirmed);
        this.mutualid = mutualId;
        this.userid = userId;
        this.place = place;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userId", userid)
                .put("mutualId", mutualid)
                .put("place", place);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
