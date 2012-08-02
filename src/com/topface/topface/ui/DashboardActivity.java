package com.topface.topface.ui;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.C2DMUtils;
import com.topface.topface.Data;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.fragments.DatingFragment;

import com.topface.topface.ui.fragments.LikesFragment;
import com.topface.topface.ui.fragments.MutualFragment;
import com.topface.topface.ui.fragments.TopsFragment;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Newbie;
import com.topface.topface.utils.http.Http;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DashboardActivity extends Activity implements View.OnClickListener {
    // Data
    private boolean mNotification;
    private TextView mLikesNotify;
    private TextView mInboxNotify;
    private TextView mSymphatyNotify;
    private View mButtonsGroup;
    private ProgressBar mProgressBar;
    private Button mDatingButton;
    private Button mProfileButton;
    private Button mTopsButton;
    private Button mLikesButton;
    private ImageView mNewbieView;
    private Newbie mNewbie;
    private ProfileRequest profileRequest;
    private SharedPreferences mPreferences;
    private AlphaAnimation mAlphaAnimation;
    // Constants
    public static final String BROADCAST_ACTION = "com.topface.topface.DASHBOARD_NOTIFICATION";
    //---------------------------------------------------------------------------
    // class NotificationReceiver
    //---------------------------------------------------------------------------
    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context,Intent intent) {
            if (intent.getAction().equals(C2DMUtils.C2DM_NOTIFICATION))
                DashboardActivity.this.refreshNotifications();
        }
    };
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_dashboard);
        Debug.log(this, "+onCreate");

        // C2DM
        C2DMUtils.init(this);

        // Version
        if (App.DEBUG)
            try {
                //String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
                PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                ((TextView)findViewById(R.id.tvVersion)).setText("version: " + pinfo.versionName + "b [" + pinfo.versionCode + "]");
            } catch(NameNotFoundException e) {
            }

        // Preferences
        mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Newbie
        mNewbie = new Newbie(mPreferences);
        mNewbieView = (ImageView)findViewById(R.id.ivNewbie);
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(200L);

        // Notifications
        mLikesNotify = (TextView)findViewById(R.id.tvDshbrdNotifyLikes);
        mInboxNotify = (TextView)findViewById(R.id.tvDshbrdNotifyChat);
        mSymphatyNotify = (TextView)findViewById(R.id.tvDshbrdNotifySymphaty);

        // Progress
        mProgressBar = (ProgressBar)findViewById(R.id.prsDashboardLoading);

        // Layouts
        mButtonsGroup = findViewById(R.id.loDshbrdGroup);

        { // Buttons
            mDatingButton = ((Button)findViewById(R.id.btnDshbrdDating));
            mDatingButton.setOnClickListener(this);

            mProfileButton = ((Button)findViewById(R.id.btnDshbrdProfile));
            mProfileButton.setOnClickListener(this);

            mTopsButton = ((Button)findViewById(R.id.btnDshbrdTops));
            mTopsButton.setOnClickListener(this);

            mLikesButton = ((Button)findViewById(R.id.btnDshbrdLikes));
            mLikesButton.setOnClickListener(this);

            ((Button)findViewById(R.id.btnDshbrdSymphaty)).setOnClickListener(this);
            ((Button)findViewById(R.id.btnDshbrdChat)).setOnClickListener(this);
        }

        if (!Http.isOnline(this))
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT).show();
    }
    //---------------------------------------------------------------------------  
    @Override
    protected void onStart() {
        super.onStart();
        System.gc();

        if (!Http.isOnline(this))
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT).show();

        registerReceiver(mNotificationReceiver, new IntentFilter(C2DMUtils.C2DM_NOTIFICATION));
        Data.facebook.extendAccessTokenIfNeeded(this, null);

        if (!Data.isSSID()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            Data.ownerAvatar = null;
            Data.userAvatar = null;
            finish();
            return;
        }

        updateProfile();
    }
    //---------------------------------------------------------------------------  
    @Override
    protected void onStop() {
        mNewbieView.setVisibility(View.INVISIBLE);
        unregisterReceiver(mNotificationReceiver);
        super.onStop();
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {
        if (profileRequest != null)
            profileRequest.cancel();

        System.gc();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------
    public void showNewbie() {
        if (mNewbie.isDashboardCompleted())
            return;

        mNewbieView.setVisibility(View.INVISIBLE);
        SharedPreferences.Editor editor = mPreferences.edit();

        if (mNewbie.profile != true) {
            mNewbie.profile = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_PROFILE, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_profile);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (mNewbie.dating != true) {
            mNewbie.dating = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_DATING, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_dating);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (mNewbie.likes != true && CacheProfile.unread_likes > 0) {
            mNewbie.likes = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_LIKES, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_likes);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (mNewbie.tops != true) {
            mNewbie.tops = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_TOPS, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_tops);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);
        }

        editor.commit();
    }
    //---------------------------------------------------------------------------
    private void updateProfile() {
        profileRequest = new ProfileRequest(getApplicationContext());
        if (!mNotification)
            profileRequest.part = ProfileRequest.P_DASHBOARD;
        else
            profileRequest.part = ProfileRequest.P_NOTIFICATION;
        profileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                if (!mNotification) {
                    CacheProfile.setData(Profile.parse(response));
                    Http.avatarOwnerPreloading();
                } else
                    CacheProfile.updateNotifications(Profile.parse(response));
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mNotification) {
                            mProgressBar.setVisibility(View.GONE);
                            mNotification = true;
                        }
                        mButtonsGroup.setVisibility(View.VISIBLE);
                        refreshNotifications();
                        showNewbie();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mNotification)
                            Toast.makeText(DashboardActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mButtonsGroup.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        mLikesNotify.setVisibility(View.INVISIBLE);
                        mInboxNotify.setVisibility(View.INVISIBLE);
                        mSymphatyNotify.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void refreshNotifications() {
        // clear notification
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(C2DMUtils.C2DM_NOTIFICATION_ID);

        if (CacheProfile.unread_likes > 0) {
            mLikesNotify.setText(" " + CacheProfile.unread_likes + " ");
            mLikesNotify.setVisibility(View.VISIBLE);
        } else
            mLikesNotify.setVisibility(View.INVISIBLE);

        if (CacheProfile.unread_messages > 0) {
            mInboxNotify.setText(" " + CacheProfile.unread_messages + " ");
            mInboxNotify.setVisibility(View.VISIBLE);
        } else
            mInboxNotify.setVisibility(View.INVISIBLE);

        if (CacheProfile.unread_mutual > 0) {
            mSymphatyNotify.setText(" " + CacheProfile.unread_mutual + " ");
            mSymphatyNotify.setVisibility(View.VISIBLE);
        } else
            mSymphatyNotify.setVisibility(View.INVISIBLE);
    }
    //---------------------------------------------------------------------------
    @Override
    public void onClick(View view) {
        if (!Http.isOnline(this)) {
            Toast.makeText(this, getString(R.string.general_internet_off), Toast.LENGTH_SHORT).show();
            return;
        }

        if (CacheProfile.city_id == 0) {
            mNotification = false;
            mProgressBar.setVisibility(View.VISIBLE);
            mButtonsGroup.setVisibility(View.GONE);
            updateProfile();
            return;
        }
        Intent intent = null;
        switch (view.getId()) {
            case R.id.btnDshbrdDating: {
                intent = new Intent(this.getApplicationContext(), DatingFragment.class);
            }
                break;
            case R.id.btnDshbrdLikes: {
                intent = new Intent(this.getApplicationContext(), LikesFragment.class);
            }
                break;
            case R.id.btnDshbrdSymphaty: {
                intent = new Intent(this.getApplicationContext(), MutualFragment.class);
            }
                break;
            case R.id.btnDshbrdChat: {
                //intent = new Intent(this.getApplicationContext(), InboxActivity.class);
            }
                break;
            case R.id.btnDshbrdTops: {
                intent = new Intent(this.getApplicationContext(), TopsFragment.class);
            }
                break;
            case R.id.btnDshbrdProfile: {
                intent = new Intent(Intent.ACTION_VIEW);
                //intent.setData(Uri.parse("market://details?id=com.topface.topface"));
                //intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.topface.topface"));
                intent = new Intent(this.getApplicationContext(), ProfileActivity.class);
            }
                break;
            default:
        }
        startActivity(intent);
    }
    //---------------------------------------------------------------------------
}

