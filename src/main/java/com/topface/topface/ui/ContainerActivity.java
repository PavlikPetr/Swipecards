package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;

import com.topface.billing.BillingFragment;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.ComplainsFragment;
import com.topface.topface.ui.fragments.ContactsFragment;
import com.topface.topface.ui.fragments.EditorBannersFragment;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.RecoverPwdFragment;
import com.topface.topface.ui.fragments.RegistrationFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.buy.CoinsSubscriptionsFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ContainerActivity extends CustomTitlesBaseFragmentActivity implements IUserOnlineListener {
    public static final String UPDATE_USER_CATEGORY = "com.topface.topface.action.USER_CATEGORY";

    public static final String CONTACTS_DATA = "contacts_data";
    public static final String INTENT_USERID = "INTENT_USERID";
    public static final String FEED_ID = "FEED_ID";
    public static final String FEED_IDS = "FEED_IDS";
    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */
    public static final int INTENT_BUY_VIP_FRAGMENT = 1;
    public static final int INTENT_BUYING_FRAGMENT = 2;
    public static final int INTENT_REGISTRATION_FRAGMENT = 4;
    public static final int INTENT_RECOVER_PASSWORD = 5;
    public static final int INTENT_SETTINGS_FRAGMENT = 7;
    public static final int INTENT_CONTACTS_FRAGMENT = 8;
    public static final int INTENT_COMPLAIN_FRAGMENT = 9;
    public static final int INTENT_COINS_SUBSCRIPTION_FRAGMENT = 10;
    // Id для админки начиная со 101
    public static final int INTENT_EDITOR_BANNERS = 101;
    public static final int INTENT_PROFILE_FRAGMENT = 6;
    public static final String TYPE = "type";
    public static final String CHANGED = "changed";
    public static final String VALUE = "value";
    private int mCurrentFragmentId = -1;
    private Fragment mCurrentFragment;
    private View mOnlineIcon;

    public static Intent getNewIntent(int code) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, code);
        return intent;
    }

    public static Intent getIntentForContacts(ArrayList<ContactsProvider.Contact> data) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CONTACTS_FRAGMENT);
        intent.putParcelableArrayListExtra(CONTACTS_DATA, data);
        return intent;
    }

    public static Intent getProfileIntent(int userId, Context context) {
        return getProfileIntent(userId, null, Static.EMPTY, context);
    }

    public static Intent getProfileIntent(int userId, String itemId, Context context) {
        return getProfileIntent(userId, itemId, null, context);
    }

    public static Intent getProfileIntent(int userId, Class callingClass, Context context) {
        return getProfileIntent(userId, null, callingClass.getName(), context);
    }

    public static Intent getProfileIntent(int userId, String itemId, String className, Context context) {
        Intent i = new Intent(context, ContainerActivity.class);
        i.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        if (className != null) {
            i.putExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT, className);
        }
        if (itemId != null) {
            i.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        i.putExtra(Static.INTENT_REQUEST_KEY, INTENT_PROFILE_FRAGMENT);
        return i;
    }

    public static Intent getComplainIntent(int userId) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_COMPLAIN_FRAGMENT);
        intent.putExtra(INTENT_USERID, userId);
        return intent;
    }

    public static Intent getComplainIntent(int userId, String feedId) {
        Intent intent = getComplainIntent(userId);
        intent.putExtra(FEED_ID, feedId);
        return intent;
    }

    public static Intent getVipBuyIntent(String extraText, String from) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY_VIP_FRAGMENT);
        intent.putExtra(VipBuyFragment.ARG_TAG_EXRA_TEXT, extraText);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        return intent;
    }

    public static Intent getBuyingIntent(String from, int itemType, int itemPrice) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUYING_FRAGMENT);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        if (itemType != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_TYPE, itemType);
        }
        if (itemPrice != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_PRICE, itemPrice);
        }
        return intent;
    }

    public static Intent getBuyingIntent(String from, int itemPrice) {
        return getBuyingIntent(from, -1, itemPrice);

    }

    public static Intent getBuyingIntent(String from) {
        return getBuyingIntent(from, -1, -1);

    }

    public static Intent getCoinsSubscriptionIntent(String from) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_COINS_SUBSCRIPTION_FRAGMENT);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        return intent;

    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initRequestKey();
        checkAuth();
        setContentView(R.layout.ac_fragment_frame);
        //Сперва пробуем получуить существующий фрагмент из fragmentManager
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.loFrame);
        if (mCurrentFragment == null) {
            //Если не находим, то создаем новый
            mCurrentFragment = getNewFragment(mCurrentFragmentId);
        }
        if (mCurrentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(
                            R.id.loFrame,
                            mCurrentFragment
                    ).commit();
        }
    }

    private void checkAuth() {
        //Если нужно авторизоваться, то возвращаемся на NavigationActivity
        if (isNeedAuth() && AuthToken.getInstance().isEmpty()) {
            //Если это последняя активити в таске, то создаем NavigationActivity
            if (isTaskRoot()) {
                Intent i = new Intent(this, NavigationActivity.class);
                startActivity(i);
            }
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {
        mOnlineIcon = mCustomView.findViewById(R.id.online);
    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_container_title_view;
    }

    private void initRequestKey() {
        if (mCurrentFragmentId == -1) {
            Intent intent = getIntent();
            try {
                mCurrentFragmentId = intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0);
            } catch (Exception ex) {
                Debug.error(ex);
                finish();
            }

            if (BuildConfig.DEBUG && mCurrentFragmentId <= 0) {
                throw new IllegalArgumentException(
                        "ContainerActivity needs request code, use static ContainerActivity methods to get Intents"
                );
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(@NotNull Bundle savedInstanceState) {
        FragmentManager manager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mCurrentFragment = manager.findFragmentById(R.id.loFrame);
        }
    }

    private Fragment getNewFragment(int id) {
        Fragment fragment = null;
        Intent intent = getIntent();

        Bundle extras = getIntent().getExtras();
        String source = intent.getStringExtra(BillingFragment.ARG_TAG_SOURCE);
        switch (id) {
            case INTENT_BUY_VIP_FRAGMENT:

                fragment = PurchasesFragment.newInstance(
                        intent.getStringExtra(VipBuyFragment.ARG_TAG_EXRA_TEXT),
                        intent.getStringExtra(BillingFragment.ARG_TAG_SOURCE)
                );
                break;
            case INTENT_BUYING_FRAGMENT:
                if (extras != null && extras.containsKey(PurchasesFragment.ARG_ITEM_TYPE)
                        && extras.containsKey(PurchasesFragment.ARG_ITEM_PRICE)) {
                    fragment = PurchasesFragment.newInstance(
                            extras.getInt(PurchasesFragment.ARG_ITEM_TYPE),
                            extras.getInt(PurchasesFragment.ARG_ITEM_PRICE),
                            source
                    );
                } else {
                    fragment = PurchasesFragment.newInstance(source);
                }
                break;
            case INTENT_COINS_SUBSCRIPTION_FRAGMENT:
                fragment = CoinsSubscriptionsFragment.newInstance(source);
                break;
            case INTENT_REGISTRATION_FRAGMENT:
                getSupportActionBar().hide();
                fragment = new RegistrationFragment();
                break;
            case INTENT_RECOVER_PASSWORD:
                getSupportActionBar().hide();
                fragment = new RecoverPwdFragment();
                break;
            case INTENT_PROFILE_FRAGMENT:
                //Открываем профиль
                fragment = UserProfileFragment.newInstance(
                        intent.getStringExtra(AbstractProfileFragment.INTENT_ITEM_ID),
                        intent.getIntExtra(AbstractProfileFragment.INTENT_UID, 0),
                        intent.getStringExtra(AbstractProfileFragment.INTENT_CALLING_FRAGMENT),
                        intent.getBooleanExtra(UserProfileFragment.IGNORE_SYMPATHY_SENT_EXTRA, false)

                );
                break;
            case INTENT_SETTINGS_FRAGMENT:
                fragment = new SettingsFragment();
                break;
            case INTENT_CONTACTS_FRAGMENT:
                ArrayList<ContactsProvider.Contact> contacts = intent.getParcelableArrayListExtra(CONTACTS_DATA);
                fragment = ContactsFragment.newInstance(contacts);
                break;
            case INTENT_COMPLAIN_FRAGMENT:
                int userId = intent.getIntExtra(INTENT_USERID, 0);
                String feedId = intent.getStringExtra(FEED_ID);
                if (feedId != null) {
                    fragment = ComplainsFragment.newInstance(userId, feedId);
                } else {
                    fragment = ComplainsFragment.newInstance(userId);
                }
                break;
            case INTENT_EDITOR_BANNERS:
                fragment = new EditorBannersFragment();
                break;
        }

        return fragment;
    }

    public enum ActionTypes {BLACK_LIST, BOOKMARK}

    public static Intent getIntentForActionsUpdate(ActionTypes type, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(CHANGED, value);
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int userId, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_ID, userId);
        intent.putExtra(FEED_IDS, new int[]{userId});
        return intent;
    }

    public static Intent getValuedActionsUpdateIntent(ActionTypes type, int[] userIds, boolean value) {
        Intent intent = new Intent(UPDATE_USER_CATEGORY);
        intent.putExtra(TYPE, type);
        intent.putExtra(VALUE, value);
        intent.putExtra(FEED_IDS, userIds);
        return intent;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @Override
    protected boolean isNeedAuth() {
        return mCurrentFragmentId != INTENT_REGISTRATION_FRAGMENT &&
                mCurrentFragmentId != INTENT_RECOVER_PASSWORD && super.isNeedAuth();
    }

    @Override
    public void setUserOnline(boolean online) {
        if (mOnlineIcon != null) {
            mOnlineIcon.setVisibility(online ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mCurrentFragment.onOptionsItemSelected(item);
                if (isTaskRoot()) {
                    Intent i = new Intent(this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
