package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.InviteContactsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContactsActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;

public class ContactsFragment extends BaseFragment {
    ListView contactsView;
    private Button addButton;
    private Button mContactsVip;
    private ArrayList<ContactsProvider.Contact> data;
    private View mLockerView;
    private CheckBox mCheckBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.contacts_layout, container, false);
        mLockerView = root.findViewById(R.id.clLocker);

        TextView title = (TextView) root.findViewById(R.id.inviteText);
        title.setText(Utils.getQuantityString(R.plurals.invite_friends_plurals, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().contacts_count, CacheProfile.getOptions().premium_period));

        contactsView = (ListView) root.findViewById(R.id.contactsList);
        //Получаем список контактов из аргументов. Если он не пришел, закрываем фрагмент.
        Bundle extras = getArguments();
        if (extras != null) {
            data = extras.getParcelableArrayList(ContactsActivity.CONTACTS_DATA);
        } else {
            ((BaseFragmentActivity) getActivity()).close(this, false);
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
                    ((ContactsListAdapter) contactsView.getAdapter()).getFilter().filter(s);
                    if (mCheckBox != null) {
                        mCheckBox.setVisibility(count != 0 ? View.GONE : View.VISIBLE);
                    }
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
                    ((ContactsListAdapter) contactsView.getAdapter()).changeButtonState();
                    emailView.setText("");
                }
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mContactsVip = (Button) root.findViewById(R.id.contactsVip);
        if (data.size() < CacheProfile.getOptions().contacts_count) {
            mContactsVip.setText(getString(R.string.general_rest_contacts, CacheProfile.getOptions().contacts_count - data.size()));
            mContactsVip.setEnabled(false);
        } else {
            mContactsVip.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
        }
        mContactsVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContactsVip.setEnabled(false);
                sendInvitesRequest();
            }
        });

        ContactsListAdapter adapter = new ContactsListAdapter(getActivity(), data);
        contactsView.setAdapter(adapter);
        return root;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void sendInvitesRequest() {
        if (contactsView.getAdapter() != null) {
            mLockerView.setVisibility(View.VISIBLE);
            final ArrayList<ContactsProvider.Contact> contacts = ((ContactsListAdapter) contactsView.getAdapter()).getOnlyChecked();
            InviteContactsRequest request = new InviteContactsRequest(getActivity(), contacts);
            request.callback(new ApiHandler() {
                @Override
                public void success(IApiResponse response) {
                    boolean isPremium = response.getJsonResult().optBoolean("premium");
                    if (isPremium) {
                        EasyTracker.sendEvent("InvitesPopup", "SuccessWithChecked", "premiumTrue", (long) contacts.size());
                        EasyTracker.sendEvent("InvitesPopup", "PremiumReceived", "", (long) CacheProfile.getOptions().premium_period);
                        App.sendProfileAndOptionsRequests(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                Activity activity = getActivity();
                                if (activity != null) {
                                    Toast.makeText(activity, Utils.getQuantityString(R.plurals.vip_status_period, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period), Toast.LENGTH_SHORT).show();
                                    activity.finish();
                                }
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                unlockUi();
                            }
                        });
                    } else {
                        EasyTracker.sendEvent("InvitesPopup", "SuccessWithChecked", "premiumFalse", (long) contacts.size());
                        Toast.makeText(getActivity(), getString(R.string.invalid_contacts), Toast.LENGTH_LONG).show();
                        if (mContactsVip != null) {
                            mContactsVip.setEnabled(true);
                        }
                        unlockUi();
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    EasyTracker.sendEvent("InvitesPopup", "RequestFail", Integer.toString(codeError), 0L);
                    if (mContactsVip != null) {
                        mContactsVip.setEnabled(true);
                    }
                    unlockUi();
                }
            }).exec();
        }
    }

    private void unlockUi() {
        if (isAdded()) {
            mLockerView.setVisibility(View.GONE);
        }
    }

    class ContactsListAdapter extends BaseAdapter implements Filterable {

        private ArrayList<ContactsProvider.Contact> mData;
        private Context mContext;
        private boolean mWasChanges = false;
        private int mCheckedCount;
        private ContactsFilter filter = new ContactsFilter();
        private ArrayList<ContactsProvider.Contact> filteredContacts = new ArrayList<>();

        public ContactsListAdapter(Context context, ArrayList<ContactsProvider.Contact> data) {
            mData = data;
            filteredContacts = data;
            mContext = context;
            mCheckedCount = data.size();
        }

        @Override
        public int getCount() {
            return filteredContacts.size();
        }

        public void addFirst(ContactsProvider.Contact contact) {
            mData.add(contact);
            mWasChanges = true;
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_item_layout, parent, false);
            // text
            TextView text = (TextView) convertView.findViewById(R.id.contactName);
            final ContactsProvider.Contact contact = filteredContacts.get(filteredContacts.size() - position - 1);
            text.setText(contact.getName());
            // checkbox
            final CheckBox currCheckBox = (CheckBox) convertView.findViewById(R.id.contactCheckbox);
            currCheckBox.setChecked(contact.isChecked());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    contact.setChecked(!currCheckBox.isChecked());
                    currCheckBox.setChecked(!currCheckBox.isChecked());
                    mWasChanges = true;
                    changeButtonState();
                }


            });
            return convertView;
        }

        public void changeButtonState() {
            int rest = CacheProfile.getOptions().contacts_count - getCheckedCount();
            if (rest > 0) {
                mContactsVip.setText(mContext.getString(R.string.general_rest_contacts, rest));
                mContactsVip.setEnabled(false);
            } else {
                mContactsVip.setText(Utils.getQuantityString(R.plurals.vip_status_period_btn, CacheProfile.getOptions().premium_period, CacheProfile.getOptions().premium_period));
                mContactsVip.setEnabled(true);
            }
            if (mCheckBox != null) {
                mCheckBox.setChecked(getCheckedCount() >= mData.size());
            }
        }

        public void setAllDataChecked(boolean checked) {
            if (mData != null) {
                for (ContactsProvider.Contact contact : filteredContacts) {
                    contact.setChecked(checked);
                }
                mWasChanges = true;
            }
            notifyDataSetChanged();
        }

        public int getCheckedCount() {
            if (mWasChanges) {
                mCheckedCount = 0;
                for (ContactsProvider.Contact contact : mData) {
                    if (contact.isChecked()) {
                        mCheckedCount++;
                    }
                }
            }
            mWasChanges = false;
            return mCheckedCount;
        }

        public ArrayList<ContactsProvider.Contact> getData() {
            return mData;
        }

        public ArrayList<ContactsProvider.Contact> getOnlyChecked() {
            ArrayList<ContactsProvider.Contact> checked = new ArrayList<>();
            for (ContactsProvider.Contact contact : mData) {
                if (contact.isChecked()) {
                    checked.add(contact);
                }
            }
            return checked;
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

        public class ContactsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults Result = new FilterResults();
                // if constraint is empty return the original names
                if (constraint.length() == 0) {
                    Result.values = mData;
                    Result.count = mData.size();
                    return Result;
                }

                ArrayList<ContactsProvider.Contact> Filtered_Names = new ArrayList<>();
                String filterString = constraint.toString().toLowerCase();
                String filterableString;

                for (ContactsProvider.Contact aData : mData) {
                    filterableString = aData.getName();
                    if (filterableString.toLowerCase().contains(filterString)) {
                        Filtered_Names.add(aData);
                    }
                }
                Result.values = Filtered_Names;
                Result.count = Filtered_Names.size();

                return Result;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredContacts = (ArrayList<ContactsProvider.Contact>) results.values;
                notifyDataSetChanged();
            }

        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_invite_friends);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        final MenuItem selectAllMenuItem = menu.findItem(R.id.action_select_all);
        mCheckBox = (CheckBox) MenuItemCompat.getActionView(selectAllMenuItem).findViewById(R.id.cbCheckBox);
        selectAllMenuItem.setChecked(true);
        mCheckBox.setChecked(true);
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(selectAllMenuItem);
            }
        });
        mCheckBox.setOnClickListener(new CompoundButton.OnClickListener() {
            @Override
            public void onClick(View buttonView) {
                ContactsListAdapter adapter = (ContactsListAdapter) contactsView.getAdapter();
                adapter.setAllDataChecked(mCheckBox.isChecked());
                adapter.changeButtonState();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                ((CheckBox) MenuItemCompat.getActionView(item).findViewById(R.id.cbCheckBox)).setChecked(checked);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_invite_contacts;
    }
}
