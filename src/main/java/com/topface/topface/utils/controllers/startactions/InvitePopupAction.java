package com.topface.topface.utils.controllers.startactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.topface.topface.App;
import com.topface.topface.ui.dialogs.InvitesPopup;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.IActivityDelegate;

import java.util.ArrayList;


public class InvitePopupAction implements IStartAction {

    private IActivityDelegate mDelegateActivity;
    private int mPriority;
    private OnNextActionListener mOnNextActionListener;

    public InvitePopupAction(IActivityDelegate iActivityDelegate, int priority) {
        mDelegateActivity = iActivityDelegate;
        mPriority = priority;
    }

    @Override
    public void callInBackground() {
        SharedPreferences preferences = App.getContext().getSharedPreferences(
                App.PREFERENCES_TAG_SHARED,
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
        return InvitesPopup.isApplicable(App.get().getOptions().popup_timeout)
                && getContactsCount() >= App.get().getOptions().contacts_count;
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setStartActionCallback(OnNextActionListener startActionCallback) {
        mOnNextActionListener = startActionCallback;
    }

    private int getContactsCount() {
        Cursor cursor = mDelegateActivity != null ? mDelegateActivity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null) : null;
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
                        mDelegateActivity = null;
                        if (mOnNextActionListener != null) {
                            mOnNextActionListener.onNextAction();
                        }
                    }
                });
                if (mDelegateActivity != null) {
                    popup.show(mDelegateActivity.getSupportFragmentManager(), InvitesPopup.TAG);
                    EasyTracker.sendEvent("InvitesPopup", "Show", "", 0L);
                }

            }
        };
        ContactsProvider provider = new ContactsProvider(App.getContext());
        provider.getContacts(-1, 0, handler);
    }

}
