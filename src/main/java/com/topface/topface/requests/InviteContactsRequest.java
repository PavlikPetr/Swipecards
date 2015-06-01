package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.ContactsProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InviteContactsRequest extends ConfirmedApiRequest {

    public final static String SERVICE_NAME = "virus.inviteContacts";
    private ArrayList<ContactsProvider.Contact> emails;
    private ArrayList<ContactsProvider.Contact> phones;

    public InviteContactsRequest(Context context, ArrayList<ContactsProvider.Contact> contacts) {
        super(context);
        parseContacts(contacts);
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject emailsObject = new JSONObject();
        JSONObject phonesObject = new JSONObject();

        for (ContactsProvider.Contact email : emails) {
            emailsObject.put(email.getPhone(), email.getName());
        }
        for (ContactsProvider.Contact phone : phones) {
            phonesObject.put(phone.getPhone(), phone.getName());
        }

        return new JSONObject().put("emails", emailsObject).put("phones", phonesObject);
    }

    private void parseContacts(ArrayList<ContactsProvider.Contact> contacts) {
        emails = new ArrayList<ContactsProvider.Contact>();
        phones = new ArrayList<ContactsProvider.Contact>();

        for (ContactsProvider.Contact contact : contacts) {
            if (contact.hasEmail()) {
                emails.add(contact);
            } else {
                phones.add(contact);
            }
        }
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }
}
