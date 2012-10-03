package com.topface.topface.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.google.android.apps.analytics.easytracking.TrackedActivity;
import com.topface.topface.R;
import com.topface.topface.data.Invite;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.InviteRequest;
import com.topface.topface.ui.adapters.InviteAdapter;
import com.topface.topface.utils.TriggersList;
import com.topface.topface.utils.Utils;

import java.util.Collection;

/**
 * Активити с приглашением друзей в приложение
 */
public class InviteActivity extends TrackedActivity {
    /**
     * Показывать ли только видимых пользователей
     */
    public static final String IN_VISIBLE_GROUP = "'1'";
    /**
     * Показывать пользователей только с телефоном
     */
    public static final String HAS_PHONE_NUMBER = "'1'";
    /**
     * Бонус в монетах за каждого приглашенного друга
     */
    private static final int COINS_BONUS = 10;

    private ListView mContactList;
    private InviteAdapter mAdapter;
    private TriggersList<Long, InviteRequest.Recipient> mTriggersList;
    private TextView mBonusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_invite);
        //Устанавливаем заголовок активити
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(R.string.activity_title_invite);

        mContactList = (ListView) findViewById(R.id.contactsList);
        mContactList.setOnItemClickListener(mListItemCheckListener);

        //Здесь будем хранить все выделенные элементы списка
        mTriggersList = new TriggersList<Long, InviteRequest.Recipient>();
        //Текст с информацией о бонусах
        mBonusText = (TextView) findViewById(R.id.inviteBonusText);
        //Ставим текст по умолчанию
        setBonusText(0);

        //Поле поиска
        EditText filterText = (EditText) findViewById(R.id.searchField);
        filterText.addTextChangedListener(filterTextListener);

        //Кнопка очистки поля поиска
        setClearSearchListener(filterText);

        setContactsAdapater();
        //Кнопка отправки приглашений
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

    /**
     * Устанавливает Листенер нажатия на кнопку "отправить
     */
    private void setSendButtonListener() {
        View sendButton = findViewById(R.id.btnInviteSend);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection<InviteRequest.Recipient> recipients = mTriggersList.getList();
                if (recipients.isEmpty()) {
                    Toast.makeText(InviteActivity.this, R.string.invite_completed, Toast.LENGTH_LONG);
                } else {
                    InviteRequest request = new InviteRequest(InviteActivity.this);
                    if (request.addRecipients(recipients)) {
                        request.callback(new ApiHandler() {
                            @Override
                            public void success(ApiResponse response) throws NullPointerException {
                                if (Invite.parse(response).completed) {
                                    Toast.makeText(InviteActivity.this, R.string.invite_completed, Toast.LENGTH_LONG);
                                }
                                else {
                                    onInviteError();
                                }
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                                onInviteError();
                            }
                        });
                        request.exec();
                    }
                }
            }
        });
    }

    /**
     * Вызывается при ошибке отправки инвайта на сервер
     */
    private void onInviteError() {
        Toast.makeText(this, R.string.invite_error, Toast.LENGTH_LONG);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdapter.getCursor().isClosed()) {
            setContactsAdapater();
        }
    }

    /**
     * Устанавливает курсор в адаптер и адаптер в ListView
     */
    private void setContactsAdapater() {
        Cursor cursor = getContacts(null);
        mAdapter = new InviteAdapter(this, cursor, mTriggersList);
        mContactList.setAdapter(mAdapter);
        //Указываем адаптеру, как производить запрос при установке на него фильтра
        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return getContacts((String) charSequence);
            }
        });
    }

    /**
     * Возвращает курсор контактов с учетом фильтра и всех настроек выборки
     *
     * @param filter подстрока имени контакта по которой производится поиск
     * @return курсок к базе из ContactsCobtract
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
     *
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
            //Если изменился текст в поле поиска, то обновляем список
            mAdapter.getFilter().filter(charSequence);
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {}
    };

    /**
     * Листенер нажатия на элемент списка
     */
    private AdapterView.OnItemClickListener mListItemCheckListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //При клике на элемент списка, переключаем его состояние
            InviteAdapter.ViewHolder holder = (InviteAdapter.ViewHolder) view.getTag();
            mTriggersList.toggle(holder.contactId, holder.recipient);
            //Не забываем обновить view списка
            mAdapter.notifyDataSetChanged();
            //Сообщение о бонусе
            setBonusText(mTriggersList.getSize());
        }
    };

    /**
     * В зависимости от количества выделенных контактов, меняем текст сообщения
     * @param friendsCnt количество выделенных контактов в списке
     */
    private void setBonusText(int friendsCnt) {
        String text;
        int multiplicator = friendsCnt > 0 ? friendsCnt : 1;

        text = Utils.getQuantityString(R.plurals.invite_bonus_text, friendsCnt, multiplicator * COINS_BONUS, friendsCnt);
        mBonusText.setText(text);
    }

}
