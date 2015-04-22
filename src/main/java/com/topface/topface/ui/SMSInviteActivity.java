package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.SMSInviteFragment;
import com.topface.topface.utils.ContactsProvider;

import java.util.ArrayList;

public class SMSInviteActivity extends CheckAuthActivity<SMSInviteFragment> {

    public static final String CONTACTS_DATA = "contacts_data";
    public static final int INTENT_CONTACTS = 8;

    public static Intent createIntent(ArrayList<ContactsProvider.Contact> data) {
        Intent intent = new Intent(App.getContext(), SMSInviteActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CONTACTS);
        intent.putParcelableArrayListExtra(CONTACTS_DATA, data);
        return intent;
    }

    public static void startFragment(final Context context) {
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                context.startActivity(createIntent(contacts));
            }
        };
        ContactsProvider provider = new ContactsProvider(context);
        provider.getContacts(-1, 0, handler);
    }

    @Override
    protected String getFragmentTag() {
        return SMSInviteFragment.class.getSimpleName();
    }

    @Override
    protected SMSInviteFragment createFragment() {
        return new SMSInviteFragment();
    }
}
