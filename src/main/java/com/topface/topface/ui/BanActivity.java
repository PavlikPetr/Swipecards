package com.topface.topface.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.ui.analytics.TrackedActivity;
import com.topface.topface.utils.AppConfig;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.social.AuthToken;

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
    private Button mButton;
    private int mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ban);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        ImageView image = (ImageView) findViewById(R.id.ivBan);
        TextView titleTextView = (TextView) findViewById(R.id.banned_title);
        TextView messageTextView = (TextView) findViewById(R.id.banned_message);
        mTimerTextView = (TextView) findViewById(R.id.banned_timer);
        mButton = (Button) findViewById(R.id.btnButton);

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
                long floodTime = getIntent().getLongExtra(INTENT_FLOOD_TIME, DEFAULT_FLOOD_WAIT_TIME)*1000l;
                getTimer(getFloodTime(floodTime)).start();
                break;
            case TYPE_RESTORE:
                title = getString(R.string.restore_of_account);
                mButton.setText(R.string.restore);
                mButton.setVisibility(View.VISIBLE);
                mButton.setOnClickListener(this);
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
        App.getConfig().saveConfigField(AppConfig.FLOOD_ENDS_TIME);
    }

    private long getFloodTime() {
        return getFloodTime(DEFAULT_FLOOD_WAIT_TIME);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnButton:
                switch (mType) {
                    case TYPE_RESTORE:
                        new RestoreAccountRequest(AuthToken.getInstance(),this)
                                .callback(new DataApiHandler<Profile>() {
                        @Override
                        protected void success(Profile data, IApiResponse response) {
                            CacheProfile.setProfile(data, (ApiResponse) response, ProfileRequest.P_ALL);
                            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(new Intent(ProfileRequest.PROFILE_UPDATE_ACTION));
                            finish();
                        }

                        @Override
                        protected Profile parseResponse(ApiResponse response) {
                            return Profile.parse(response);
                        }

                        @Override
                        public void fail(int codeError, IApiResponse response) {
                        }
                    })
                                .exec();
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }
}
