package com.topface.topface.ui;

import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.frames.DatingActivity;
import com.topface.topface.ui.frames.DialogActivity;
import com.topface.topface.ui.frames.FrameActivity;
import com.topface.topface.ui.frames.LikesActivity;
import com.topface.topface.ui.frames.SettingsActivity;
import com.topface.topface.ui.frames.SympathyActivity;
import com.topface.topface.ui.frames.TopsActivity;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Http;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

public class TopfaceActivity extends ActivityGroup {
    // Data
    private View mMenuGroup;
    private TabHost mTabHost;
    private TranslateAnimation mStartAnimation;
    private TranslateAnimation mBackAnimation;
    private TranslateAnimation mHideFrameAnimation;
    private TranslateAnimation mBackFrameAnimation;
    private TextView f; // notify
    private String mNextFrameName;
    private Button[] mFrameButtons;
    private boolean isMenuOpened;
    // Constants
    private static final String FRAME_PROFILE = "profile";
    private static final String FRAME_DATING = "dating";
    private static final String FRAME_LIKES = "likes";
    private static final String FRAME_SYMPATHY = "mutual";
    private static final String FRAME_TOPS = "tops";
    private static final String FRAME_CHAT = "messages";
    private static final String FRAME_SETTINGS = "settings";
    View loFrameWindow;
    //---------------------------------------------------------------------------
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_topface);

        // Shadow
        final View ivFrameShadow = findViewById(R.id.ivTFShadow);
        loFrameWindow = findViewById(R.id.loTFWindow);

        mNextFrameName = "";

        // Animation
        mStartAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.7f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        mStartAnimation.setDuration(200L);
        mStartAnimation.setFillEnabled(true);
        mStartAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Debug.log(this, "+mStartAnimation");
                ivFrameShadow.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                loFrameWindow.layout(400, 0, 400 + mTabHost.getMeasuredWidth(), mTabHost.getMeasuredHeight()); // !!!!!!!!!!!!!!!!!!!!!!!
            }
        });

        // Back
        mBackAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.7f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        mBackAnimation.setDuration(200L);
        mBackAnimation.setFillEnabled(true);
        mBackAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Debug.log(this, "+mBackAnimation");
                ivFrameShadow.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mMenuGroup.setVisibility(View.INVISIBLE);
                loFrameWindow.layout(0, 0, mTabHost.getMeasuredWidth(), mTabHost.getMeasuredHeight());
            }
        });

        // Hide
        mHideFrameAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.7f, Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        mHideFrameAnimation.setDuration(70L);
        mHideFrameAnimation.setFillEnabled(true);
        mHideFrameAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Debug.log(this, "+mHideFrameAnimation");
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                ivFrameShadow.setVisibility(View.GONE);
                mTabHost.setCurrentTabByTag(mNextFrameName);
                FrameActivity frame = (FrameActivity)getCurrentActivity();
                if (frame instanceof FrameActivity)
                    frame.clearLayout(); // CLEAR ACTIVITY
                loFrameWindow.startAnimation(mBackFrameAnimation);
            }
        });

        // Back Frame
        mBackFrameAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        mBackFrameAnimation.setDuration(180L);
        mBackFrameAnimation.setFillEnabled(true);
        mBackFrameAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Debug.log(this, "+mBackFrameAnimation");
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                loFrameWindow.layout(0, 0, mTabHost.getMeasuredWidth(), mTabHost.getMeasuredHeight());
                FrameActivity frame = (FrameActivity)getCurrentActivity();
                if (frame instanceof FrameActivity)
                    frame.fillLayout(); //  FILL ACTIVITY
                mMenuGroup.setVisibility(View.INVISIBLE);
            }
        });

        // Tab Host
        mTabHost = (TabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(getLocalActivityManager());

        // Frames
        mTabHost.addTab(mTabHost.newTabSpec("profile").setIndicator("").setContent(new Intent(this, ProfileActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("dating").setIndicator("").setContent(new Intent(this, DatingActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("likes").setIndicator("").setContent(new Intent(this, LikesActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("mutual").setIndicator("").setContent(new Intent(this, SympathyActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("tops").setIndicator("").setContent(new Intent(this, TopsActivity.class)));
        //mTabHost.addTab(mTabHost.newTabSpec("messages").setIndicator("").setContent(new Intent(this, InboxActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("messages").setIndicator("").setContent(new Intent(this, DialogActivity.class)));
        mTabHost.addTab(mTabHost.newTabSpec("settings").setIndicator("").setContent(new Intent(this, SettingsActivity.class)));

        // Menu View Groups
        mMenuGroup = findViewById(R.id.loTFGroupMenu);

        // Header
        TextView tvHeaderTitle = (TextView)findViewById(R.id.tvTFHeaderTitle);
        tvHeaderTitle.setText("Адын");

        // Header Button - Open Menu
        Button btnOpenHomeMenu = (Button)findViewById(R.id.btnTFHome);
        btnOpenHomeMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMenuOpened) {
                    loFrameWindow.layout(0, 0, mTabHost.getMeasuredWidth(), mTabHost.getMeasuredHeight());
                    loFrameWindow.startAnimation(mBackAnimation);
                } else {
                    mMenuGroup.setVisibility(View.VISIBLE);
                    loFrameWindow.startAnimation(mStartAnimation);
                }
                isMenuOpened = !isMenuOpened;
            }
        });

        // Profile
        final Button btnTFProfile = (Button)findViewById(R.id.btnTFProfile);
        btnTFProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_PROFILE;
                openFrame(btnTFProfile);
            }
        });

        // Dating
        final Button btnTFDating = (Button)findViewById(R.id.btnTFDating);
        btnTFDating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_DATING;
                openFrame(btnTFDating);
            }
        });

        // Likes
        final Button btnTFLikes = (Button)findViewById(R.id.btnTFLikes);
        btnTFLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_LIKES;
                openFrame(btnTFLikes);
            }
        });

        // Sympathy - Mutual
        final Button btnTFSympathy = (Button)findViewById(R.id.btnTFSympathy);
        btnTFSympathy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_SYMPATHY;
                openFrame(btnTFSympathy);
            }
        });

        // Messages - Chat
        final Button btnTFChat = (Button)findViewById(R.id.btnTFChat);
        btnTFChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_CHAT;
                openFrame(btnTFChat);
            }
        });

        // Tops
        final Button btnTFTops = (Button)findViewById(R.id.btnTFTops);
        btnTFTops.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_TOPS;
                openFrame(btnTFTops);
            }
        });

        // Settings
        final Button btnTFSettings = (Button)findViewById(R.id.btnTFSettings);
        btnTFSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextFrameName = FRAME_SETTINGS;
                openFrame(btnTFSettings);
            }
        });

        mFrameButtons = new Button[]{btnTFProfile,btnTFDating,btnTFLikes,btnTFSympathy,btnTFChat,btnTFTops,btnTFSettings};

        updateProfile();

        // Preferences, Last opened frame  // !!!!!!!!!!!!!!!!!!!!!!!!!
    }
    @Override
	protected void onStart() {    	
		super.onStart();
		Data.facebook.extendAccessTokenIfNeeded(this, null);
	}
	//---------------------------------------------------------------------------
    private void openFrame(final Button button) {
        if (button.isSelected()) {
            loFrameWindow.layout(0, 0, mTabHost.getMeasuredWidth(), mTabHost.getMeasuredHeight());
            loFrameWindow.startAnimation(mBackAnimation);
        } else {
            for (Button btn : mFrameButtons)
                btn.setSelected(false);
            button.setSelected(true);
            loFrameWindow.startAnimation(mHideFrameAnimation);
        }
        isMenuOpened = false;
    }
    //---------------------------------------------------------------------------
    private void updateProfile() {
        ProfileRequest profileRequest = new ProfileRequest(getApplicationContext());
        profileRequest.part = ProfileRequest.P_DASHBOARD;
        profileRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                CacheProfile.setData(Profile.parse(response));
                Http.avatarOwnerPreloading();
                post(new Runnable() {
                    @Override
                    public void run() {
                        CacheProfile.setData(Profile.parse(response));
                        Http.avatarOwnerPreloading();
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {

        // Рилизить данные !!!

        Data.release();
        Recycle.release();

        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------    
}
/* RelativeLayout.LayoutParams params = new
 * RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
 * LayoutParams.WRAP_CONTENT);
 * params.width = mTabHost.getWidth();
 * params.height = mTabHost.getHeight();
 * params.leftMargin = 400;
 * mTabHost.setLayoutParams(params); */
