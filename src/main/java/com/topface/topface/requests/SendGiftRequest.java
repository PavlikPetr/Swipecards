package com.topface.topface.requests;

import android.content.Context;
import com.google.analytics.tracking.android.EasyTracker;
import org.json.JSONException;
import org.json.JSONObject;

public class SendGiftRequest extends ConfirmedApiRequest {
    public static final String SERVICE = "gift.send";

    static final String USER_ID = "userId";
    static final String GIFT_ID = "giftId";

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
        return SERVICE;
    }

    @Override
    public void exec() {
        super.exec();
        EasyTracker.getTracker().sendEvent("Gifts", "Send", Integer.toString(giftId), 1L);
    }
}
