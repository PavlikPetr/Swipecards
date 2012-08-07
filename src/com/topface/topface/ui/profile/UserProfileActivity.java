package com.topface.topface.ui.profile;

import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.fragments.GiftsFragment;
import com.topface.topface.ui.views.IndicatorView;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileActivity extends FragmentActivity {
    
    private int mUserId;
    private int mMutualId;
    private boolean mChatInvoke;
   
    private ImageView mUserAvatar;
    private TextView  mUserName;
    private TextView  mUserCity;
    
    private Button mUserDelight;
    private Button mUserMutual;
    private Button mUserChat;
    
    private RadioGroup  mUserRadioGroup;
    private RadioButton mUserPhoto;
    private RadioButton mUserQuestionnaire;
    private RadioButton mUserGifts;
    private RadioButton mUserActions;
    
    private RateController mRateController;    
    private IndicatorView mIndicatorView;
    private LockerView mLockerView;
    private ViewPager mViewPager;

    public User mDataUser;
    
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_MUTUAL_ID = "mutual_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_CHAT_INVOKE = "chat_invoke";
    
    public static final int F_PHOTO = 0;
    public static final int F_QUESTIONNAIRE = 1;
    public static final int F_GIFTS = 2;
    public static final int F_ACTIONS = 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_user_profile);

        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1); // свой - чужой профиль
        mMutualId = getIntent().getIntExtra(INTENT_MUTUAL_ID, -1);        
        mChatInvoke = getIntent().getBooleanExtra(INTENT_CHAT_INVOKE, false); // пришли из чата
        
        // Header Name
        String name = getIntent().getStringExtra(INTENT_USER_NAME); // name

        mRateController = new RateController(this);
        mLockerView = (LockerView)findViewById(R.id.llvProfileLoading);
        
        mUserAvatar = (ImageView)findViewById(R.id.ivUserAvatar);
        mUserName   = (TextView)findViewById(R.id.ivUserName);
        mUserCity   = (TextView)findViewById(R.id.ivUserCity);
        
        mUserDelight = (Button)findViewById(R.id.btnUserDelight);
        mUserDelight.setOnClickListener(mRatesClickListener);
        mUserMutual = (Button)findViewById(R.id.btnUserMutual);
        mUserMutual.setOnClickListener(mRatesClickListener);
        mUserChat = (Button)findViewById(R.id.btnUserChat);
        mUserChat.setOnClickListener(mRatesClickListener);
        
        mUserRadioGroup = (RadioGroup)findViewById(R.id.UserRadioGroup);
        mUserPhoto = (RadioButton)findViewById(R.id.btnUserPhoto);
        mUserPhoto.setOnClickListener(mInfoClickListener);
        mUserQuestionnaire = (RadioButton)findViewById(R.id.btnUserQuestionnaire);
        mUserQuestionnaire.setOnClickListener(mInfoClickListener);
        mUserGifts = (RadioButton)findViewById(R.id.btnUserGifts);
        mUserGifts.setOnClickListener(mInfoClickListener);
        mUserActions = (RadioButton)findViewById(R.id.btnUserActions);
        mUserActions.setOnClickListener(mInfoClickListener);
        
        mUserPhoto.setChecked(true);
        
        mViewPager = (ViewPager)findViewById(R.id.UserViewPager);
        mViewPager.setAdapter(new UserProfilePageAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
            
        mIndicatorView = (IndicatorView)findViewById(R.id.viewUserIndicator);
        mIndicatorView.setIndicator(F_PHOTO);
        ViewTreeObserver vto = mIndicatorView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = mIndicatorView.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

                mIndicatorView.setButtonMeasure(R.id.btnUserPhoto, mUserPhoto.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserQuestionnaire, mUserQuestionnaire.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserGifts, mUserGifts.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserActions,mUserActions.getMeasuredWidth());
                mIndicatorView.reCompute();
            }

        });
        
        getUserProfile();
    }

    private void getUserProfile() {
        mLockerView.setVisibility(View.VISIBLE);
        UserRequest userRequest = new UserRequest(getApplicationContext());
        userRequest.uids.add(mUserId);
        userRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                mDataUser = User.parse(mUserId, response);
                Bitmap rawBitmap = Http.bitmapLoader(mDataUser.getBigLink());
                final Bitmap avatar = Utils.getRoundedBitmap(rawBitmap, 100, 100);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLockerView.setVisibility(View.INVISIBLE);
                        mUserAvatar.setImageBitmap(avatar);
                        mUserName.setText(mDataUser.first_name + ", " + mDataUser.age);
                        mUserCity.setText(mDataUser.city_name);
                    }
                });
            }
            @Override
            public void fail(int codeError, ApiResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }
    
    View.OnClickListener mRatesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnUserDelight:
                    mRateController.onRate(mUserId, 10);
                    break;
                case R.id.btnUserMutual:
                    mRateController.onRate(mUserId, 9);
                    break;
                case R.id.btnUserChat:
                    Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.INTENT_USER_ID,   mDataUser.uid);
                    intent.putExtra(ChatActivity.INTENT_USER_URL,  mDataUser.getSmallLink());                
                    intent.putExtra(ChatActivity.INTENT_USER_NAME, mDataUser.first_name);                
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
    
    View.OnClickListener mInfoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnUserPhoto:
                    mIndicatorView.setIndicator(F_PHOTO);
                    mViewPager.setCurrentItem(F_PHOTO);
                    break;
                case R.id.btnUserQuestionnaire:
                    mIndicatorView.setIndicator(F_QUESTIONNAIRE);
                    mViewPager.setCurrentItem(F_QUESTIONNAIRE);
                    break;
                case R.id.btnUserGifts:
                    mIndicatorView.setIndicator(F_GIFTS);
                    mViewPager.setCurrentItem(F_GIFTS);
                    break;
                case R.id.btnUserActions:
                    mIndicatorView.setIndicator(F_ACTIONS);
                    mViewPager.setCurrentItem(F_ACTIONS);
                    break;
            }
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0,float arg1,int arg2) {
        }
        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case F_PHOTO:
                    mIndicatorView.setIndicator(F_PHOTO);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_PHOTO)).setChecked(true);
                    break;
                case F_QUESTIONNAIRE:
                    mIndicatorView.setIndicator(F_QUESTIONNAIRE);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_QUESTIONNAIRE)).setChecked(true);
                    break;
                case F_GIFTS:
                    mIndicatorView.setIndicator(F_GIFTS);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_GIFTS)).setChecked(true);
                    break;
                case F_ACTIONS:
                    mIndicatorView.setIndicator(F_ACTIONS);
                    ((RadioButton)mUserRadioGroup.getChildAt(F_ACTIONS)).setChecked(true);
                    break;
            }
        }
    };
    
    /*
     *     UserProfilePageAdapter
     */
    public class UserProfilePageAdapter extends FragmentPagerAdapter {

        public UserProfilePageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return F_ACTIONS+1;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null; 
            switch (position) {
                case F_PHOTO:
                    Bundle bundle = new Bundle();
                    bundle.putInt(UserProfileActivity.INTENT_USER_ID, mUserId);
                    fragment = new PhotoFragment();
                    fragment.setArguments(bundle);
                    break;
                case F_QUESTIONNAIRE:
                    fragment = new QuestionnaireFragment();
                    break;
                case F_GIFTS:
                    fragment = new GiftsFragment();
                    break;
                case F_ACTIONS:
                    fragment = new ActionsFragment();
                    break;
            }
            return fragment;
        }
    }
}
