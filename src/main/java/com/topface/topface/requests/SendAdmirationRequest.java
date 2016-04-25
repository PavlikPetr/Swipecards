package com.topface.topface.requests;

import android.content.Context;

public class SendAdmirationRequest extends SendLikeRequest {

    public static final String service = "admiration.send";

    public SendAdmirationRequest(Context context, int userId, int mutualId, @Place int place, boolean blockUnconfirmed) {
        super(context, userId, mutualId, place, blockUnconfirmed);
    }

    @Override
    public String getServiceName() {
        return service;
    }
}
