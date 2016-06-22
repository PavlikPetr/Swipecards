package com.topface.topface.requests;

import android.content.Context;
import android.text.TextUtils;

import com.topface.topface.utils.ContactsProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InviteContactsRequest extends ConfirmedApiRequest {

    public final static String SERVICE_NAME = "virus.inviteContacts";
    private ArrayList<String> emails;
    private ArrayList<String> phones;

    public InviteContactsRequest(Context context, ArrayList<ContactsProvider.Contact> contacts
            , boolean blockUnconfirmed) {
        super(context, blockUnconfirmed);
        parseContacts(contacts);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return new JSONObject().put("emails", new JSONArray(emails)).put("phones", new JSONArray(phones));
    }

    private void parseContacts(ArrayList<ContactsProvider.Contact> contacts) {
        emails = new ArrayList<>();
        phones = new ArrayList<>();

        for (ContactsProvider.Contact contact : contacts) {
            String email = contact.getEmail();
            if (!TextUtils.isEmpty(email)) {
                emails.add(email);
            }
            String phone = contact.getPhone();
            if (!TextUtils.isEmpty(phone)) {
                phones.add(phone);
            }
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
