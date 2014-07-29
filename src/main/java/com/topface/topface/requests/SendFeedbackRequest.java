package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.ui.settings.FeedbackMessageFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class SendFeedbackRequest extends ApiRequest {

    public static final String SERVICE = "user.sendFeedback";
    public String subject;

    public String message;
    public List<String> extraEmails;
    public String email;
    public String extra;
    public List<String> tags;

    public SendFeedbackRequest(Context context, FeedbackMessageFragment.Report report) {
        super(context);
        extra = report.getExtra();
        email = report.getEmail();
        subject = report.getSubject();
        message = report.getBody();
        extraEmails = report.getUserDeviceAccounts();
        tags = Arrays.asList(report.getType().getTag());
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("email", email)
                .put("subject", subject)
                .put("message", message)
                .put("extraEmails", new JSONArray(extraEmails))
                .put("extra", extra)
                .put("tags", new JSONArray(tags));
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
