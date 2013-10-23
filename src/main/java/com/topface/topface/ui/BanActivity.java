package com.topface.topface.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Auth;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.analytics.TrackedActivity;
import com.topface.topface.utils.AppConfig;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

public class BanActivity extends TrackedActivity implements View.OnClickListener {
    private SharedPreferences mPreferences;

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_BAN = 1;
    public static final int TYPE_FLOOD = 2;
    public static final int TYPE_RESTORE = 3;

    public static final String INTENT_TYPE = "message_type";
    public static final String BANNING_TEXT_INTENT = "banning_intent";
    public static final String INTENT_FLOOD_TIME = "flood_time";

    private static final long DEFAULT_FLOOD_WAIT_TIME = 180L;

    private TextView mTimerTextView;
    private Button mBtnConfirm;
    private Button mBtnCancel;
    private int mType;
    private boolean mRestored;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ban);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        ImageView image = (ImageView) findViewById(R.id.ivBan);
        TextView titleTextView = (TextView) findViewById(R.id.banned_title);
        TextView messageTextView = (TextView) findViewById(R.id.banned_message);
        mTimerTextView = (TextView) findViewById(R.id.banned_timer);
        mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
        mBtnCancel = (Button) findViewById(R.id.btnCancel);

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
                getTimer(getFloodTime(floodTime)).start();
                break;
            case TYPE_RESTORE:
                title = getString(R.string.delete_account_will_be_restored_are_you_sure);
                mBtnConfirm.setText(R.string.restore);
                mBtnConfirm.setVisibility(View.VISIBLE);
                mBtnConfirm.setOnClickListener(this);
                mBtnCancel.setText(android.R.string.cancel);
                mBtnCancel.setVisibility(View.VISIBLE);
                mBtnCancel.setOnClickListener(this);
                mTimerTextView.setVisibility(View.GONE);
                image.setVisibility(View.GONE);
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
        switch (mType) {
            case TYPE_BAN:
                break;
            case TYPE_FLOOD:
                App.getConfig().saveConfigField(AppConfig.FLOOD_ENDS_TIME);
                break;
            case TYPE_RESTORE:
                if (!mRestored) {
                    AuthToken authToken = AuthToken.getInstance();
                    if (!authToken.isEmpty()) {
                        authToken.removeToken();
                    }
                }
                break;
            default:
                break;
        }
    }

    private long getFloodTime(long floodTime) {
        AppConfig config = App.getConfig();
        long result;
        long endTime = config.getFloodEndsTime();
        long now = System.currentTimeMillis();

        if (endTime < now) {
            endTime = now + floodTime;
            config.setFloodEndsTime(endTime);
            result = floodTime;
        } else {
            result = endTime - now;
        }

        return result;
    }

    private CountDownTimer getTimer(long time) {
        CountDownTimer timer = new CountDownTimer(time, 1000) {

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
        return timer;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRestored = false;
    }

    @Override
    public void onClick(View v) {
        switch (mType) {
            case TYPE_RESTORE:
                switch (v.getId()) {
                    case R.id.btnConfirm:
                        new RestoreAccountRequest(AuthToken.getInstance(), this)
                                .callback(new SimpleApiHandler() {
                                    @Override
                                    public void success(IApiResponse response) {
                                        super.success(response);
                                        AuthorizationManager.saveAuthInfo(response);
                                        mRestored = true;
                                        finish();
                                    }
                                }).exec();
                        break;
                    case R.id.btnCancel:
                        mRestored = false;
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
