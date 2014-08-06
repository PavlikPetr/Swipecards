package com.topface.topface.ui;

import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.ContactsFragment;
import com.topface.topface.utils.ContactsProvider;

import java.util.ArrayList;

public class ContactsActivity extends CheckAuthActivity<ContactsFragment> {

    public static final int INTENT_CONTACTS = 8;
    public static final String CONTACTS_DATA = "contacts_data";

    public static Intent createIntent(ArrayList<ContactsProvider.Contact> data) {
        Intent intent = new Intent(App.getContext(), ContactsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CONTACTS);
        intent.putParcelableArrayListExtra(CONTACTS_DATA, data);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return ContactsFragment.class.getSimpleName();
    }

    @Override
    protected ContactsFragment createFragment() {
        return new ContactsFragment();
    }
}
