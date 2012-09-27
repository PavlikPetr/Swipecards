package com.topface.topface.ui;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.*;
import com.topface.topface.data.Options;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.*;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Newbie;
import com.topface.topface.utils.Utils;

public class DashboardActivity extends MenuActivity implements View.OnClickListener {
    // Data
    private boolean mNotification;
    private TextView mLikesNotify;
    private TextView mInboxNotify;
    private TextView mSymphatyNotify;
    private View mButtonsGroup;
    private ProgressBar mProgressBar;
    private ImageView mNewbieView;
    private Newbie mNewbie;
    private ProfileRequest profileRequest;
    private SharedPreferences mPreferences;
    private AlphaAnimation mAlphaAnimation;
    private boolean mIsReceiverRegistered;
    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(C2DMUtils.C2DM_NOTIFICATION)) {
                updateProfile();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_dashboard);
        Debug.log(this, "+onCreate");

        // Version
        if (App.DEBUG)
            try {
                //String app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
                PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                ((TextView) findViewById(R.id.tvVersion)).setText("version: " + pinfo.versionName + "b [" + pinfo.versionCode + "]");
            } catch (NameNotFoundException e) {
                Debug.error(e);
            }

        // Preferences
        mPreferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Newbie
        mNewbie = new Newbie(mPreferences);
        mNewbieView = (ImageView) findViewById(R.id.ivNewbie);
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(200L);

        // Notifications
        mLikesNotify = (TextView) findViewById(R.id.tvDshbrdNotifyLikes);
        mInboxNotify = (TextView) findViewById(R.id.tvDshbrdNotifyChat);
        mSymphatyNotify = (TextView) findViewById(R.id.tvDshbrdNotifySymphaty);

        // Progress
        mProgressBar = (ProgressBar) findViewById(R.id.prsDashboardLoading);

        // Layouts
        mButtonsGroup = findViewById(R.id.loDshbrdGroup);

        { // Buttons
            Button datingButton = ((Button) findViewById(R.id.btnDshbrdDating));
            datingButton.setOnClickListener(this);

            Button profileButton = ((Button) findViewById(R.id.btnDshbrdProfile));
            profileButton.setOnClickListener(this);

            Button topsButton = ((Button) findViewById(R.id.btnDshbrdTops));
            topsButton.setOnClickListener(this);

            Button likesButton = ((Button) findViewById(R.id.btnDshbrdLikes));
            likesButton.setOnClickListener(this);

            findViewById(R.id.btnDshbrdSymphaty).setOnClickListener(this);
            findViewById(R.id.btnDshbrdChat).setOnClickListener(this);
        }

        if (App.isOnline()) {
            ratingPopup();
        }
        else {
            App.showConnectionError();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsReceiverRegistered) {
            registerReceiver(mNotificationReceiver, new IntentFilter(C2DMUtils.C2DM_NOTIFICATION));
            mIsReceiverRegistered = true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.gc();

        if (!App.isOnline())
            App.showConnectionError();

        Data.facebook.extendAccessTokenIfNeeded(this, null);

        if (!Data.isSSID()) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            return;
        }

        updateProfile();
        if (!CacheProfile.isOptionsLoaded()) {
            updateOptions();
        }
    }

    private void updateOptions() {
        new OptionsRequest(getApplicationContext()).callback(new BaseApiHandler() {
            @Override
            public void success(ApiResponse response) throws NullPointerException {
                Options options = Options.parse(response);
                CacheProfile.setOptions(options, response.mJSONResult);
            }
        }).exec();
    }


    @Override
    protected void onPause() {
        mNewbieView.setVisibility(View.INVISIBLE);
        if (mIsReceiverRegistered) {
            unregisterReceiver(mNotificationReceiver);
            mIsReceiverRegistered = false;
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (profileRequest != null)
            profileRequest.cancel();

        System.gc();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }

    public void showNewbie() {
        if (mNewbie.isDashboardCompleted())
            return;

        mNewbieView.setVisibility(View.INVISIBLE);
        SharedPreferences.Editor editor = mPreferences.edit();

        if (!mNewbie.profile) {
            mNewbie.profile = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_PROFILE, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_profile);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (!mNewbie.dating) {
            mNewbie.dating = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_DATING, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_dating);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (!mNewbie.likes && CacheProfile.unread_likes > 0) {
            mNewbie.likes = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_LIKES, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_likes);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (!mNewbie.tops) {
            mNewbie.tops = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DASHBOARD_TOPS, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_tops);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);
        }

        editor.commit();
    }

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
                    CacheProfile.setData(Profile.parse(response), response);
                    Debug.log("Avatar downloaded!");
                } else {
                    CacheProfile.updateNotifications(Profile.parse(response));
                }

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
            public void fail(int codeError, ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mNotification)
                            Utils.showErrorMessage(DashboardActivity.this);
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

    private void refreshNotifications() {
        // clear notification
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(C2DMUtils.C2DM_NOTIFICATION_ID);

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

        if (CacheProfile.unread_symphaties > 0) {
            mSymphatyNotify.setText(" " + CacheProfile.unread_symphaties + " ");
            mSymphatyNotify.setVisibility(View.VISIBLE);
        } else
            mSymphatyNotify.setVisibility(View.INVISIBLE);
    }

    private void ratingPopup() {
        // Rating popup

        long date_start = mPreferences.getLong(Static.PREFERENCES_RATING, 1);

        if (date_start == 0)
            return;
        else if (date_start == 1) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putLong(Static.PREFERENCES_RATING, new java.util.Date().getTime());
            editor.commit();
            return;
        }

        long date_now = new java.util.Date().getTime();
        if (date_now - date_start < 1000 * 60 * 60 * 24 * 3)
            return;

        final Dialog ratingPopup = new Dialog(this) {
            @Override
            public void onBackPressed() {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(Static.PREFERENCES_RATING, new java.util.Date().getTime());
                editor.commit();
                super.onBackPressed();
            }
        };
        ratingPopup.setTitle(R.string.dashbrd_popup_title);
        ratingPopup.setContentView(R.layout.popup_rating);
        ratingPopup.show();

        ratingPopup.findViewById(R.id.btnRatingPopupRate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.topface.topface")));
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(Static.PREFERENCES_RATING, 0);
                editor.commit();
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupLate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(Static.PREFERENCES_RATING, new java.util.Date().getTime());
                editor.commit();
                ratingPopup.cancel();
            }
        });
        ratingPopup.findViewById(R.id.btnRatingPopupCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putLong(Static.PREFERENCES_RATING, 0);
                editor.commit();
                ratingPopup.cancel();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (!App.isOnline()) {
            App.showConnectionError();
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
                intent = new Intent(this.getApplicationContext(), DatingActivity.class);
            }
            break;
            /*case R.id.btnDshbrdInvite: {
                intent = new Intent(this.getApplicationContext(), InviteActivity.class);
            }
            break;*/
            case R.id.btnDshbrdLikes: {
                intent = new Intent(this.getApplicationContext(), LikesActivity.class);
            }
            break;
            case R.id.btnDshbrdSymphaty: {
                intent = new Intent(this.getApplicationContext(), SymphatyActivity.class);
            }
            break;
            case R.id.btnDshbrdChat: {
                intent = new Intent(this.getApplicationContext(), InboxActivity.class);
            }
            break;
            case R.id.btnDshbrdTops: {
                intent = new Intent(this.getApplicationContext(), VisitorsActivity.class);
            }
            break;
            case R.id.btnDshbrdProfile: {
                //intent = new Intent(Intent.ACTION_VIEW);
                //intent.setData(Uri.parse("market://details?id=com.topface.topface"));
                //intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.topface.topface"));
                intent = new Intent(this.getApplicationContext(), ProfileActivity.class);
            }
            break;
            default:
        }
        startActivity(intent);
    }

}


