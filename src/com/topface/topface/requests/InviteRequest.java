package com.topface.topface.requests;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.topface.topface.utils.Debug;
import android.content.Context;

import java.util.ArrayList;

public class InviteRequest extends ApiRequest {
    // Data
    private static final String service = "invite";
    private ArrayList<Recepient> recipients;       // строка данных заказа от Google Play

    public InviteRequest(Context context) {
        super(context);
        recipients = new ArrayList<Recepient>();
    }

    @Override
    public String toString() {
        JSONObject root = new JSONObject();
        try {
            root.put("service", service);
            root.put("ssid", ssid);
            JSONObject jsondata = new JSONObject();
            jsondata.put("recipients", getRecipientsJson());
            root.put("data", jsondata);
        } catch(JSONException e) {
            Debug.log(this,"Wrong request compiling: " + e);
        }

        return root.toString();
    }

    private JSONArray getRecipientsJson() throws JSONException {
        JSONArray recipientsJson = new JSONArray();
        for (final Recepient user : recipients) {
            recipientsJson.put(getJSONFromRecepient(user));
        }
        return recipientsJson;
    }

    public boolean addRecipient(String name, String phone) {
        return isValidPhone(phone) &&
               recipients.add(new Recepient(name, phone));

    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= 10;
    }
    
    private JSONObject getJSONFromRecepient(Recepient recipient) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", recipient.name);
        json.put("phone", recipient.phone);

        return json;
    }

    private static class Recepient {
        public Recepient(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }
        public  String name;
        public String phone;
    }

}
