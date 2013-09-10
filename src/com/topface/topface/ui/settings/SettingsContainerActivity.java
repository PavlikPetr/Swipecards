package com.topface.topface.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;

public class SettingsContainerActivity extends BaseFragmentActivity {

    Fragment mFragment;

    public static final String CONFIRMATION_CODE = "confirmation";

    public static final int INTENT_ACCOUNT = 201;
    public static final int INTENT_FEEDBACK = 202;
    public static final int INTENT_ABOUT = 203;
    public static final int INTENT_SEND_FEEDBACK = 204;
    public static final int INTENT_CHANGE_PASSWORD = 205;
    private String mConfirmCode;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.fragment_frame);

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_left);

        Intent intent = getIntent();

        mConfirmCode = getIntent().getStringExtra(CONFIRMATION_CODE);

        switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0)) {
            case INTENT_ACCOUNT:
                AuthToken token = AuthToken.getInstance();
                if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                    mFragment = new SettingsTopfaceAccountFragment();
                } else {
                    mFragment = new SettingsAccountFragment();
                }
                break;
            case INTENT_CHANGE_PASSWORD:
                boolean needExit = intent.getBooleanExtra(SettingsTopfaceAccountFragment.NEED_EXIT, false);
                mFragment = SettingsChangePasswordFragment.newInstance(needExit);
                break;
            case INTENT_FEEDBACK:
                mFragment = new SettingsFeedbackFragment();
                break;
            case INTENT_ABOUT:
                mFragment = new SettingsAboutFragment();
                break;
            case INTENT_SEND_FEEDBACK:
                mFragment = new SettingsFeedbackMessageFragment();
            default:
                break;
        }




        if (mFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.loFrame, mFragment).commit();
        }
    }

    public String getConfirmationCode () {
        return mConfirmCode;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_right);
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