/*
  public void showNewbie() {
    if(mNewbieState == NEWBIE_COMPLETED)
      return;

    int[] coords = {0,0};
    int x = mNewbieView.getMeasuredWidth();
    int y = mNewbieView.getMeasuredHeight();
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(x,y);
    switch(mNewbieState) {
      case NEWBIE_PROFILE: {
        mProfileButton.getLocationOnScreen(coords);
        int h = mProfileButton.getMeasuredHeight();
        int z = coords[1]+h;
        params.setMargins(0, z-y, 0, 0);
        mNewbieView.setLayoutParams(params);
        mNewbieView.setVisibility(View.VISIBLE);
      } break;
      case NEWBIE_DATING: {
        mDatingButton.getLocationInWindow(coords);
        params.setMargins(0, coords[1]-20, 0, 0);
        mNewbieView.setLayoutParams(params);
        mNewbieView.setVisibility(View.VISIBLE);
      } break;
      case NEWBIE_LIKES: {
        mLikesButton.getLocationInWindow(coords);
        params.setMargins(0, coords[1], 0, 0);
        mNewbieView.setLayoutParams(params);
        mNewbieView.setVisibility(View.VISIBLE);
      } break;
      case NEWBIE_TOPS: {
        mTopsButton.getLocationOnScreen(coords);
        params.setMargins(0, coords[1], 0, 0);
        mNewbieView.setLayoutParams(params);
        mNewbieView.setVisibility(View.VISIBLE);
      } break;
      default: {
        mNewbieView.setVisibility(View.INVISIBLE);
      }
    }

    Toast.makeText(this,""+mNewbieState+","+x+","+y+","+coords[1],Toast.LENGTH_SHORT).show();

    // условие для записи
    mNewbieState++;
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putInt(Static.PREFERENCES_NEWBIE_DASHBOARD, mNewbieState);
    editor.commit();
  }
*/