/*public void showNewbie() {
 * if(mNewbieState == NEWBIE_COMPLETED)
 * return;
 * 
 * int[] coords = {0,0};
 * int x = mNewbieView.getMeasuredWidth();
 * int y = mNewbieView.getMeasuredHeight();
 * RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(x,y);
 * switch(mNewbieState) {
 * case NEWBIE_PROFILE: {
 * mProfileButton.getLocationOnScreen(coords);
 * int h = mProfileButton.getMeasuredHeight();
 * int z = coords[1]+h;
 * params.setMargins(0, z-y, 0, 0);
 * mNewbieView.setLayoutParams(params);
 * mNewbieView.setVisibility(View.VISIBLE);
 * } break;
 * case NEWBIE_DATING: {
 * mDatingButton.getLocationInWindow(coords);
 * params.setMargins(0, coords[1]-20, 0, 0);
 * mNewbieView.setLayoutParams(params);
 * mNewbieView.setVisibility(View.VISIBLE);
 * } break;
 * case NEWBIE_LIKES: {
 * mLikesButton.getLocationInWindow(coords);
 * params.setMargins(0, coords[1], 0, 0);
 * mNewbieView.setLayoutParams(params);
 * mNewbieView.setVisibility(View.VISIBLE);
 * } break;
 * case NEWBIE_TOPS: {
 * mTopsButton.getLocationOnScreen(coords);
 * params.setMargins(0, coords[1], 0, 0);
 * mNewbieView.setLayoutParams(params);
 * mNewbieView.setVisibility(View.VISIBLE);
 * } break;
 * default: {
 * mNewbieView.setVisibility(View.INVISIBLE);
 * }
 * }
 * 
 * Toast.makeText(this,""+mNewbieState+","+x+","+y+","+coords[1],Toast.LENGTH_SHORT
 * ).show();
 * 
 * // условие для записи
 * mNewbieState++;
 * SharedPreferences.Editor editor = mPreferences.edit();
 * editor.putInt(Static.PREFERENCES_NEWBIE_DASHBOARD, mNewbieState);
 * editor.commit();
 * } */
