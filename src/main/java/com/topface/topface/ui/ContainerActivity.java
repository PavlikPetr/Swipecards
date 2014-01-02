package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;

import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.BuyingFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.ComplainsFragment;
import com.topface.topface.ui.fragments.ContactsFragment;
import com.topface.topface.ui.fragments.EditorBannersFragment;
import com.topface.topface.ui.fragments.ProfileFragment;
import com.topface.topface.ui.fragments.RecoverPwdFragment;
import com.topface.topface.ui.fragments.RegistrationFragment;
import com.topface.topface.ui.fragments.SettingsFragment;
import com.topface.topface.ui.fragments.VipBuyFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.ContactsProvider;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;

public class ContainerActivity extends CustomTitlesBaseFragmentActivity implements ChatFragment.IUserOnlineListener{

    public static final String CONTACTS_DATA = "contacts_data";
    public static final String INTENT_USERID = "INTENT_USERID";
    public static final String FEED_ID = "FEED_ID";

    private int mCurrentFragmentId = -1;
    private Fragment mCurrentFragment;

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */
    public static final int INTENT_BUY_VIP_FRAGMENT = 1;
    public static final int INTENT_BUYING_FRAGMENT = 2;
    public static final int INTENT_CHAT_FRAGMENT = 3;
    public static final int INTENT_REGISTRATION_FRAGMENT = 4;
    public static final int INTENT_RECOVER_PASSWORD = 5;
    private static final int INTENT_PROFILE_FRAGMENT = 6;
    public static final int INTENT_SETTINGS_FRAGMENT = 7;
    public static final int INTENT_CONTACTS_FRAGMENT = 8;
    public static final int INTENT_COMPLAIN_FRAGMENT = 9;

    // Id для админки начиная со 101
    public static final int INTENT_EDITOR_BANNERS = 101;

    private View mOnlineIcon;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initRequestKey();
        checkAuth();
        setContentView(R.layout.fragment_frame);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left);

        //Сперва пробуем
        mCurrentFragment = getSupportFragmentManager().findFragmentById(R.id.loFrame);
        if (mCurrentFragment == null) {
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
        setRotationMode();
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

            if (App.DEBUG && mCurrentFragmentId <= 0) {
                throw new IllegalArgumentException("ContainerActivity needs request code, use static ContainerActivity methods to get Intents");
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        FragmentManager manager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mCurrentFragment = manager.findFragmentById(R.id.loFrame);
        }
    }

    private Fragment getNewFragment(int id) {
        Fragment fragment = null;
        Intent intent = getIntent();

        switch (id) {
            case INTENT_BUY_VIP_FRAGMENT:
                fragment = VipBuyFragment.newInstance(
                        true,
                        intent.getStringExtra(VipBuyFragment.ARG_TAG_EXRA_TEXT),
                        intent.getStringExtra(BillingFragment.ARG_TAG_SOURCE)
                );
                break;
            case INTENT_BUYING_FRAGMENT:
                Bundle extras = getIntent().getExtras();
                String source = intent.getStringExtra(BillingFragment.ARG_TAG_SOURCE);
                if (extras.containsKey(BuyingFragment.ARG_ITEM_TYPE) && extras.containsKey(BuyingFragment.ARG_ITEM_PRICE)) {
                    fragment = BuyingFragment.newInstance(
                            extras.getInt(BuyingFragment.ARG_ITEM_TYPE),
                            extras.getInt(BuyingFragment.ARG_ITEM_PRICE),
                            source
                    );
                } else {
                    fragment = BuyingFragment.newInstance(source);
                }
                break;
            case INTENT_CHAT_FRAGMENT:
                fragment = ChatFragment.newInstance(intent.getStringExtra(ChatFragment.INTENT_ITEM_ID),
                        intent.getIntExtra(ChatFragment.INTENT_USER_ID, -1),
                        false,
                        intent.getIntExtra(ChatFragment.INTENT_USER_SEX, Static.BOY),
                        intent.getStringExtra(ChatFragment.INTENT_USER_NAME),
                        intent.getIntExtra(ChatFragment.INTENT_USER_AGE, 0),
                        intent.getStringExtra(ChatFragment.INTENT_USER_CITY),
                        intent.getStringExtra(BaseFragmentActivity.INTENT_PREV_ENTITY));
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
                fragment = ProfileFragment.newInstance(
                        intent.getStringExtra(ProfileFragment.INTENT_ITEM_ID),
                        intent.getIntExtra(ProfileFragment.INTENT_UID, 0),
                        intent.getIntExtra(ProfileFragment.INTENT_TYPE, ProfileFragment.TYPE_MY_PROFILE),
                        intent.getStringExtra(ProfileFragment.INTENT_CALLING_FRAGMENT)

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

    private void setRotationMode() {
        if (mCurrentFragmentId == INTENT_CHAT_FRAGMENT) {
            int rotationStatus = Settings.System.getInt(
                    getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION,
                    1
            );

            if (rotationStatus == 1) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

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
        int type = (userId == CacheProfile.uid) ?
                ProfileFragment.TYPE_MY_PROFILE :
                ProfileFragment.TYPE_USER_PROFILE;

        Intent i = new Intent(context, ContainerActivity.class);
        i.putExtra(ProfileFragment.INTENT_UID, userId);
        i.putExtra(ProfileFragment.INTENT_TYPE, type);
        if (className != null) {
            i.putExtra(ProfileFragment.INTENT_CALLING_FRAGMENT, className);
        }
        if (itemId != null) {
            i.putExtra(ProfileFragment.INTENT_ITEM_ID, itemId);
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

    public static Intent getBuyingIntent(String from) {
        Intent intent = new Intent(App.getContext(), ContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUYING_FRAGMENT);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        return intent;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setUserOnline(boolean online) {
        if (mOnlineIcon != null) {
            mOnlineIcon.setVisibility(online ? View.VISIBLE : View.INVISIBLE);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
