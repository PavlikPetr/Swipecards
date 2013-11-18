package com.topface.topface.requests;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class SendMailNotificationsRequest extends ApiRequest {

    private static final String SERVICE = "notification.setOptions";

    public Boolean mailSympathy = null;
    public Boolean mailMutual = null;
    public Boolean mailChat = null;
    public Boolean mailGuests = null;
    public Boolean apnsSympathy = null;
    public Boolean apnsMutual = null;
    public Boolean apnsChat = null;
    public Boolean apnsVisitors = null;

    public SendMailNotificationsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        if (mailSympathy != null) result.put("mailSympathy", mailSympathy);
        if (mailMutual != null) result.put("mailMutual", mailMutual);
        if (mailChat != null) result.put("mailMessage", mailChat);
        if (mailGuests != null) result.put("mailVisitor", mailGuests);
        if (apnsSympathy != null) result.put("apnsSympathy", apnsSympathy);
        if (apnsMutual != null) result.put("apnsMutual", apnsMutual);
        if (apnsChat != null) result.put("apnsMessage", apnsChat);
        if (apnsVisitors != null) result.put("apnsVisitor", apnsVisitors);
        return result;
    }

    @Override
    public String getServiceName() {
        return SERVICE;
    }

}
