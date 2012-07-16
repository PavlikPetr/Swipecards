package com.topface.topface.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import com.topface.topface.R;

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
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_invite);

        mContactList = (ListView) findViewById(R.id.contactsList);
        EditText filterText = (EditText) findViewById(R.id.searchField);
        filterText.addTextChangedListener(filterTextListener);
        setContactsAdapater();
    }


    private void setContactsAdapater() {
        Cursor cursor = getContacts("");
        String[] fields = new String[] {
                ContactsContract.Data.DISPLAY_NAME
        };
        mAdapter = new SimpleCursorAdapter(this, R.layout.item_invite, cursor,
                fields, new int[] {R.id.contactName});
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
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
        };

        String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = " + IN_VISIBLE_GROUP;
        selection += " AND " + ContactsContract.Contacts.HAS_PHONE_NUMBER + " = " + HAS_PHONE_NUMBER;
        if (filter != null && !filter.equals("")) {
            selection += " AND " + ContactsContract.Contacts.DISPLAY_NAME + " LIKE '%" + filter.trim() + "%'";
        }
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        return managedQuery(uri, projection, selection, null, sortOrder);
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

}
