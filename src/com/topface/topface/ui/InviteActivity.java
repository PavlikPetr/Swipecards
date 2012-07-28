package com.topface.topface.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.*;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.InviteRequest;
import com.topface.topface.ui.adapters.InviteAdapter;
import com.topface.topface.utils.TriggersList;

import java.io.PrintStream;
import java.text.Format;
import java.util.Collection;
import java.util.Formatter;

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
    private InviteAdapter mAdapter;
    private TriggersList<Long, InviteRequest.Recipient> mTriggersList;
    private TextView mBonusText;
    private static final int COINS_BONUS = 10;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_invite);
        //Устанавливаем заголовок активити
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(R.string.activity_title_invite);

        mContactList = (ListView) findViewById(R.id.contactsList);
        mTriggersList = new TriggersList<Long, InviteRequest.Recipient>();
        mContactList.setOnItemClickListener(mListItemCheckListener);
        mBonusText = (TextView) findViewById(R.id.inviteBonusText);

        EditText filterText = (EditText) findViewById(R.id.searchField);
        filterText.addTextChangedListener(filterTextListener);

        setClearSearchListener(filterText);

        setContactsAdapater();
        setSendButtonListener();
    }

    private void setClearSearchListener(final EditText searchField) {
        View clearSearch = findViewById(R.id.searchClear);
        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchField.setText(null);
            }
        });
    }

    private void setSendButtonListener() {
        View sendButton = findViewById(R.id.btnInviteSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection<InviteRequest.Recipient> recipients = mTriggersList.getList();
                if (recipients.isEmpty()) {

                } else {
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
        mAdapter = new InviteAdapter(this, cursor, mTriggersList);
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
            InviteAdapter.ViewHolder holder = (InviteAdapter.ViewHolder) view.getTag();
            mTriggersList.toggle(holder.contactId, holder.recipient);
            mAdapter.notifyDataSetChanged();
            setBonusText(mTriggersList.getSize());
        }
    };

    private void setBonusText(int friendsCnt) {
        String text;
        friendsCnt = friendsCnt > 0 ? friendsCnt : 1;
        text = getResources().getQuantityString(R.plurals.invite_bonus_text, friendsCnt, friendsCnt * COINS_BONUS, friendsCnt);
        mBonusText.setText(text);
    }

}
