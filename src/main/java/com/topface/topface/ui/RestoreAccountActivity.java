package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.RestoreAccountActivityBinding;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.utils.actionbar.ActionBarView;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import org.jetbrains.annotations.NotNull;

public class RestoreAccountActivity extends TrackedFragmentActivity {

    public static final int RESTORE_RESULT = 46452;
    public static final String RESTORE_ACCOUNT_SHOWN = "restore_account_shown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RestoreAccountActivityBinding) DataBindingUtil.setContentView(this, R.layout.restore_account_activity))
                .setHandlers(new Handlers(this));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            new ActionBarView(actionBar, this).setSimpleView();
            actionBar.show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RESTORE_ACCOUNT_SHOWN));
    }

    @SuppressWarnings("unused")
    public static class Handlers {

        private final Activity mActivity;

        public Handlers(@NotNull Activity activity) {
            mActivity = activity;
        }

        public void onRestoreClick(View view) {
            final AuthToken.TokenInfo tokenInfo = AuthToken.getInstance().getTokenInfo();
            new RestoreAccountRequest(tokenInfo, mActivity.getApplicationContext())
                    .callback(new SimpleApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            AuthToken.getInstance().setTokeInfo(tokenInfo);
                            AuthorizationManager.saveAuthInfo(response);
                            mActivity.setResult(RESULT_OK);
                            mActivity.finish();
                        }
                    }).exec();
        }

        public void onCancelClick(View view) {
            LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(RESTORE_ACCOUNT_SHOWN));
            new AuthorizationManager().logout(mActivity);
        }

    }

}
