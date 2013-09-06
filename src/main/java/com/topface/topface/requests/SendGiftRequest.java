package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class SendGiftRequest extends ConfirmedApiRequest {

    static final String USER_ID = "userid";
    static final String GIFT_ID = "giftid";

    public static final String service = "gift";
    public int userId;
    public int giftId;

    public SendGiftRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put(USER_ID, userId).put(GIFT_ID, giftId);
    }

    @Override
    public String getServiceName() {
        return service;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().trackEvent("Gifts", "Send", Integer.toString(giftId), 1L);
    }
}
