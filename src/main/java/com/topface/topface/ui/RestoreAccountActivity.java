package com.topface.topface.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.utils.actionbar.ActionBarView;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class RestoreAccountActivity extends TrackedFragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        ActionBarView barView = new ActionBarView(getSupportActionBar(), this);
        barView.setSimpleView();
        getSupportActionBar().show();
        setContentView(R.layout.restore_account_activity);
        initView();
    }

    private void initView() {
        Button restore = (Button) findViewById(R.id.restore_account);
        restore.setOnClickListener(this);
        Button cancel = (Button) findViewById(R.id.cancel_restore);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restore_account:
                final AuthToken.TokenInfo tokenInfo = AuthToken.getInstance().getTokenInfo();
                new RestoreAccountRequest(tokenInfo, this)
                        .callback(new SimpleApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                super.success(response);
                                AuthToken.getInstance().setTokeInfo(tokenInfo);
                                AuthorizationManager.saveAuthInfo(response);
                                finish();
                            }
                        }).exec();
                break;
            case R.id.cancel_restore:
                AuthToken.getInstance().removeToken();
                finish();
                break;
        }
    }

}
