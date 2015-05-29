package com.topface.topface.ui.fragments;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.framework.JsonUtils;
import com.topface.topface.R;
import com.topface.topface.receivers.CatchSmsActions;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.GenerateSMSInviteRequest;
import com.topface.topface.requests.GetSMSInvitesStatusesRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.ContactsProvider;

import java.util.ArrayList;

public class SmsInviteFragment extends ContentListFragment {
    public static final String SMS_PHONE_NUMBER = "sms_phone_number";
    public static final String SMS_PHONE_ID = "sms_phone_id";
    public static final String SMS_TEXT = "sms_text";
    public static final String SMS_ID = "sms_id";
    private static final String ALL_READ_CONTACTS = "all_read_contacts";
    private static final String UPDATABLE_STATE = "updatable_state";
    private static final String OFFSET_POSITION = "offset_position";
    private static final String SCROLL_POSITION = "scroll_position";
    private static final String NO_CONTACTS_VISIBILITY = "no_contacts_visibility";
    private static final String HEADER_TEXT = "header_text";
    private final static int ONE_REQUEST_CONTACTS_LIMIT = 10;
    private boolean isUpdatable = false;
    private boolean isInProgress;
    private int mOffsetPosition;
    private int mLastContactBoxSize;
    private TextView mNoContactsAvailable;
    private TextView mHeaderText;
    private int mInvitationsSentCount;
    private int mFriendsRegisteredCount;

