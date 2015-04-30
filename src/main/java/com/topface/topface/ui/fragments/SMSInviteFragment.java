package com.topface.topface.ui.fragments;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.framework.JsonUtils;
import com.topface.topface.R;
import com.topface.topface.utils.ContactsProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;

public class SMSInviteFragment extends ContentListFragment {
    public static final String PHONE_NUMBER = "phone_number";
    public static final String SMS_TEXT = "sms_text";
    private static final String ALL_READ_CONTACTS = "all_read_contacts";
    private static final String UPDATABLE_STATE = "updatable_state";
    private static final String OFFSET_POSITION = "offset_position";
    private static final String SCROLL_POSITION = "scroll_position";
    private static final String NO_CONTACTS_VISIBILITY = "no_contacts_visibility";
    private final static int ONE_REQUEST_CONTACTS_LIMIT = 10;
    private static final int SMS_REQUEST_CODE = 20;
    Set<String> mInvitedIds;
    private boolean isUpdatable = false;
    private boolean isInProgress;
    private int mOffsetPosition;
    private int mLastContactBoxSize;
    private TextView mNoContactsAvailable;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNoContactsAvailable = (TextView) view.findViewById(R.id.no_contacts_available);
        isUpdatable = true;
        mOffsetPosition = 0;
        ArrayList<ContactsProvider.Contact> contacts = new ArrayList<>();
        int scrolledPosition = 0;
        if (savedInstanceState != null) {
            contacts = savedInstanceState.getParcelableArrayList(ALL_READ_CONTACTS);
            isUpdatable = savedInstanceState.getBoolean(UPDATABLE_STATE);
            mOffsetPosition = savedInstanceState.getInt(OFFSET_POSITION);
            scrolledPosition = savedInstanceState.getInt(SCROLL_POSITION);
            setNoContactsVisibility(savedInstanceState.getBoolean(NO_CONTACTS_VISIBILITY, false));
        } else {
            getContactsWithPhone();
        }
        setAdapter(new ContactsAdapter(contacts));
        getListView().setSelection(scrolledPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getAdapter() != null) {
            outState.putParcelableArrayList(ALL_READ_CONTACTS, ((ContactsAdapter) getAdapter()).getAllData());
        }
        outState.putBoolean(UPDATABLE_STATE, isUpdatable);
        outState.putInt(OFFSET_POSITION, mOffsetPosition);
        if (getListView() != null) {
            outState.putInt(SCROLL_POSITION, getListView().getFirstVisiblePosition());
        }
        outState.putBoolean(NO_CONTACTS_VISIBILITY, isNoContactsVisible());
    }

    private void getContactsWithPhone() {
        if (!isUpdatable || isInProgress) {
            if (!isInProgress) {
                showProgress(false);
            }
            return;
        }
        isInProgress = true;
        showProgress(true);
        ContactsProvider.GetContactsHandler handler = new ContactsProvider.GetContactsHandler() {
            @Override
            public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {
                mLastContactBoxSize = contacts.size();
                if (contacts.size() == 0) {
                    isUpdatable = false;
                    showProgress(false);
                    showNoContactsIfNeeded();
                } else {
                    validateContactsOnServer(contacts);
                }
            }
        };
        ContactsProvider provider = new ContactsProvider(getActivity());
        provider.getContactsWithPhones(ONE_REQUEST_CONTACTS_LIMIT, mOffsetPosition, handler);
    }

    private void validateContactsOnServer(final ArrayList<ContactsProvider.Contact> contacts) {
        testParse(contacts);
//        new GetSMSInvitesStatusesRequest(getActivity(), contacts).callback(new ApiHandler() {
//            @Override
//            public void success(IApiResponse response) {
////                ValidatedData data = JsonUtils.fromJson(response.toString(), ValidatedData.class);
////                addContactsStatuses(contacts, data);
//            }
//
//            @Override
//            public void fail(int codeError, IApiResponse response) {
//                String responseString = "";
//                try {
//                    responseString = createFakeResponse(contacts).toString();
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                ValidatedData data = JsonUtils.fromJson(responseString, ValidatedData.class);
//                addContactsStatuses(contacts, data);
//            }
//        }).exec();
    }

    // TEST
    private void testParse(ArrayList<ContactsProvider.Contact> contacts) {
        String responseString = "";
        try {
            responseString = createFakeResponse(contacts).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ValidatedData data = JsonUtils.fromJson(responseString, ValidatedData.class);
        addContactsStatuses(contacts, data);
    }

    private JSONObject createFakeResponse(ArrayList<ContactsProvider.Contact> contacts) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("items", createJsonArray(contacts));
        jsonObject.put("sentCount", 4);
        jsonObject.put("registeredCount", 1);
        return jsonObject;
    }

    private JSONArray createJsonArray(ArrayList<ContactsProvider.Contact> contacts) throws JSONException {
        JSONArray res = new JSONArray();
        for (ContactsProvider.Contact contact : contacts) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("phone", contact.getPhone());
//            jsonObject.put("status", new Random().nextInt(3));
            jsonObject.put("status", 0);
            res.put(jsonObject);
        }
        return res;
    }

    //

    private void addContactsStatuses(ArrayList<ContactsProvider.Contact> contacts, ValidatedData validatedData) {
        for (ContactsProvider.Contact contact : contacts) {
            for (ValidatedNumber validatedNumber : validatedData.items) {
                if (contact.getPhone().equals(validatedNumber.phone)) {
                    contact.setStatus(validatedNumber.status);
                    break;
                }
            }
        }
        removeInvilidNumber(contacts);
    }

    private void addContacts(ArrayList<ContactsProvider.Contact> contacts) {
        mOffsetPosition += mLastContactBoxSize;
        showProgress(false);
        if (getAdapter() != null) {
            ((ContactsAdapter) getAdapter()).addContacts(contacts);
            getAdapter().notifyDataSetChanged();
        }
        isInProgress = false;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.sms_invite_subtitle);
    }

    private class ContactsAdapter extends BaseAdapter {

        ArrayList<ContactsProvider.Contact> contacts;

        ContactsAdapter(ArrayList<ContactsProvider.Contact> contacts) {
            this.contacts = contacts;
        }

        @Override
        public int getCount() {
            return contacts != null ? contacts.size() : 0;
        }

        @Override
        public ContactsProvider.Contact getItem(int position) {
            return contacts != null ? contacts.get(position) : null;
        }

        public void addContacts(ArrayList<ContactsProvider.Contact> contacts) {
            this.contacts.addAll(contacts);
        }

        public ArrayList<ContactsProvider.Contact> getAllData() {
            return contacts;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_contact_layout, parent, false);
                holder = new ViewHolder();
                holder.photo = (ImageView) convertView.findViewById(R.id.contact_photo);
                holder.name = (TextView) convertView.findViewById(R.id.contact_name);
                holder.phone = (TextView) convertView.findViewById(R.id.contact_content);
                holder.invite = (Button) convertView.findViewById(R.id.contact_invite_button);
                holder.invitedText = (TextView) convertView.findViewById(R.id.contact_invited_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (holder != null) {
                initHolder(holder, getItem(position));
            }
            return convertView;
        }

        private void initHolder(ViewHolder holder, final ContactsProvider.Contact contact) {
            holder.photo.setImageURI(contact.getPhoto());
            holder.name.setText(contact.getName());
            holder.phone.setText(contact.getPhone());
            PHONES_STATUSES status = getSatusByPos(contact.getStatus());
            switch (status) {
                case CAN_SEND_CONFIRMATION:
                    holder.invite.setText(status.getResourceId());
                    holder.invite.setVisibility(View.VISIBLE);
                    holder.invitedText.setVisibility(View.INVISIBLE);
                    holder.invite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendContact(contact);
                        }
                    });
                    break;
                default:
                    holder.invitedText.setText(status.getResourceId());
                    holder.invite.setVisibility(View.INVISIBLE);
                    holder.invite.setOnClickListener(null);
                    holder.invitedText.setVisibility(View.VISIBLE);
                    break;

            }
        }

        class ViewHolder {
            ImageView photo;
            TextView name;
            TextView phone;
            Button invite;
            TextView invitedText;
        }
    }

    private class ValidatedData {
        public ArrayList<ValidatedNumber> items;
        public int sentCount;
        public int registeredCount;
    }

    private class ValidatedNumber {
        public String phone;
        public int status;
    }

    @Override
    protected void needToLoad() {
        super.needToLoad();
        getContactsWithPhone();
    }

    public enum PHONES_STATUSES {
        CAN_SEND_CONFIRMATION(0, R.string.status_can_send_confirmation),
        INVILID_PHONE_NUMBER(1, R.string.status_invilid_phone_number),
        CONFIRMATION_WAS_SENT(2, R.string.status_confirmation_was_sent),
        USER_REGISTERED(3, R.string.status_user_registered);

        private int pos;
        private int resourceId;

        PHONES_STATUSES(int pos, int resourceId) {
            this.pos = pos;
            this.resourceId = resourceId;
        }

        public int getPosition() {
            return pos;
        }

        public int getResourceId() {
            return resourceId;
        }
    }

    private PHONES_STATUSES getSatusByPos(int pos) {
        for (PHONES_STATUSES status : PHONES_STATUSES.values()) {
            if (status.getPosition() == pos) {
                return status;
            }
        }
        return PHONES_STATUSES.INVILID_PHONE_NUMBER;
    }

    private void removeInvilidNumber(ArrayList<ContactsProvider.Contact> contact) {
        new removeInvilidNumberAsync().execute(contact);
    }

    private class removeInvilidNumberAsync extends AsyncTask<ArrayList<ContactsProvider.Contact>, Void, ArrayList<ContactsProvider.Contact>> {
        @Override
        protected void onPostExecute(ArrayList<ContactsProvider.Contact> contacts) {
            super.onPostExecute(contacts);
            addContacts(contacts);
        }

        @Override
        protected final ArrayList<ContactsProvider.Contact> doInBackground(ArrayList<ContactsProvider.Contact>... params) {
            ArrayList<ContactsProvider.Contact> contacts = params[0];
            for (int i = 0; i < contacts.size(); i++) {
                if (contacts.get(i).getStatus() == PHONES_STATUSES.INVILID_PHONE_NUMBER.getPosition()) {
                    contacts.remove(i);
                    i--;
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return contacts;
        }
    }

    private void showProgress(boolean visibility) {
        if (getAdapter() == null || getAdapter().getCount() == 0) {
            showMainProgressBar(visibility);
            showFooterProgressBar(false);
        } else {
            showMainProgressBar(false);
            if (isUpdatable) {
                showFooterProgressBar(visibility);
            } else {
                showFooterProgressBar(false);
            }
        }
    }

    private void sendContact(ContactsProvider.Contact contact) {
        Intent sentIntent = new Intent(CatchSMSActions.FILTER_SMS_SENT);
        String num = "+79990362102";
        String text = "hi";
        sentIntent.putExtra(PHONE_NUMBER, num);
        sentIntent.putExtra(SMS_TEXT, text);
        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0,
                sentIntent, 0);
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(num, null, text, sentPI, null);
    }

    private void setNoContactsVisibility(boolean visibility) {
        if (mNoContactsAvailable != null) {
            mNoContactsAvailable.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    private void showNoContactsIfNeeded() {
        if (getAdapter() != null) {
            ArrayList<ContactsProvider.Contact> contacts = ((ContactsAdapter) getAdapter()).getAllData();
            setNoContactsVisibility(null == contacts || contacts.size() == 0);
        }
    }

    private boolean isNoContactsVisible() {
        return mNoContactsAvailable != null && mNoContactsAvailable.getVisibility() == View.VISIBLE;
    }
}
