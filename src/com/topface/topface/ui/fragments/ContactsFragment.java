package com.topface.topface.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.InviteContactsRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.LinkedList;

public class ContactsFragment extends BaseFragment{
    public static final String CONTACTS = "contacts";
    ListView contactsView;
    private static final int NEED_INVITE = 20;
    private Button addButton;
    private Button contactsVip;
    private ArrayList<ContactsProvider.Contact> data;
    private LockerView locker;

    public static ContactsFragment newInstance(ArrayList<ContactsProvider.Contact> contacts) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(CONTACTS, contacts);
        ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.contacts_layout, container, false);

        locker = (LockerView) root.findViewById(R.id.clLocker);

        ActionBar mActionBar = getActionBar(root);
        mActionBar.showBackButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        TextView title = (TextView) root.findViewById(R.id.inviteText);
        title.setText(Utils.getQuantityString(R.plurals.invite_friends_plurals, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().contacts_count, CacheProfile.getOptions().premium_period));

        ActionBar actionBar = getActionBar(root);
        actionBar.setTitleText(getString(R.string.general_invite_friends));
        actionBar.showCheckBox(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox)v;
                ((ContactsListAdapter)contactsView.getAdapter()).setAllDataChecked(checkBox.isChecked());
                ((ContactsListAdapter)contactsView.getAdapter()).changeButtonState();
            }
        }, true);

        contactsView = (ListView) root.findViewById(R.id.contactsList);
        //Получаем список контактов из аргументов. Если он не пришел, закрываем фрагмент.
        if (getArguments() != null) {
            data = getArguments().getParcelableArrayList(CONTACTS);
        } else {
            ((BaseFragmentActivity)getActivity()).close(this, false);
        }

        addButton = (Button) root.findViewById(R.id.addButton);
        final EditText emailView = (EditText) root.findViewById(R.id.addInput);
        if (data.size() > CacheProfile.getOptions().contacts_count) {
            emailView.setHint(getString(R.string.input_contact_name));
            addButton.setVisibility(View.GONE);
            emailView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((ContactsListAdapter)contactsView.getAdapter()).getFilter().filter(s);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emailView.getText().toString().equals("")) {
                    ContactsProvider.Contact contact = new ContactsProvider.Contact(emailView.getText().toString(), emailView.getText().toString(), true);
                    ((ContactsListAdapter) contactsView.getAdapter()).addFirst(contact);
                    ((ContactsListAdapter)contactsView.getAdapter()).changeButtonState();
                    emailView.setText("");
                }
            }
        });

        contactsVip = (Button) root.findViewById(R.id.contactsVip);
        if (data.size() < CacheProfile.getOptions().contacts_count) {
            contactsVip.setText(getString(R.string.general_rest_contacts, CacheProfile.getOptions().contacts_count - data.size()));
            contactsVip.setEnabled(false);
        } else {
            contactsVip.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
        }
        contactsVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactsVip.setEnabled(false);
                sendInvitesRequest();
            }
        });

        ContactsListAdapter adapter = new ContactsListAdapter(getActivity(), data);
        contactsView.setAdapter(adapter);
        return root;
    }

    private void sendInvitesRequest() {
        if (contactsView.getAdapter() != null) {
            locker.setVisibility(View.VISIBLE);
            final ArrayList<ContactsProvider.Contact> contacts = ((ContactsListAdapter)contactsView.getAdapter()).getOnlyChecked();
            InviteContactsRequest request = new InviteContactsRequest(getActivity(),contacts);
            request.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    boolean isPremium = response.jsonResult.optBoolean("premium");
                    if (isPremium) {
                        EasyTracker.getTracker().trackEvent("InvitesPopup", "SuccessWithChecked", "premiumTrue", (long)contacts.size());
                        EasyTracker.getTracker().trackEvent("InvitesPopup", "PremiumReceived", "", (long)CacheProfile.getOptions().premium_period);
                        if (getActivity() != null) {

                            Toast.makeText(getActivity(), Utils.getQuantityString(R.plurals.vip_status_period, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period), 1500).show();
                            CacheProfile.premium = true;
                            CacheProfile.canInvite = false;
                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                            getActivity().finish();
                        }
                    } else {
                        EasyTracker.getTracker().trackEvent("InvitesPopup", "SuccessWithChecked", "premiumFalse", (long)contacts.size());
                        Toast.makeText(getActivity(), getString(R.string.invalid_contacts), 2000).show();
                        if(contactsVip != null) {
                            contactsVip.setEnabled(true);
                        }
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    EasyTracker.getTracker().trackEvent("InvitesPopup", "RequestFail", Integer.toString(codeError), 0L);
                    if(contactsVip != null) {
                        contactsVip.setEnabled(true);
                    }
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    if (isAdded()) {
                        locker.setVisibility(View.GONE);
                    }
                }
            }).exec();
        }
    }

    class ContactsListAdapter extends BaseAdapter implements Filterable {

        private ArrayList<ContactsProvider.Contact> data;
        private Context context;
        private boolean wasChanges = false;
        private int checkedCount;
        private ContactsFilter filter = new ContactsFilter();
        private ArrayList<ContactsProvider.Contact> filteredContacts = new ArrayList<ContactsProvider.Contact>();

        public ContactsListAdapter(Context context, ArrayList<ContactsProvider.Contact> data) {
            this.data = data;
            filteredContacts = data;
            this.context = context;
            checkedCount = data.size();
        }

        @Override
        public int getCount() {
            return filteredContacts.size();
        }

        public void addFirst(ContactsProvider.Contact contact) {
            data.add(contact);
            wasChanges = true;
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_item_layout, parent, false);

            TextView text = (TextView) convertView.findViewById(R.id.contactName);

            final ContactsProvider.Contact contact = filteredContacts.get(filteredContacts.size() - position - 1);

            text.setText(contact.getName());

            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.contactCheckbox);
            checkBox.setChecked(contact.isChecked());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contact.setChecked(!checkBox.isChecked());
                    checkBox.setChecked(!checkBox.isChecked());
                    wasChanges = true;
                    changeButtonState();
                }


            });
            return convertView;
        }

        public void changeButtonState() {
            int rest = CacheProfile.getOptions().contacts_count - getCheckedCount();
            if (rest > 0) {
                contactsVip.setText(context.getString(R.string.general_rest_contacts, rest));
                contactsVip.setEnabled(false);
            } else {
                contactsVip.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
                contactsVip.setEnabled(true);
            }
        }

        public void setAllDataChecked(boolean checked) {
            if (data != null) {
                for (ContactsProvider.Contact contact : data) {
                    contact.setChecked(checked);
                }
                wasChanges = true;
            }
            notifyDataSetChanged();
        }

        public int getCheckedCount() {
            if (wasChanges) {
                checkedCount = 0;
                for(ContactsProvider.Contact contact : data) {
                    if(contact.isChecked()) {
                        checkedCount++;
                    }
                }
            }
            wasChanges = false;
            return checkedCount;
        }

        public ArrayList<ContactsProvider.Contact> getData() {
            return data;
        }

        public ArrayList<ContactsProvider.Contact> getOnlyChecked() {
            ArrayList<ContactsProvider.Contact> checked = new ArrayList<ContactsProvider.Contact>();
            for (ContactsProvider.Contact contact : data) {
                if(contact.isChecked()) {
                    checked.add(contact);
                }
            }
            return checked;
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

        public class ContactsFilter extends Filter{

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // TODO Auto-generated method stub

                FilterResults Result = new FilterResults();
                // if constraint is empty return the original names
                if(constraint.length() == 0 ){
                    Result.values = data;
                    Result.count = data.size();
                    return Result;
                }

                ArrayList<ContactsProvider.Contact> Filtered_Names = new ArrayList<ContactsProvider.Contact>();
                String filterString = constraint.toString().toLowerCase();
                String filterableString;

                for(int i = 0; i<data.size(); i++){
                    filterableString = data.get(i).getName();
                    if(filterableString.toLowerCase().contains(filterString)){
                        Filtered_Names.add(data.get(i));
                    }
                }
                Result.values = Filtered_Names;
                Result.count = Filtered_Names.size();

                return Result;
            }

            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                // TODO Auto-generated method stub
                filteredContacts = (ArrayList<ContactsProvider.Contact>) results.values;
                notifyDataSetChanged();
            }

        }
    }


}