    private BroadcastReceiver mSMSCountersReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mInvitationsSentCount = intent.getIntExtra(CatchSmsActions.INVITATIONS_SENT_COUNT, mInvitationsSentCount);
            mFriendsRegisteredCount = intent.getIntExtra(CatchSmsActions.FRIENDS_REGISTERED_COUNT, mFriendsRegisteredCount);
            setHeaderText();
            if (null != getAdapter()) {
                ((ContactsAdapter) getAdapter()).setUserStatus(intent.getStringExtra(SMS_PHONE_ID), intent.getIntExtra(CatchSmsActions.SMS_SENT_STATUS, 0));
            }
        }
    };

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHeaderText = (TextView) view.findViewById(R.id.sms_invite_header);
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
            if (savedInstanceState.containsKey(HEADER_TEXT)) {
                mHeaderText.setText(savedInstanceState.getString(HEADER_TEXT));
            }
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
        if (null != mHeaderText) {
            outState.putString(HEADER_TEXT, mHeaderText.getText().toString());
        }
    }

    /**
     * pull user contacts
     */
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

    /**
     * validate contacts list on server side
     *
     * @param contacts list of users contacts
     */
    private void validateContactsOnServer(final ArrayList<ContactsProvider.Contact> contacts) {
        new GetSMSInvitesStatusesRequest(getActivity(), contacts).callback(new DataApiHandler<ArrayList<ContactsProvider.Contact>>() {

            @Override
            public void fail(int codeError, IApiResponse response) {

            }

            @Override
            protected void success(ArrayList<ContactsProvider.Contact> data, IApiResponse response) {
                setHeaderText();
                addContacts(data);
            }

            @Override
            protected ArrayList<ContactsProvider.Contact> parseResponse(ApiResponse response) {
                ValidatedData data = JsonUtils.fromJson(response.toString(), ValidatedData.class);
                if (null != data) {
                    mInvitationsSentCount = data.sentCount;
                    mFriendsRegisteredCount = data.registeredCount;
                }
                return addContactsStatuses(contacts, data);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                showProgress(false);
            }
        }).exec();
    }

    private ArrayList<ContactsProvider.Contact> addContactsStatuses(ArrayList<ContactsProvider.Contact> contacts, ValidatedData validatedData) {
        for (ContactsProvider.Contact contact : contacts) {
            for (ValidatedNumber validatedNumber : validatedData.items) {
                if (contact.getPhone().equals(validatedNumber.phone)) {
                    contact.setStatus(validatedNumber.status);
                    break;
                }
            }
        }
        return removeInvalidNumber(contacts);
    }

    /**
     * add validated users contacts to ContactsAdapter
     *
     * @param contacts list of validated users contacts
     */
    private void addContacts(ArrayList<ContactsProvider.Contact> contacts) {
        mOffsetPosition += mLastContactBoxSize;
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
                holder.progress = (ProgressBar) convertView.findViewById(R.id.contact_progress_bar);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (holder != null) {
                initHolder(holder, getItem(position));
            }
            return convertView;
        }

        public void setUserStatus(String phoneId, int status) {
            ContactsProvider.Contact contact = getContactById(phoneId);
            if (null != contact) {
                contact.setStatus(status);
                notifyDataSetChanged();
            }
        }

        private ContactsProvider.Contact getContactById(String id) {
            for (ContactsProvider.Contact contact : contacts) {
                if (contact.getId().equals(id)) {
                    return contact;
                }
            }
            return null;
        }

        private void setButtonVisibility(ViewHolder holder, boolean isVisible) {
            if (null != holder) {
                holder.invite.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        private void setTextVisibility(ViewHolder holder, boolean isVisible) {
            if (null != holder) {
                holder.invitedText.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        private void setProgressVisibility(ViewHolder holder, boolean isVisible) {
            if (null != holder) {
                holder.progress.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
            }
        }

        private void setButtonVisible(ViewHolder holder) {
            setButtonVisibility(holder, true);
            setTextVisibility(holder, false);
            setProgressVisibility(holder, false);
        }

        private void setTextVisible(ViewHolder holder) {
            setButtonVisibility(holder, false);
            setTextVisibility(holder, true);
            setProgressVisibility(holder, false);
        }

        private void setProgressVisible(ViewHolder holder) {
            setButtonVisibility(holder, false);
            setTextVisibility(holder, false);
            setProgressVisibility(holder, true);
        }

        private void initHolder(ViewHolder holder, final ContactsProvider.Contact contact) {
            holder.photo.setImageURI(contact.getPhoto());
            holder.name.setText(contact.getName());
            holder.phone.setText(contact.getPhone());
            PHONES_STATUSES status = getCorrelateStatus(contact.getStatus());
            switch (status) {
                case USER_IN_PROGRESS:
                    setProgressVisible(holder);
                    holder.invite.setOnClickListener(null);
                    break;
                case CAN_SEND_CONFIRMATION:
                    holder.invite.setText(status.getResourceId());
                    setButtonVisible(holder);
                    holder.invite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            contact.setStatus(PHONES_STATUSES.USER_IN_PROGRESS.getPosition());
                            notifyDataSetChanged();
                            sendRequestToGetSMSText(contact.getPhone(), contact.getId());
                        }
                    });
                    break;
                default:
                    holder.invitedText.setText(status.getResourceId());
                    holder.invite.setOnClickListener(null);
                    setTextVisible(holder);
                    break;

            }
        }

        class ViewHolder {
            ImageView photo;
            TextView name;
            TextView phone;
            Button invite;
            TextView invitedText;
            ProgressBar progress;
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

    private class SMSInvite {
        public int id;
        public String text;
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
        USER_REGISTERED(3, R.string.status_user_registered),
        USER_IN_PROGRESS(4);

        private int pos;
        private int resourceId;

        PHONES_STATUSES(int pos) {
            this(pos, 0);
        }

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

    private PHONES_STATUSES getCorrelateStatus(int pos) {
        for (PHONES_STATUSES status : PHONES_STATUSES.values()) {
            if (status.getPosition() == pos) {
                return status;
            }
        }
        return PHONES_STATUSES.INVILID_PHONE_NUMBER;
    }

    private ArrayList<ContactsProvider.Contact> removeInvalidNumber(ArrayList<ContactsProvider.Contact> contact) {
        for (int i = 0; i < contact.size(); i++) {
            if (contact.get(i).getStatus() == PHONES_STATUSES.INVILID_PHONE_NUMBER.getPosition()) {
                contact.remove(i);
                i--;
            }
        }
        return contact;
    }

    /**
     * show main or footer progressBar. If adapter is emty, main progressBar will show.
     *
     * @param visibility visibility state (visibility ? View.VISIBLE : View.GONE)
     */
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

    // отправляем запрос для получения текста смс сообщения
    private void sendRequestToGetSMSText(final String phoneNumber, final String id) {
        new GenerateSMSInviteRequest(getActivity(), phoneNumber).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                SMSInvite smsInvite = JsonUtils.fromJson(response.toString(), SMSInvite.class);
                if (null != smsInvite) {
                    sendSMSMessage(phoneNumber, id, smsInvite);
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {

            }
        }).exec();
    }

    /**
     * send sms
     *
     * @param phoneNumber destination phone number
     * @param id          phone id from contacts book
     * @param smsInvite   sms text and id from backend
     */
    private void sendSMSMessage(String phoneNumber, String id, SMSInvite smsInvite) {
        Intent sendIntent = new Intent(CatchSmsActions.FILTER_SMS_SENT);
        sendIntent.putExtra(SMS_PHONE_NUMBER, phoneNumber);
        sendIntent.putExtra(SMS_PHONE_ID, id);
        sendIntent.putExtra(SMS_TEXT, smsInvite.text);
        sendIntent.putExtra(SMS_ID, smsInvite.id);
        PendingIntent sendPI = PendingIntent.getBroadcast(getActivity(), 0,
                sendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        SmsManager sms = SmsManager.getDefault();
        if (null != sms) {
            sms.sendTextMessage(phoneNumber, null, smsInvite.text, sendPI, null);
        }
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

    private void setHeaderText() {
        if (null != mHeaderText) {
            mHeaderText.setText(getResources().getString(R.string.premium_for_invitations) + "\n" + generateText(mInvitationsSentCount, mFriendsRegisteredCount));
        }
    }

    private String generateText(int invited, int registered) {
        return String.format(
                getResources().getString(R.string.premium_for_invitations_current_status),
                invited,
                registered
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mSMSCountersReceiver, new IntentFilter(CatchSmsActions.SMS_WAS_SEND));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mSMSCountersReceiver);
    }

    @Override
    protected Integer getFooterLayout() {
        return R.layout.gridview_footer_progress_bar;
    }

    @Override
    protected Integer getHeaderLayout() {
        return R.layout.header_sms_invite;
    }

    @Override
    protected int getMainLayout() {
        return R.layout.fragment_sms_invite;
    }
}
