package com.topface.topface.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;

import java.util.ArrayList;
import java.util.Collection;

public class InviteRequest extends AbstractApiRequest {
    public static final int MIN_PHONE_LENGTH = 10;
    private ArrayList<Recipient> recipients;       // строка данных заказа от Google Play

    public InviteRequest(Context context) {
        super(context);
        recipients = new ArrayList<Recipient>();
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("recipients", getRecipientsJson());
    }

    @Override
    protected String getServiceName() {
        return "invite";
    }

    private JSONArray getRecipientsJson() throws JSONException {
        JSONArray recipientsJson = new JSONArray();
        for (final Recipient user : recipients) {
            recipientsJson.put(getJSONFromRecepient(user));
        }
        return recipientsJson;
    }

    public boolean addRecipient(String name, String phone) {
        return isValidPhone(phone) &&
               recipients.add(new Recipient(name, phone));

    }

    public boolean addRecipient(Recipient recipient) {
        return isValidPhone(recipient.phone) && recipients.add(recipient);
    }

    public boolean addRecipients(Collection<Recipient> recipientsList) {
        for (Recipient recipient : recipientsList) {
            if (isValidPhone(recipient.phone)) {
                recipients.add(recipient);
            }
        }

        return !recipients.isEmpty();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= MIN_PHONE_LENGTH;
    }
    
    private JSONObject getJSONFromRecepient(Recipient recipient) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", recipient.name);
        json.put("phone", recipient.phone);

        return json;
    }

    public static class Recipient {
        public Recipient(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
        public  String name;
        public String phone;
    }

}
