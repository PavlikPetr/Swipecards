package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.fragments.DatingFragment;
import com.topface.topface.ui.fragments.GiftsFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.IndicatorView;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.RateController.OnRateControllerListener;
import com.topface.topface.utils.http.ProfileBackgrounds;
import org.json.JSONArray;

public class UserProfileActivity extends BaseFragmentActivity {

    private int mUserId;

    private ImageViewRemote mUserAvatar;
    private TextView mUserName;
    private TextView mUserCity;
    private ViewGroup mUserProfileHeader;

    private Button mUserDelight;
    private Button mUserMutual;

    private RadioGroup mUserRadioGroup;
    private RadioButton mUserPhoto;
    private RadioButton mUserForm;
    private RadioButton mUserGifts;
    private RadioButton mUserActions;

    private RateController mRateController;
    private IndicatorView mIndicatorView;
    private LockerView mLockerView;
    private ViewPager mViewPager;

    private UserFormFragment mFormFragment;
    private UserPhotoFragment mPhotoFragment;

    public User mUser;

    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_CHAT_INVOKE = "chat_invoke";

    public static final int F_PHOTO = 0;
    public static final int F_FORM = 1;
    public static final int F_GIFTS = 2;
    //    public static final int F_ACTIONS = 3;
    public static final int F_COUNT = F_GIFTS + 1;

    public static final int GIFTS_LOAD_COUNT = 30;

