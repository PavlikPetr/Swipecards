package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.TestRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;

public class SslErrorActivity extends Activity {
    private final static int ACTION_DATE_SETTINGS_INTENT_ID = 111;
    TextView mText;
    ProgressBar mProgressBar;
    ApiRequest mApiRequest;
    Button mSettingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ssl_error_dialog);
        mSettingsButton = (Button) findViewById(R.id.btnOpenDateSettings);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), ACTION_DATE_SETTINGS_INTENT_ID);
            }
        });
        mText = (TextView) findViewById(R.id.tvText);
        mProgressBar = (ProgressBar) findViewById(R.id.prsSendingRequest);
        setInProgressState(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_DATE_SETTINGS_INTENT_ID) {
            setInProgressState(true);
            sendRequest();
        }
    }

    private void setInProgressState(boolean state) {
        mText.setVisibility(state ? View.INVISIBLE : View.VISIBLE);
        mProgressBar.setVisibility(state ? View.VISIBLE : View.INVISIBLE);
        mSettingsButton.setEnabled(!state);
    }

    @Override
    public void finish() {
        removeRequest();
        super.finish();
    }

    private void removeRequest() {
        if (mApiRequest != null) {
            if (!mApiRequest.isCanceled()) {
                mApiRequest.cancel();
            }
            mApiRequest = null;
        }
    }

    private void sendRequest() {
        removeRequest();
        mApiRequest = new TestRequest(this).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                finish();
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (codeError == ErrorCodes.NOT_VALID_CERTIFICATE) {
                    setInProgressState(false);
                } else {
                    finish();
                }
            }
        });
        mApiRequest.exec();
    }
}
