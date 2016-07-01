package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.App;
import com.topface.topface.ui.bonus.view.BonusActivity;
import com.topface.topface.ui.settings.SettingsChangeAuthDataFragment;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.ExternalLinkExecuter;
import com.topface.topface.utils.social.AuthToken;

public class ExternalLinkActivity extends BaseFragmentActivity {

    private boolean mIsNeedRestorePwd;

    ExternalLinkExecuter.OnExternalLinkListener mListener = new ExternalLinkExecuter.OnExternalLinkListener() {
        @Override
        public void onProfileLink(int profileID) {
            startActivity(UserProfileActivity.createIntent(null, null, profileID, null, true, true, null, null));
            getIntent().setData(null);
            finish();
        }

        @Override
        public void onConfirmLink(String code) {
            AuthToken token = AuthToken.getInstance();
            if (!token.isEmpty() && token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                Intent intent = new Intent(ExternalLinkActivity.this, SettingsContainerActivity.class);
                intent.putExtra(App.INTENT_REQUEST_KEY, SettingsContainerActivity.INTENT_ACCOUNT);
                intent.putExtra(SettingsContainerActivity.CONFIRMATION_CODE, code);
                startActivity(intent);
                getIntent().setData(null);
                finish();
            }
            getIntent().setData(null);
            finish();
        }

        @Override
        public void onRestorePassword(String code) {
            mIsNeedRestorePwd = true;
            SettingsChangeAuthDataFragment fragment = (SettingsChangeAuthDataFragment) getSupportFragmentManager()
                    .findFragmentByTag(SettingsChangeAuthDataFragment.class.getSimpleName());
            if (fragment == null) {
                fragment = SettingsChangeAuthDataFragment.newInstance(true, true, code);
            }
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment
                    , SettingsChangeAuthDataFragment.class.getSimpleName()).commit();
        }

        @Override
        public void onOfferWall() {
            startActivity(BonusActivity.createIntent());
            getIntent().setData(null);
            finish();
        }

        @Override
        public void onNothingToShow() {
            getIntent().setData(null);
            finish();
            startActivity(new Intent(ExternalLinkActivity.this, NavigationActivity.class));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setHasContent(false);
        super.onCreate(savedInstanceState);
        new ExternalLinkExecuter(mListener).execute(this, getIntent());
    }

    @Override
    protected int getContentLayout() {
        return -1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!AuthToken.getInstance().isEmpty()) {
            new ExternalLinkExecuter(mListener).execute(this, getIntent());
        }
    }

    @Override
    public boolean startAuth() {
        return !mIsNeedRestorePwd && super.startAuth();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (!AuthToken.getInstance().isEmpty()) {
            new ExternalLinkExecuter(mListener).execute(this, getIntent());
        }
    }

    @Override
    public boolean isTrackable() {
        return false;
    }
}
