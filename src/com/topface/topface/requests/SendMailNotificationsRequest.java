package com.topface.topface.requests;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class SendMailNotificationsRequest extends AbstractApiRequest {

    private static final String SERVICE_NAME = "notifications";

    public Boolean mailsympathy = null;
    public Boolean mailmutual = null;
    public Boolean mailchat = null;
    public Boolean mailguests = null;
    public Boolean apnssympathy = null;
    public Boolean apnsmutual = null;
    public Boolean apnschat = null;
    public Boolean apnsguests = null;

    public SendMailNotificationsRequest(Context context) {
        super(context);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        if (mailsympathy != null) result.put("mailsympathy", mailsympathy);
        if (mailmutual != null) result.put("mailmutual", mailmutual);
        if (mailchat != null) result.put("mailmessage", mailchat);
        if (mailguests != null) result.put("mailvisitor", mailguests);
        if (apnssympathy != null) result.put("apnssympathy", apnssympathy);
        if (apnsmutual != null) result.put("apnsmutual", apnsmutual);
        if (apnschat != null) result.put("apnsmessage", apnschat);
        if (apnsguests != null) result.put("apnsvisitor", apnsguests);
        return result;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

}
