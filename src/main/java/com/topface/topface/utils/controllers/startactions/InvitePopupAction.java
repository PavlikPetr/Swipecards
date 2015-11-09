package com.topface.topface.utils.controllers.startactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.InvitesPopup;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.EasyTracker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class InvitePopupAction extends LinkedStartAction {

    private WeakReference<BaseFragmentActivity> mActivity;
    private int mPriority;

    public InvitePopupAction(BaseFragmentActivity activity, int priority) {
        mActivity = new WeakReference<>(activity);
        mPriority = priority;
    }

    @Override
    public void callInBackground() {
        SharedPreferences preferences = App.getContext().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED,
                Context.MODE_PRIVATE
        );
        preferences.edit()
                .putLong(InvitesPopup.INVITE_POPUP_PREF_KEY, System.currentTimeMillis())
                .apply();
    }

    @Override
    public void callOnUi() {
        startInvitePopup();
    }

    @Override
    public boolean isApplicable() {
        return InvitesPopup.isApplicable(App.from(mActivity.get()).getOptions().popup_timeout, mActivity.get())
                && getContactsCount() >= App.from(mActivity.get()).getOptions().contacts_count;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

    private int getContactsCount() {
        Cursor cursor = mActivity.get().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int contacts_count = 0;
        if (cursor != null) {
            contacts_count = cursor.getCount();
            cursor.close();
        }
        return contacts_count;
    }


    private void startInvitePopup() {
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                InvitesPopup popup = InvitesPopup.newInstance(contacts);
                popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mOnNextActionListener != null) {
                            mOnNextActionListener.onNextAction();
                        }
                    }
                });
                popup.show(mActivity.get().getSupportFragmentManager(), InvitesPopup.TAG);
                EasyTracker.sendEvent("InvitesPopup", "Show", "", 0L);

            }
        };
        ContactsProvider provider = new ContactsProvider(mActivity.get());
        provider.getContacts(-1, 0, handler);
    }

}
