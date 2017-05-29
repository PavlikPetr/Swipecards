package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.RestoreAccountActivityBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.statistics.AuthStatistics;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

public class RestoreAccountActivity extends TrackedFragmentActivity<RestoreAccountActivityBinding> {

    public static final int RESTORE_RESULT = 46452;
    public static final String RESTORE_ACCOUNT_SHOWN = "restore_account_shown";
    public static final String TOKEN_DATA = "token_data";
    private AuthToken.TokenInfo mTokenInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mTokenInfo = savedInstanceState.getParcelable(TOKEN_DATA);
        }
        RestoreAccountActivityBinding viewBinding = getViewBinding();
        if (viewBinding != null) {
            viewBinding.setHandlers(new Handlers(this, AuthToken.getInstance().getTokenInfo()));
        }
        /*
        Чистим токен чтоб при смахивании таска, и последующем входе в приложение не было ситуации,
        при которой приложение думает что оно авторизованно, но на самом деле нет=)
         */
        AuthToken.getInstance().removeToken();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TOKEN_DATA, mTokenInfo);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(RESTORE_ACCOUNT_SHOWN));
    }

    @NotNull
    @Override
    public ToolbarViewBinding getToolbarBinding(@NotNull RestoreAccountActivityBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.restore_account_activity;
    }

    @SuppressWarnings("unused")
    public static class Handlers {

        private final Activity mActivity;
        private final AuthToken.TokenInfo mTokenInfo;

        public Handlers(@NotNull Activity activity, AuthToken.TokenInfo tokenDataHolder) {
            mActivity = activity;
            mTokenInfo = tokenDataHolder;
        }

        private void sendFirstAuthUser(String platform, String authStatus) {
            AppConfig appConfig = App.getAppConfig();
            if (appConfig.isFirstAuth()) {
                AuthStatistics.sendFirstAuth(platform, authStatus);
                appConfig.setFirstAuth();
                appConfig.saveConfig();
            }
        }

        public void onRestoreClick(View view) {
            if (mTokenInfo == null) {
                return;
            }
            new RestoreAccountRequest(mTokenInfo, mActivity.getApplicationContext())
                    .callback(new SimpleApiHandler() {
                        @Override
                        public void success(IApiResponse response) {
                            super.success(response);
                            try {
                                String authStatus = response.getJsonResult().getString("authStatus");
                                sendFirstAuthUser(AuthToken.SN_TOPFACE, authStatus);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            AuthToken.getInstance().setTokeInfo(mTokenInfo);
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
