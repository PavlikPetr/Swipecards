package com.topface.topface.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedActivity;

public class BanActivity extends TrackedActivity {

    private SharedPreferences mPreferences;
    public static final String FLOOD_ENDS_TIME = "flood_ens_time";

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_BAN = 1;
    public static final int TYPE_FLOOD = 2;

    public static final String INTENT_TYPE = "message_type";
    public static final String BANNING_TEXT_INTENT = "banning_intent";

    private static final long FLOOD_WAIT_TIME = 180000L;

    private TextView mTimerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ban);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext());

        TextView titleContainer = (TextView) findViewById(R.id.banned_title);
        TextView messageContainer = (TextView) findViewById(R.id.banned_message);
        mTimerContainer = (TextView) findViewById(R.id.banned_timer);

        int type = getIntent().getIntExtra(INTENT_TYPE,TYPE_UNKNOWN);

        String title = "";
        String message = "";
        switch(type) {
            case TYPE_BAN:
                title = getString(R.string.ban_title);
                message = getIntent().getStringExtra(BANNING_TEXT_INTENT);
                break;
            case TYPE_FLOOD:
                message = getString(R.string.ban_flood_detected);
                mTimerContainer.setVisibility(View.VISIBLE);
                getTimer(getFloodTime()).start();
                break;
            default:
                break;
        }

        titleContainer.setText(title);
        messageContainer.setText(message);
    }

    private long getFloodTime() {
        long result;
        long endTime = mPreferences.getLong(FLOOD_ENDS_TIME,0L);
        long now = System.currentTimeMillis();

        if (endTime < now) {
            endTime = now + FLOOD_WAIT_TIME;
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putLong(FLOOD_ENDS_TIME,endTime);
            editor.commit();
            result = FLOOD_WAIT_TIME;
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
                seconds     = seconds % 60;

                mTimerContainer.setText(String.format("%d:%02d", minutes, seconds));
            }

            public void onFinish() {
                mTimerContainer.setText("0:00");
                finish();
            }
        };
        return timer;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
