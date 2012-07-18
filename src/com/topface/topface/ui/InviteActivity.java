package com.topface.topface.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.InviteRequest;
import com.topface.topface.ui.adapters.ContactsListAdapter;
import com.topface.topface.utils.TriggersList;

import java.util.Collection;

/**
 * Активити с приглашением друзей в приложение
 */
public class InviteActivity extends Activity {
    /**
     * Показывать ли только видимых пользователей
     */
    public static final String IN_VISIBLE_GROUP = "'1'";
    /**
     * Показывать пользователей только с телефоном
     */
    public static final String HAS_PHONE_NUMBER = "'1'";
    private ListView mContactList;
    private View mSendButton;
    private ContactsListAdapter mAdapter;
    private TriggersList<Long, InviteRequest.Recipient> mTriggersList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_invite);

        mContactList = (ListView) findViewById(R.id.contactsList);
        mTriggersList = new TriggersList<Long, InviteRequest.Recipient>();
        mContactList.setOnItemClickListener(mListItemCheckListener);

        EditText filterText = (EditText) findViewById(R.id.searchField);
        filterText.addTextChangedListener(filterTextListener);
        setContactsAdapater();

        mSendButton = findViewById(R.id.btnInviteSend);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection<InviteRequest.Recipient> recipients = mTriggersList.getList();
                if (recipients.isEmpty()) {

                }
                else {
                    InviteRequest request = new InviteRequest(InviteActivity.this);
                    if (request.addRecipients(recipients)) {
                        request.callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) throws NullPointerException {
                                Toast.makeText(InviteActivity.this, "Не удалось отправить сообщение. Попробуйте еще раз", Toast.LENGTH_LONG);
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                                Toast.makeText(InviteActivity.this, "Не удалось отправить сообщение. Попробуйте еще раз", Toast.LENGTH_LONG);
                            }
                        });
                        request.exec();
                    }
                }
            }
        });
    }


    private void setContactsAdapater() {
        Cursor cursor = getContacts("");
        mAdapter = new ContactsListAdapter(this, cursor, mTriggersList);
        mContactList.setAdapter(mAdapter);
        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return getContacts((String) charSequence);
            }
        });
    }

    /**
     * Возвращает
     */
    private Cursor getContacts(String filter) {
        // Run query
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        };

        String selection = ContactsContract.CommonDataKinds.Phone.IN_VISIBLE_GROUP + " = " + IN_VISIBLE_GROUP;
        selection += " AND " + ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = " + HAS_PHONE_NUMBER;
        //Проверяем длинну
        selection += " AND length(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ") >= " + InviteRequest.MIN_PHONE_LENGTH;
        selection += " AND " + ContactsContract.CommonDataKinds.Phone.TYPE + " = " + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
        //Если пользователь вводит запрос на поиск, то вносим его в запрос
        if (filter != null && !filter.equals("")) {
            selection += " AND " +  getCaseFixLike(filter.trim());
        }
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, null, sortOrder);
    }

    /**
     * В связи с тем, что sqlite не имеет встроенное поддержки регистра для не ASCII символов,
     * пока используем просто более сложный запрос, который покроет большинство видов написаний
     * @param filter строка для котопрой необходимо сделать запрос в разных регистрах
     * @return Like запрос
     */
    private String getCaseFixLike(String filter) {
        StringBuilder like = new StringBuilder(ContactsContract.Contacts.DISPLAY_NAME)
                .append(" LIKE '%")
                .append(filter)
                .append("%'");
        like.append(" OR ")
                .append(ContactsContract.Contacts.DISPLAY_NAME)
                .append(" LIKE '%")
                .append(filter.toLowerCase())
                .append("%'");
        like.append(" OR ")
                .append(ContactsContract.Contacts.DISPLAY_NAME)
                .append(" LIKE '%")
                .append(filter.toUpperCase())
                .append("%'");
        like.append(" OR ")
                .append(ContactsContract.Contacts.DISPLAY_NAME)
                .append(" LIKE '%")
                .append(filter.substring(0, 1).toUpperCase())
                .append(filter.substring(1).toLowerCase())
                .append("%'");
        return like.toString();
    }


    /**
     * Листенер на изменение текста
     */
    private TextWatcher filterTextListener = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            //Если изменился текст в поле, то обновляем список
            mAdapter.getFilter().filter(charSequence);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    private AdapterView.OnItemClickListener mListItemCheckListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ContactsListAdapter.ViewHolder holder = (ContactsListAdapter.ViewHolder) view.getTag();
            mTriggersList.toggle(holder.contactId, holder.recipient);
            mAdapter.notifyDataSetChanged();
        }
    };

}
