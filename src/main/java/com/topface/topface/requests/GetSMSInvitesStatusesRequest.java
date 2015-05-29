package com.topface.topface.requests;

import android.content.Context;

import com.topface.topface.utils.ContactsProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class GetSMSInvitesStatusesRequest extends ApiRequest {
    public static final String SERVICE_NAME = "virus.getSmsInvitesStatuses";
    private ArrayList<ContactsProvider.Contact> mContactsArray;

    public GetSMSInvitesStatusesRequest(Context context, ArrayList<ContactsProvider.Contact> contactsArray) {
        super(context);
        mContactsArray = contactsArray;
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("phones", createIdArray());
        return result;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    private JSONArray createIdArray() {
        JSONArray phonesArray = new JSONArray();
        for (int i = 0; i < mContactsArray.size(); i++) {
            phonesArray.put(mContactsArray.get(i).getPhone());
        }
        return phonesArray;
    }
}
