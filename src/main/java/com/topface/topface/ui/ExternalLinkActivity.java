package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.Static;
import com.topface.topface.ui.settings.SettingsContainerActivity;
import com.topface.topface.utils.ExternalLinkExecuter;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;

public class ExternalLinkActivity extends BaseFragmentActivity {

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
                intent.putExtra(Static.INTENT_REQUEST_KEY, SettingsContainerActivity.INTENT_ACCOUNT);
                intent.putExtra(SettingsContainerActivity.CONFIRMATION_CODE, code);
                startActivity(intent);
            }
            getIntent().setData(null);
            finish();
        }

        @Override
        public void onOfferWall() {
            OfferwallsManager.startOfferwall(ExternalLinkActivity.this);
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
        super.onCreate(savedInstanceState);
        new ExternalLinkExecuter(mListener).execute(this, getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ExternalLinkExecuter(mListener).execute(this, getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        new ExternalLinkExecuter(mListener).execute(this, getIntent());
    }
}
