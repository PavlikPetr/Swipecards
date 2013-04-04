package com.topface.topface.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.*;
import com.topface.topface.utils.Debug;

public class ContainerActivity extends BaseFragmentActivity {

    private int mCurrentFragmentId = -1;
    private Fragment mCurrentFragment;

    private static final String TAG_FRAGMENT = "current_fragment";

    public static final int INTENT_BUY_VIP_FRAGMENT = 1;
    public static final int INTENT_BUYING_FRAGMENT = 2;
    public static final int INTENT_CHAT_FRAGMENT = 3;
    public static final int INTENT_REGISTRATION_FRAGMENT = 4;
    public static final int INTENT_RECOVER_PASSWORD = 5;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.fragment_frame);

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left);

        initRequestKey();
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentFragment == null) {
            mCurrentFragment = getFragment(mCurrentFragmentId);
        }
        setRotationMode();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mCurrentFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.loFrame, mCurrentFragment, TAG_FRAGMENT).commit();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        FragmentManager manager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            mCurrentFragment = manager.findFragmentByTag(TAG_FRAGMENT);
        }
    }

    private Fragment getFragment(int id) {
        Fragment fragment = null;
        switch (id) {
            case INTENT_BUY_VIP_FRAGMENT:
                fragment = VipBuyFragment.newInstance(true);
                break;
            case INTENT_BUYING_FRAGMENT:
                Bundle extras = getIntent().getExtras();
                if (extras.containsKey(BuyingFragment.ARG_ITEM_TYPE) && extras.containsKey(BuyingFragment.ARG_ITEM_PRICE)) {
                    fragment = BuyingFragment.newInstance(extras.getInt(BuyingFragment.ARG_ITEM_TYPE),
                            extras.getInt(BuyingFragment.ARG_ITEM_PRICE));
                } else {
                    fragment = BuyingFragment.newInstance();
                }
                break;
            case INTENT_CHAT_FRAGMENT:
                Intent intent = getIntent();

                fragment = ChatFragment.newInstance(intent.getIntExtra(ChatFragment.INTENT_ITEM_ID, -1),
                        intent.getIntExtra(ChatFragment.INTENT_USER_ID, -1),
                        false,
                        intent.getIntExtra(ChatFragment.INTENT_USER_SEX, Static.BOY),
                        intent.getStringExtra(ChatFragment.INTENT_USER_NAME),
                        intent.getIntExtra(ChatFragment.INTENT_USER_AGE, 0),
                        intent.getStringExtra(ChatFragment.INTENT_USER_CITY),
                        intent.getStringExtra(BaseFragmentActivity.INTENT_PREV_ENTITY));

                break;
            case INTENT_REGISTRATION_FRAGMENT:
                fragment = new RegistrationFragment();
                break;
            case INTENT_RECOVER_PASSWORD:
                fragment = new RecoverPwdFragment();
                break;
            default:
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
        initRequestKey();
        return mCurrentFragmentId != INTENT_REGISTRATION_FRAGMENT &&
                mCurrentFragmentId != INTENT_RECOVER_PASSWORD && super.isNeedAuth();
    }
}