    private RelativeLayout lockScreen;
    private OnClickListener finishActivityListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_user_profile);

        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1); // свой - чужой профиль

        // Navigation bar
        String userName = getIntent().getStringExtra(INTENT_USER_NAME); // name
        ((TextView) findViewById(R.id.tvNavigationTitle)).setText(userName);

        findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        if (getIntent().hasExtra(INTENT_PREV_ENTITY)) {
            Button btnBack = (Button) findViewById(R.id.btnNavigationBackWithText);
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(finishActivityListener);
            String prevEntity = getIntent().getStringExtra(INTENT_PREV_ENTITY);
            if (prevEntity.equals(ChatActivity.class.getSimpleName())) {
                btnBack.setText(R.string.general_chat);
            } else if (prevEntity.equals(DatingFragment.class.getSimpleName())) {
                btnBack.setText(R.string.general_dating);
            } else if (prevEntity.equals(DialogsFragment.class.getSimpleName())) {
                btnBack.setText(R.string.general_dialogs);
            } else if (prevEntity.equals(LikesFragment.class.getSimpleName())) {
                btnBack.setText(R.string.general_likes_me);
            } else if (prevEntity.equals(MutualFragment.class.getSimpleName())) {
                btnBack.setText(R.string.general_mutual);
            } else if (prevEntity.equals(VisitorsFragment.class.getSimpleName())) {
                btnBack.setText(R.string.general_visitors);
            }
        } else {
            ImageButton btnBack = (ImageButton) findViewById(R.id.btnNavigationBack);
            btnBack.setVisibility(View.VISIBLE);
            btnBack.setOnClickListener(finishActivityListener);
        }

        mUserProfileHeader = (ViewGroup) findViewById(R.id.loProfileHeader);

        mRateController = new RateController(this);
        mLockerView = (LockerView) findViewById(R.id.llvProfileLoading);

        mUserAvatar = (ImageViewRemote) findViewById(R.id.ivUserAvatar);
        mUserName = (TextView) findViewById(R.id.ivUserName);
        mUserCity = (TextView) findViewById(R.id.ivUserCity);

        mUserDelight = (Button) findViewById(R.id.btnUserDelight);
        mUserDelight.setOnClickListener(mRatesClickListener);
        mUserMutual = (Button) findViewById(R.id.btnUserMutual);
        mUserMutual.setOnClickListener(mRatesClickListener);
        findViewById(R.id.btnUserChat)
                .setOnClickListener(mRatesClickListener);

        mUserRadioGroup = (RadioGroup) findViewById(R.id.UserRadioGroup);
        mUserPhoto = (RadioButton) findViewById(R.id.btnUserPhoto);
        mUserPhoto.setOnClickListener(mInfoClickListener);
        mUserForm = (RadioButton) findViewById(R.id.btnUserQuestionnaire);
        mUserForm.setOnClickListener(mInfoClickListener);
        mUserGifts = (RadioButton) findViewById(R.id.btnUserGifts);
        mUserGifts.setOnClickListener(mInfoClickListener);
        mUserActions = (RadioButton) findViewById(R.id.btnUserActions);
        mUserActions.setOnClickListener(mInfoClickListener);

        lockScreen = (RelativeLayout) findViewById(R.id.lockScreen);
        RetryView retryBtn = new RetryView(getApplicationContext());
        retryBtn.init(getLayoutInflater());
        retryBtn.setErrorMsg(getString(R.string.general_profile_error));
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile();
                lockScreen.setVisibility(View.GONE);
            }
        });
        lockScreen.addView(retryBtn);

        mViewPager = (ViewPager) findViewById(R.id.UserViewPager);
        mViewPager.setAdapter(new UserProfilePageAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);

        mIndicatorView = (IndicatorView) findViewById(R.id.viewUserIndicator);
        mIndicatorView.setIndicator(F_PHOTO);
        ViewTreeObserver vto = mIndicatorView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = mIndicatorView.getViewTreeObserver();
                //noinspection deprecation
                obs.removeGlobalOnLayoutListener(this);

                mIndicatorView.setButtonMeasure(R.id.btnUserPhoto, mUserPhoto.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserQuestionnaire, mUserForm.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserGifts, mUserGifts.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserActions, mUserActions.getMeasuredWidth());
                mIndicatorView.reCompute();
            }
        });

        mUserPhoto.setChecked(true);

        getUserProfile();
    }

    private void getUserProfile() {
        mLockerView.setVisibility(View.VISIBLE);
        if (mUserId < 1) {
            mLockerView.setVisibility(View.INVISIBLE);
            lockScreen.setVisibility(View.VISIBLE);
            lockScreen.findViewById(R.id.retry).setVisibility(View.GONE);
            return;
        }
        UserRequest userRequest = new UserRequest(mUserId, getApplicationContext());
        userRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                try {
                    Object test = response.jsonResult.get("profiles");
                    if (test.equals(new JSONArray("[]"))) lockScreen.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Debug.error(e);
                }
                mUser = User.parse(mUserId, response);
                mRateController.setOnRateControllerListener(mRateControllerListener);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mUser.mutual) {
                            mUserDelight.setCompoundDrawablesWithIntrinsicBounds(null,
                                    getResources().getDrawable(R.drawable.user_dbl_delight_selector),
                                    null, null);
                            mUserMutual.setCompoundDrawablesWithIntrinsicBounds(null,
                                    getResources().getDrawable(R.drawable.user_dbl_mutual_selector),
                                    null, null);
                            mUserDelight.setEnabled(!mUser.rated);
                            mUserMutual.setEnabled(!mUser.rated);
                        }
                        mUserProfileHeader.setBackgroundResource(ProfileBackgrounds.getBackgroundResource(getApplicationContext(), mUser.background));
                        mLockerView.setVisibility(View.INVISIBLE);
                        mUserAvatar.setPhoto(mUser.photo);
                        mUserName.setText(mUser.getNameAndAge());
                        mUserCity.setText(mUser.city_name);
                        ((TextView) findViewById(R.id.tvNavigationTitle)).setText(mUser.getNameAndAge());
                        if (mFormFragment != null)
                            mFormFragment.setUserData(mUser);
                        if (mPhotoFragment != null)
                            mPhotoFragment.setUserData(mUser);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Toast.makeText(UserProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mLockerView.setVisibility(View.GONE);
                        lockScreen.setVisibility(View.VISIBLE);
                        lockScreen.findViewById(R.id.retry).setVisibility(View.VISIBLE);
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
                    mRateController.onRate(mUserId, 10, mUser.mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL);
                    mUserDelight.setEnabled(false);
                    mUserMutual.setEnabled(false);
                    break;
                case R.id.btnUserMutual:
                    mRateController.onRate(mUserId, 9, mUser.mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL);
                    mUserDelight.setEnabled(false);
                    mUserMutual.setEnabled(false);
                    break;
                case R.id.btnUserChat:
                    Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.INTENT_USER_ID, mUser.uid);
                    intent.putExtra(ChatActivity.INTENT_USER_NAME, mUser.first_name);
                    intent.putExtra(ChatActivity.INTENT_USER_SEX, mUser.sex);
                    intent.putExtra(ChatActivity.INTENT_USER_AGE, mUser.age);
                    intent.putExtra(ChatActivity.INTENT_USER_CITY, mUser.city_name);
                    intent.putExtra(ChatActivity.INTENT_PROFILE_INVOKE, true);
                    intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, UserProfileActivity.this.getClass().getSimpleName());
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };

    OnRateControllerListener mRateControllerListener = new OnRateControllerListener() {

        @Override
        public void successRate() {
            mUser.rated = true;
            mUserDelight.setEnabled(!mUser.rated);
            mUserMutual.setEnabled(!mUser.rated);
        }

        @Override
        public void failRate() {
            mUser.rated = false;
            mUserDelight.setEnabled(!mUser.rated);
            mUserMutual.setEnabled(!mUser.rated);
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
                    mIndicatorView.setIndicator(F_FORM);
                    mViewPager.setCurrentItem(F_FORM);
                    break;
                case R.id.btnUserGifts:
                    mIndicatorView.setIndicator(F_GIFTS);
                    mViewPager.setCurrentItem(F_GIFTS);
                    break;
//                case R.id.btnUserActions:
//                    mIndicatorView.setIndicator(F_ACTIONS);
//                    mViewPager.setCurrentItem(F_ACTIONS);
//                    break;
            }
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            switch (arg0) {
                case F_PHOTO:
                    mIndicatorView.setIndicator(F_PHOTO);
                    ((RadioButton) mUserRadioGroup.getChildAt(F_PHOTO)).setChecked(true);
                    break;
                case F_FORM:
                    mIndicatorView.setIndicator(F_FORM);
                    ((RadioButton) mUserRadioGroup.getChildAt(F_FORM)).setChecked(true);
                    break;
                case F_GIFTS:
                    mIndicatorView.setIndicator(F_GIFTS);
                    ((RadioButton) mUserRadioGroup.getChildAt(F_GIFTS)).setChecked(true);
                    break;
//                case F_ACTIONS:
//                    mIndicatorView.setIndicator(F_ACTIONS);
//                    ((RadioButton) mUserRadioGroup.getChildAt(F_ACTIONS)).setChecked(true);
//                    break;
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
            return F_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case F_PHOTO:
                    fragment = mPhotoFragment = new UserPhotoFragment();
                    break;
                case F_FORM:
                    fragment = mFormFragment = new UserFormFragment();
                    break;
                case F_GIFTS:
                    fragment = new GiftsFragment();
                    break;
//                case F_ACTIONS:
//                    fragment = new UserActionsFragment();
//                    break;
            }
            return fragment;
        }
    }
}
