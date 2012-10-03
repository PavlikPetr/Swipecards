package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class FeedbackReport extends AbstractApiRequest {

    private static String service = "userfeedback";

    public String subject;
    public String text;
    public String extra;

    public FeedbackReport(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("subject", subject);
        result.put("text", text);
        result.put("extra", extra);
        return result;
    }

    @Override
    protected String getServiceName() {
        return service;
    }

}
