package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.ContactsProvider;

import java.util.ArrayList;

public class ContactsActivity extends CheckAuthActivity {

    public static final int INTENT_CONTACTS = 8;
    public static final String CONTACTS_DATA = "contacts_data";

    public static Intent getIntentForContacts(ArrayList<ContactsProvider.Contact> data) {
        Intent intent = new Intent(App.getContext(), ContactsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CONTACTS);
        intent.putParcelableArrayListExtra(CONTACTS_DATA, data);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ac_contacts);
    }
}
