package com.topface.topface.utils.controllers.startactions;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.topface.topface.App;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.dialogs.InvitesPopup;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.EasyTracker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class InvitePopupAction implements IStartAction {

    private WeakReference<BaseFragmentActivity> mActivity;
    private int mPriority;
    private OnNextActionListener mOnNextActionListener;

    public InvitePopupAction(BaseFragmentActivity activity, int priority) {
        mActivity = new WeakReference<>(activity);
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
        Cursor cursor = mActivity.get().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int contacts_count = 0;
        if (cursor != null) {
            contacts_count = cursor.getCount();
            cursor.close();
        }
        return contacts_count;
    }


    private void startInvitePopup() {
        final BaseFragmentActivity activity = mActivity.get();
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                if (activity == null) {
                    return;
                }
                InvitesPopup popup = InvitesPopup.newInstance(contacts);
                popup.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mOnNextActionListener != null) {
                            mOnNextActionListener.onNextAction();
                        }
                    }
                });
                popup.show(activity.getSupportFragmentManager(), InvitesPopup.TAG);
                EasyTracker.sendEvent("InvitesPopup", "Show", "", 0L);

            }
        };
        if (activity != null) {
            ContactsProvider provider = new ContactsProvider(activity);
            provider.getContacts(-1, 0, handler);
        }
    }

}
