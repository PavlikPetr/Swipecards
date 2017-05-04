package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.settings.payment_ninja.SettingsPaymentsNinjaFragment;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.NotNull;

import static com.topface.topface.ui.settings.FeedbackMessageFragment.FeedbackType;

public class SettingsContainerActivity extends BaseFragmentActivity<AcFragmentFrameBinding> {

    public static final String CONFIRMATION_CODE = "confirmation";

    public static final int INTENT_ACCOUNT = 201;
    public static final int INTENT_ABOUT = 203;
    public static final int INTENT_SEND_FEEDBACK = 204;
    public static final int INTENT_CHANGE_PASSWORD = 205;
    public static final int INTENT_CHANGE_EMAIL = 206;
    public static final int INTENT_NOTIFICATIONS = 207;
    public static final int INTENT_FEEDBACK = 208;
    public static final int INTENT_PURCHASES = 209;

    private String mConfirmCode;

    public static Intent getFeedbackMessageIntent(Context context, FeedbackMessageFragment.FeedbackType feedbackType) {
        Intent intent = new Intent(context, SettingsContainerActivity.class);
        intent.putExtra(FeedbackMessageFragment.INTENT_FEEDBACK_TYPE, feedbackType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //we will create fragment once time only
        if (savedInstanceState == null) {
            Fragment fragment = null;
            Intent intent = getIntent();
            mConfirmCode = getIntent().getStringExtra(CONFIRMATION_CODE);
            switch (intent.getIntExtra(App.INTENT_REQUEST_KEY, 0)) {
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
                case INTENT_PURCHASES:
                    fragment = new SettingsPaymentsNinjaFragment();
                    break;
                default:
                    break;
            }
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();
            }
        }
    }

    public String getConfirmationCode() {
        return mConfirmCode;
    }

    @Override
    public boolean isTrackable() {
        return false;
    }

    @NotNull
    @Override
    public ToolbarViewBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }
}
