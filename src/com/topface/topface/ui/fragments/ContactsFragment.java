package com.topface.topface.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
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
        if (getArguments() != null) {
            data = getArguments().getParcelableArrayList(CONTACTS);
        } else {
            ((BaseFragmentActivity)getActivity()).close(this, false);
        }

        final EditText emailView = (EditText) root.findViewById(R.id.addInput);
        addButton = (Button) root.findViewById(R.id.addButton);
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
        contactsVip.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
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
            ArrayList<ContactsProvider.Contact> contacts = ((ContactsListAdapter)contactsView.getAdapter()).getOnlyChecked();
            InviteContactsRequest request = new InviteContactsRequest(getActivity(),contacts);
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
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.invalid_contacts), 2000).show();
                        if(contactsVip != null) {
                            contactsVip.setEnabled(true);
                        }
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    if(contactsVip != null) {
                        contactsVip.setEnabled(true);
                    }
                }
            }).exec();
        }
    }

    class ContactsListAdapter extends BaseAdapter {

        private ArrayList<ContactsProvider.Contact> data;
        private Context context;
        private boolean wasChanges = false;
        private int checkedCount;

        public ContactsListAdapter(Context context, ArrayList<ContactsProvider.Contact> data) {
            this.data = data;
            this.context = context;
            checkedCount = data.size();
        }

        @Override
        public int getCount() {
            return data.size();
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

            final ContactsProvider.Contact contact = data.get(data.size() - position - 1);

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
                contactsVip.setText(context.getText(R.string.get_seven_days_vip));
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

    }


}
