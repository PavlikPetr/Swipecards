package com.topface.topface.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.analytics.TrackedActivity;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class BanActivity extends TrackedActivity implements View.OnClickListener {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_BAN = 1;
    public static final int TYPE_FLOOD = 2;
    public static final int TYPE_RESTORE = 3;

    public static final String INTENT_TYPE = "message_type";
    public static final String BANNING_TEXT_INTENT = "banning_intent";
    public static final String INTENT_FLOOD_TIME = "flood_time";

    private static final long DEFAULT_FLOOD_WAIT_TIME = 60L;

    private TextView mTimerTextView;
    private int mType;

    // variables for Restore process
    private AuthToken.TokenInfo mLocalTokenInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ban);
        ImageView image = (ImageView) findViewById(R.id.ivBan);
        TextView titleTextView = (TextView) findViewById(R.id.banned_title);
        TextView messageTextView = (TextView) findViewById(R.id.banned_message);
        mTimerTextView = (TextView) findViewById(R.id.banned_timer);
        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);

        mType = getIntent().getIntExtra(INTENT_TYPE, TYPE_UNKNOWN);

        String title = Static.EMPTY;
        String message = Static.EMPTY;
        switch (mType) {
            case TYPE_BAN:
                title = getString(R.string.ban_title);
                message = getIntent().getStringExtra(BANNING_TEXT_INTENT);
                mTimerTextView.setVisibility(View.GONE);
                break;
            case TYPE_FLOOD:
                message = getString(R.string.ban_flood_detected);
                mTimerTextView.setVisibility(View.VISIBLE);
                long floodTime = getIntent().getLongExtra(INTENT_FLOOD_TIME, DEFAULT_FLOOD_WAIT_TIME) * 1000l;
                getTimer(floodTime).start();
                break;
            case TYPE_RESTORE:
                title = getString(R.string.delete_account_will_be_restored_are_you_sure);
                btnConfirm.setText(R.string.restore);
                btnConfirm.setVisibility(View.VISIBLE);
                btnConfirm.setOnClickListener(this);
                btnCancel.setText(android.R.string.cancel);
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(this);
                mTimerTextView.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
                mLocalTokenInfo = AuthToken.getInstance().getTokenInfo();
                AuthToken.getInstance().removeToken();
                break;
            default:
                break;
        }

        titleTextView.setText(title);
        messageTextView.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private CountDownTimer getTimer(long time) {
        return new CountDownTimer(time, 1000) {
            public void onTick(long millis) {
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                mTimerTextView.setText(String.format("%d:%02d", minutes, seconds));
            }

            public void onFinish() {
                mTimerTextView.setText("0:00");
                finish();
            }
        };
    }

    @Override
    public void onClick(View v) {
        switch (mType) {
            case TYPE_RESTORE:
                switch (v.getId()) {
                    case R.id.btnConfirm:
                        new RestoreAccountRequest(mLocalTokenInfo, this)
                                .callback(new SimpleApiHandler() {
                                    @Override
                                    public void success(IApiResponse response) {
                                        super.success(response);
                                        AuthToken.getInstance().setTokeInfo(mLocalTokenInfo);
                                        AuthorizationManager.saveAuthInfo(response);

                                        finish();
                                    }
                                }).exec();
                        break;
                    case R.id.btnCancel:
                        finish();
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
