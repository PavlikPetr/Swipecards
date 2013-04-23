package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.InviteContactsRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;

public class InvitesPopup extends BaseFragment{

    public static final String CONTACTS = "contacts";
    private ArrayList<ContactsProvider.Contact> contacts;

    public static InvitesPopup newInstance(ArrayList<ContactsProvider.Contact> data) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(CONTACTS, data);
        InvitesPopup popup = new InvitesPopup();
        popup.setArguments(args);
        return popup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.invites_popup, container, false);
        init(root);
        return root;
    }

   private void init(View view) {
       ((NavigationActivity)getActivity()).setPopupVisible(true);
       final RelativeLayout invitesPopup = (RelativeLayout) view.findViewById(R.id.loInvitesPopup);
       TextView invitesTitle = (TextView) view.findViewById(R.id.invitesTitle);
       invitesTitle.setText(getString(R.string.get_vip_for_invites, CacheProfile.getOptions().contacts_count));

       if(getArguments() != null) {
           contacts = getArguments().getParcelableArrayList(CONTACTS);
       } else {
           ((BaseFragmentActivity) getActivity()).close(InvitesPopup.this, false);
       }
       invitesPopup.setVisibility(View.VISIBLE);
       final ImageView closeInvites = (ImageView) view.findViewById(R.id.closePopup);
       closeInvites.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (isAdded()) {
                   ((BaseFragmentActivity)getActivity()).close(InvitesPopup.this);
               }
           }
       });

       invitesTitle.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
                closeInvites.performClick();
           }
       });

       invitesPopup.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {}
       });
       RelativeLayout invitesText = (RelativeLayout)view.findViewById(R.id.checkboxContainer);
       final CheckBox invitesCheckBox = (CheckBox) view.findViewById(R.id.sendAllContacts);
       if (contacts.size() < CacheProfile.getOptions().contacts_count) {
           invitesCheckBox.setChecked(false);
           invitesCheckBox.setVisibility(View.GONE);
       }
//       final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.checkProgresBar);

       invitesText.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               invitesCheckBox.setChecked(!invitesCheckBox.isChecked());
           }
       });

       final Button sendContacts = (Button) invitesPopup.findViewById(R.id.sendContacts);
       sendContacts.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
       sendContacts.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (!invitesCheckBox.isChecked()) {
                   startActivity(ContainerActivity.getIntentForContacts(contacts));
                   ((BaseFragmentActivity)getActivity()).close(InvitesPopup.this);
               } else {
                   sendInvitesRequest();
               }
           }
       });

   }

   private void sendInvitesRequest() {
       InviteContactsRequest request = new InviteContactsRequest(getActivity(), contacts);
       request.callback(new ApiHandler() {
           @Override
           public void success(ApiResponse response) {
               boolean isPremium = response.jsonResult.optBoolean("premium");
               if (isPremium) {
                   if (getActivity() != null) {
                       Toast.makeText(getActivity(), Utils.getQuantityString(R.plurals.vip_status_period, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period), 1500).show();
                       CacheProfile.premium = true;
                       CacheProfile.canInvite = false;
                       LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                       ((BaseFragmentActivity)getActivity()).close(InvitesPopup.this);
                   }
               } else {
                   Toast.makeText(getActivity(), getString(R.string.invalid_contacts), 2000).show();
               }
           }

           @Override
           public void fail (int codeError, ApiResponse response){

           }
       }).exec();
   }
}
