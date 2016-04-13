package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.InviteContactsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.InvitesStatistics;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

import static com.topface.topface.statistics.InvitesStatistics.PLC_INVITE_POPUP;

public class InvitesPopup extends AbstractDialogFragment implements View.OnClickListener {
    public static final java.lang.String TAG = "InvitePopup";

    public static final String INVITE_POPUP_PREF_KEY = "INVITE_POPUP";
    public static final String CONTACTS = "contacts";
    private ArrayList<ContactsProvider.Contact> contacts;
    private View mLocker;

    public static InvitesPopup newInstance(ArrayList<ContactsProvider.Contact> data) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(CONTACTS, data);
        InvitesPopup popup = new InvitesPopup();
        popup.setArguments(args);
        return popup;
    }

    @Override
    protected void initViews(View root) {
        root.setClickable(true);
        final Activity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(true);
        }
        TextView invitesTitle = (TextView) root.findViewById(R.id.invitesTitle);
        int neededContact = CacheProfile.getOptions().contacts_count;
        invitesTitle.setText(Utils.getQuantityString(R.plurals.get_vip_for_invites_plurals, neededContact, neededContact));
        if (getArguments() != null) {
            contacts = getArguments().getParcelableArrayList(CONTACTS);
        } else {
            ((BaseFragmentActivity) activity).close(InvitesPopup.this, false);
        }
        invitesTitle.setOnClickListener(this);
        root.findViewById(R.id.ivClose).setOnClickListener(this);
        mLocker = root.findViewById(R.id.ipLocker);
        final Button sendContacts = (Button) root.findViewById(R.id.sendContacts);
        sendContacts.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn,
                CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
        sendContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InvitesStatistics.sendInviteBtnClickAction(PLC_INVITE_POPUP);
                sendInvitesRequest();
                if (isAdded()) {
                    ((BaseFragmentActivity) getActivity()).close(InvitesPopup.this);
                }
            }
        });

    }

    @Override
    public boolean isUnderActionBar() {
        return true;
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.invites_popup;
    }

    private void sendInvitesRequest() {
        InviteContactsRequest request = new InviteContactsRequest(getActivity(), contacts);
        final int contactsCount = contacts.size();
        FlurryManager.getInstance().sendInviteEvent(FlurryManager.PHONE_BOOK_INVITES, contactsCount);
        mLocker.setVisibility(View.VISIBLE);
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                boolean isPremium = response.getJsonResult().optBoolean("premium");
                InvitesStatistics.sendSuccessInviteResponseAction(PLC_INVITE_POPUP, isPremium, contactsCount);
                if (isPremium) {
                    InvitesStatistics.sendPremiumReceivedAction(PLC_INVITE_POPUP, CacheProfile.getOptions().premium_period);
                    Utils.showToastNotification(
                            Utils.getQuantityString(R.plurals.vip_status_period, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period),
                            Toast.LENGTH_LONG
                    );
                    CacheProfile.canInvite = false;
                } else {
                    Utils.showToastNotification(getString(R.string.invalid_contacts), Toast.LENGTH_LONG);
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                InvitesStatistics.sendFailedInviteResponseAction(PLC_INVITE_POPUP, codeError);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (isAdded()) {
                    mLocker.setVisibility(View.GONE);
                }
            }
        }).exec();
    }

    public static boolean isApplicable() {
        if (CacheProfile.canInvite) {
            final SharedPreferences preferences = App.getContext().getSharedPreferences(
                    App.PREFERENCES_TAG_SHARED,
                    Context.MODE_PRIVATE
            );
            long date_start = preferences.getLong(INVITE_POPUP_PREF_KEY, 1);
            long date_now = System.currentTimeMillis();

            if ((date_now - date_start) >= CacheProfile.getOptions().popup_timeout) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivClose:
            case R.id.invitesTitle:
                InvitesStatistics.sendCloseScreenAction(PLC_INVITE_POPUP);
                if (isAdded()) {
                    ((BaseFragmentActivity) getActivity()).close(InvitesPopup.this);
                }
                break;
            default:
                break;
        }
    }
}
