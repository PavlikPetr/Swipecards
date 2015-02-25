package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.analytics.TrackedFragmentActivity;
import com.topface.topface.ui.fragments.BanFragment;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.actionbar.ActionBarView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class BanActivity extends TrackedFragmentActivity {

    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_BAN = 1;
    public static final int TYPE_FLOOD = 2;
    public static final int TYPE_RESTORE = 3;

    public static final String INTENT_TYPE = "message_type";
    public static final String BANNING_TEXT_INTENT = "banning_intent";
    public static final String INTENT_FLOOD_TIME = "flood_time";

    private static final long DEFAULT_FLOOD_WAIT_TIME = 60L;
    public static final String FLOOD = "flood";

    private AtomicBoolean mBackPressedOnce = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarView actionBarView = new ActionBarView(getSupportActionBar(), this);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        actionBarView.setArrowUpView();
        getSupportActionBar().hide();
        setContentView(R.layout.ban_activity);
        BaseFragment fragment = getFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.ban_content, fragment, null).commit();

    }

    private BaseFragment getFragment() {
        BaseFragment fragment;
        int type = getIntent().getIntExtra(INTENT_TYPE, TYPE_UNKNOWN);
        long banExpire;
        String userMessage;
        if (type == TYPE_BAN) {
            banExpire = getIntent().getLongExtra(BanFragment.BAN_EXPIRE, TYPE_UNKNOWN);
            userMessage = getIntent().getStringExtra(BanFragment.USER_MESSAGE);
        } else {
            banExpire = getIntent().getLongExtra(INTENT_FLOOD_TIME, DEFAULT_FLOOD_WAIT_TIME) * 1000l;
            userMessage = App.getContext().getResources().getString(R.string.ban_flood_detected);
        }

        Bundle arg = new Bundle();
        switch (type) {
            case TYPE_FLOOD:
                arg.putBoolean(FLOOD, true);
            case TYPE_BAN:
                fragment = new BanFragment();
                arg.putString(BanFragment.USER_MESSAGE, userMessage);
                arg.putLong(BanFragment.BAN_EXPIRE, banExpire);
                fragment.setArguments(arg);
                return fragment;
            case TYPE_RESTORE:
                //todo
                break;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
                actionBar.setTitle(R.string.app_name);
                actionBar.setSubtitle(null);
            }
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
