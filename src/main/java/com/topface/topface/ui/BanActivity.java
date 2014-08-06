package com.topface.topface.ui;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RestoreAccountRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.settings.FeedbackMessageFragment;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.topface.topface.ui.settings.FeedbackMessageFragment.FeedbackType;

public class BanActivity extends TrackedFragmentActivity implements View.OnClickListener {

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
    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);
    private boolean mIndeterminateSupported = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setWindowOptions();
        setContentView(R.layout.ban);
        initViews();
    }

    @SuppressWarnings("deprecation")
    private void setWindowOptions() {
        // supportRequestWindowFeature() вызывать только до setContent(),
        // метод setSupportProgressBarIndeterminateVisibility(boolean) вызывать строго после setContent();
        mIndeterminateSupported = supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // для корректного отображения картинок
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
    }

    @Override
    public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
        if (mIndeterminateSupported) {
            if (getSupportActionBar() != null) {
                super.setSupportProgressBarIndeterminateVisibility(visible);
            }
        }
    }

    private void initViews() {
        TextView titleTextView = (TextView) findViewById(R.id.banned_title);
        TextView messageTextView = (TextView) findViewById(R.id.banned_message);
        mTimerTextView = (TextView) findViewById(R.id.banned_timer);
        Button btnCancel = (Button) findViewById(R.id.btnCancel);

        mType = getIntent().getIntExtra(INTENT_TYPE, TYPE_UNKNOWN);
        String title = Static.EMPTY;
        String message = Static.EMPTY;
        switch (mType) {
            case TYPE_BAN:
                initViewsForBan(titleTextView, messageTextView, btnCancel);
                break;
            case TYPE_FLOOD:
                initViewsForFlood(titleTextView, messageTextView, title);
                long floodTime = getIntent().getLongExtra(INTENT_FLOOD_TIME, DEFAULT_FLOOD_WAIT_TIME) * 1000l;
                getTimer(floodTime).start();
                break;
            case TYPE_RESTORE:
                initViewsForRestore(titleTextView, messageTextView, btnCancel, message);
                mLocalTokenInfo = AuthToken.getInstance().getTokenInfo();
                AuthToken.getInstance().removeToken();
                break;
            default:
                break;
        }
    }

    private void initViewsForRestore(TextView titleTextView, TextView messageTextView, Button btnCancel, String message) {
        ImageView image = (ImageView) findViewById(R.id.ivBan);
        Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnConfirm.setText(R.string.restore);
        btnConfirm.setVisibility(View.VISIBLE);
        btnConfirm.setOnClickListener(this);
        btnCancel.setText(android.R.string.cancel);
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(this);
        mTimerTextView.setVisibility(View.GONE);
        image.setVisibility(View.GONE);
        titleTextView.setText(R.string.delete_account_will_be_restored_are_you_sure);
        messageTextView.setText(message);
    }

    private void initViewsForFlood(TextView titleTextView, TextView messageTextView, String title) {
        mTimerTextView.setVisibility(View.VISIBLE);
        titleTextView.setText(title);
        messageTextView.setText(R.string.ban_flood_detected);
    }

    private void initViewsForBan(TextView titleTextView, TextView messageTextView, Button btnCancel) {
        View logoutView = findViewById(R.id.logout_text);
        mTimerTextView.setVisibility(View.GONE);
        btnCancel.setText(R.string.ban_complain);
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(this);
        logoutView.setVisibility(View.VISIBLE);
        logoutView.setOnClickListener(this);
        titleTextView.setText(R.string.ban_title);
        messageTextView.setText(getIntent().getStringExtra(BANNING_TEXT_INTENT));
    }

    @Override
    public void finish() {
        super.finish();
        ConnectionManager.getInstance().onBanActivityFinish();
    }

    private CountDownTimer getTimer(long time) {
        return new CountDownTimer(time, 1000) {
            public void onTick(long millis) {
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                mTimerTextView.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
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
            case TYPE_BAN:
                switch (v.getId()) {
                    case R.id.btnCancel:
                        ConnectionManager.getInstance().onBanActivityFinish();
                        getSupportFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.slide_in_from_right, R.anim.slide_out_right,
                                        R.anim.slide_in_from_right, R.anim.slide_out_right
                                )
                                .add(
                                        R.id.foreground_frame_layout,
                                        FeedbackMessageFragment.newInstance(FeedbackType.BAN)
                                )
                                .addToBackStack(null)
                                .commit();
                        break;
                    case R.id.logout_text:
                        AuthorizationManager.logout(this);
                        break;
                    default:
                        break;
                }
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setSubtitle(null);
        } else if (!mBackPressedOnce.get()) {
            (new Timer()).schedule(new TimerTask() {
                @Override
                public void run() {
                    mBackPressedOnce.set(false);
                }
            }, 3000);
            mBackPressedOnce.set(true);
            Toast.makeText(this, R.string.press_back_more_to_close_app, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(NavigationActivity.INTENT_EXIT, true);
            startActivity(intent);
            super.onBackPressed();
        }
    }
}
