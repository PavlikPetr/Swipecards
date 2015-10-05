package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.utils.social.AuthToken;

import static com.topface.topface.ui.settings.FeedbackMessageFragment.FeedbackType;

public class SettingsContainerActivity extends BaseFragmentActivity {

    public static final String CONFIRMATION_CODE = "confirmation";

    public static final int INTENT_ACCOUNT = 201;
    public static final int INTENT_ABOUT = 203;
    public static final int INTENT_SEND_FEEDBACK = 204;
    public static final int INTENT_CHANGE_PASSWORD = 205;
    public static final int INTENT_CHANGE_EMAIL = 206;
    public static final int INTENT_NOTIFICATIONS = 207;
    public static final int INTENT_FEEDBACK = 208;

    private String mConfirmCode;

    public static Intent getFeedbackMessageIntent(Context context, FeedbackMessageFragment.FeedbackType feedbackType) {
        Intent intent = new Intent(context, SettingsContainerActivity.class);
        intent.putExtra(FeedbackMessageFragment.INTENT_FEEDBACK_TYPE, feedbackType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Fragment fragment = null;
        Intent intent = getIntent();
        mConfirmCode = getIntent().getStringExtra(CONFIRMATION_CODE);
        switch (intent.getIntExtra(Static.INTENT_REQUEST_KEY, 0)) {
            case INTENT_ACCOUNT:
                AuthToken token = AuthToken.getInstance();
                if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                    fragment = new SettingsTopfaceAccountFragment();
                } else {
                    fragment = new SettingsAccountFragment();
                }
                break;
            case INTENT_CHANGE_PASSWORD:
                boolean needExit = intent.getBooleanExtra(SettingsTopfaceAccountFragment.NEED_EXIT, false);
                fragment = SettingsChangeAuthDataFragment.newInstance(needExit, true);
                break;
            case INTENT_CHANGE_EMAIL:
                fragment = SettingsChangeAuthDataFragment.newInstance(false, false);
                break;
            case INTENT_FEEDBACK:
                fragment = new SettingsFeedbackFragment();
                break;
            case INTENT_ABOUT:
                fragment = new SettingsAboutFragment();
                break;
            case INTENT_SEND_FEEDBACK:
                fragment = FeedbackMessageFragment.newInstance(
                        (FeedbackType) getIntent().getSerializableExtra(
                                FeedbackMessageFragment.INTENT_FEEDBACK_TYPE
                        )
                );
                break;
            case INTENT_NOTIFICATIONS:
                fragment = new SettingsNotificationsFragment();
                break;
            default:
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.loFrame, fragment).commit();
        }
    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_fragment_frame;
    }

    public String getConfirmationCode() {
        return mConfirmCode;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
