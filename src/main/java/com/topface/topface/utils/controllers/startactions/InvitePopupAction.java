package com.topface.topface.utils.controllers.startactions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.dialogs.InvitesPopup;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;


public class InvitePopupAction extends LinkedStartAction {

    private Activity mActivity;
    private int mPriority;

    public InvitePopupAction(Activity activity, int priority) {
        mActivity = activity;
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
        if (isFacebook()) {
            return true;
        } else {
            return InvitesPopup.isApplicable() && getContactsCount() >= CacheProfile.getOptions().contacts_count;
        }
    }

    @Override
    public int getPriority() {
        return mPriority;
    }

    @Override
    public String getActionName() {
        return getClass().getSimpleName();
    }

    private boolean isFacebook() {
        return AuthToken.getInstance().getSocialNet()
                .equals(AuthToken.SN_FACEBOOK);
    }

    private int getContactsCount() {
        Cursor cursor = mActivity.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int contacts_count = cursor.getCount();
        cursor.close();
        return contacts_count;
    }


    private void startInvitePopup() {
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                InvitesPopup popup = InvitesPopup.newInstance(contacts);
                popup.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (mOnNextActionListener != null) {
                            mOnNextActionListener.onNextAction();
                        }
                    }
                });
                popup.show(((ActionBarActivity) mActivity).getSupportFragmentManager(), InvitesPopup.TAG);
                EasyTracker.sendEvent("InvitesPopup", "Show", "", 0L);

            }
        };
        ContactsProvider provider = new ContactsProvider(mActivity);
        provider.getContacts(-1, 0, handler);
    }

}
