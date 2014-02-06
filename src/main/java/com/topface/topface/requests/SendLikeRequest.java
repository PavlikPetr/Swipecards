package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class SendLikeRequest extends ConfirmedApiRequest {
    private static final String SERVICE = "like.send";

    public static final int DEFAULT_MUTUAL = 1;
    public static final int DEFAULT_NO_MUTUAL = 0;

    public enum Place {
        FROM_SEARCH(0),
        FROM_PROFILE(1),
        FROM_FEED(2);

        int placeId;

        Place(int id) {
            this.placeId = id;
        }

        public int id() {
            return placeId;
        }
    }

    public int getUserid() {
        return userid;
    }

    public int getMutualid() {
        return mutualid;
    }

    public Place getPlace() {
        return place;
    }

    // Data
    private int userid; // идентификатор пользователя для оценки
    private int mutualid; // идентификатор сообщения из ленты, на который отправляется взаимная симпатия
    private Place place; //TODO место отправки лайка

    public SendLikeRequest(Context context, int userId, Place place) {
        super(context);
        this.userid = userId;
        this.place = place;
    }

    public SendLikeRequest(Context context, int userId, int mutualId, Place place) {
        super(context);
        this.mutualid = mutualId;
        this.userid = userId;
        this.place = place;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("userId", userid)
                .put("mutualId", mutualid)
                .put("place", place.id());
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }
}
