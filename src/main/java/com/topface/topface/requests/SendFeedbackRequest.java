package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.ui.settings.FeedbackMessageFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SendFeedbackRequest extends ApiRequest {

    public static final String SERVICE = "user.sendFeedback";
    public String subject;

    public String message;
    public List<String> extraEmails;
    public String email;
    public String[] tags;

    public SendFeedbackRequest(Context context, FeedbackMessageFragment.Report report) {
        super(context);
        email = report.getEmail();
        subject = report.getSubject();
        message = report.getBody();
        extraEmails = report.getUserDeviceAccounts();
        tags = new String[]{report.getType().getTag()};
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject()
                .put("email", email)
                .put("subject", subject)
                .put("message", message)
                .put("extraEmails", extraEmails)
                .put("tags", tags);
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
