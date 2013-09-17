package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackReport extends ApiRequest {

    public static final String SERVICE = "user.sendFeedback";

    public String subject;
    public String text;
    public String extra;
    public String email;

    public FeedbackReport(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("subject", subject)
                .put("message", text)
                .put("extra", extra)
                .put("email", email);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
