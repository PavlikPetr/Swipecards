package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.NovicePower;
import com.topface.topface.data.Search;
import com.topface.topface.data.SearchUser;
import com.topface.topface.data.SkipRate;
import com.topface.topface.requests.*;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.*;

import java.util.LinkedList;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker, RateController.OnRateControllerListener {

    private int mCurrentUserPos;
    private int mCurrentPhotoPrevPos;
    private TextView mResourcesPower;
    private TextView mResourcesMoney;
    private Button mDelightBtn;
    private Button mMutualBtn;
    private Button mSkipBtn;
    private Button mPrevBtn;
    private Button mProfileBtn;
    private Button mChatBtn;
    private Button mSwitchNextBtn;
    private Button mSwitchPrevBtn;
    private TextView mUserInfoName;
    private TextView mUserInfoCity;
    private TextView mUserInfoStatus;
    private TextView mCounter;
    private View mDatingGroup;
    //    private View mFirstRateButtons;
//    private View mSecondRateButtons;
    private ImageSwitcher mImageSwitcher;
    private LinkedList<SearchUser> mUserSearchList;
    private ProgressBar mProgressBar;
    private Newbie mNewbie;
    private ImageView mNewbieView;
    private AlphaAnimation mAlphaAnimation;
    private SharedPreferences mPreferences;
    private RateController mRateController;
    private View mNavigationHeader;
    private RelativeLayout mDatingLoveBtnLayout;
    private ViewFlipper mViewFlipper;

    private ImageButton mRetryBtn;
    private PreloadManager mPreloadManager;

    private Drawable singleMutual;
    private Drawable singleDelight;
    private Drawable doubleMutual;
    private Drawable doubleDelight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        View view = inflater.inflate(R.layout.ac_dating, null);

        mRetryBtn = (ImageButton) view.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);

        mViewFlipper = (ViewFlipper) view.findViewById(R.id.vfFlipper);

        singleMutual = getResources().getDrawable(R.drawable.dating_mutual_selector);
        singleDelight = getResources().getDrawable(R.drawable.dating_delight_selector);

        doubleMutual = getResources().getDrawable(R.drawable.dating_dbl_mutual_selector);
        doubleDelight = getResources().getDrawable(R.drawable.dating_dbl_delight_selector);

        // Data
        mUserSearchList = new LinkedList<SearchUser>();

        // Navigation Header
        mNavBarController = new NavigationBarController((ViewGroup)view.findViewById(R.id.loNavigationBar));
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getResources().getString(R.string.dashbrd_btn_dating));
        mNavigationHeader = view.findViewById(R.id.loNavigationBar);
        Button settingsButton = (Button) view.findViewById(R.id.btnNavigationSettingsBar);
        settingsButton.setVisibility(View.VISIBLE);
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER);
            }
        });

        // Rate Controller
        mRateController = new RateController(getActivity());
        mRateController.setOnRateControllerListener(this);

        // Rate buttons groups
//        mFirstRateButtons = view.findViewById(R.id.ratingButtonsFirst);
//        mSecondRateButtons = view.findViewById(R.id.ratingButtonsSecond);

        // Position
        mCurrentUserPos = -1;

        // Dating controls
        mDatingGroup = view.findViewById(R.id.loDatingGroup);

        // Preferences
        mPreferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Newbie
        mNewbie = new Newbie(mPreferences);
        mNewbieView = (ImageView) view.findViewById(R.id.ivNewbie);

        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);

        // Resources
        view.findViewById(R.id.loDatingResources).setOnClickListener(this);
        mResourcesPower = (TextView) view.findViewById(R.id.tvResourcesPower);
        mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
        mResourcesPower.setText("" + CacheProfile.power + "%");
        mResourcesMoney = (TextView) view.findViewById(R.id.tvResourcesMoney);
        mResourcesMoney.setText("" + CacheProfile.money);

        // Control Buttons
        mDelightBtn = (Button) view.findViewById(R.id.btnDatingLove);
        mDelightBtn.setOnClickListener(this);
        mMutualBtn = (Button) view.findViewById(R.id.btnDatingSympathy);
        mMutualBtn.setOnClickListener(this);
        mSkipBtn = (Button) view.findViewById(R.id.btnDatingSkip);
        mSkipBtn.setOnClickListener(this);
        mPrevBtn = (Button) view.findViewById(R.id.btnDatingPrev);
        mPrevBtn.setOnClickListener(this);
        mProfileBtn = (Button) view.findViewById(R.id.btnDatingProfile);
        mProfileBtn.setOnClickListener(this);
        mChatBtn = (Button) view.findViewById(R.id.btnDatingChat);
        mChatBtn.setOnClickListener(this);
        mSwitchNextBtn = (Button) view.findViewById(R.id.btnDatingSwitchNext);
        mSwitchNextBtn.setOnClickListener(this);
        mSwitchPrevBtn = (Button) view.findViewById(R.id.btnDatingSwitchPrev);
        mSwitchPrevBtn.setOnClickListener(this);
        // Dating Love Price
        mDatingLoveBtnLayout = (RelativeLayout) view.findViewById(R.id.loDatingLove);

        // User Info
        mUserInfoName = ((TextView) view.findViewById(R.id.tvDatingUserName));
        mUserInfoCity = ((TextView) view.findViewById(R.id.tvDatingUserCity));
        mUserInfoStatus = ((TextView) view.findViewById(R.id.tvDatingUserStatus));

        // Counter
        mCounter = ((TextView) view.findViewById(R.id.tvDatingCounter));

        // Progress
        mProgressBar = (ProgressBar) view.findViewById(R.id.prsDatingLoading);

        // Dating Album
        mImageSwitcher = ((ImageSwitcher) view.findViewById(R.id.glrDatingAlbum));
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);
        mImageSwitcher.setUpdateHandler(mUnlockHandler);

        mPreloadManager = new PreloadManager(getActivity().getApplicationContext());

        updateData(false);

        return view;
    }

    private void updateData(final boolean isAddition) {
        lockControls();
        if (!isAddition)
            onUpdateStart(isAddition);
        Debug.log(this, "update");
        SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        SearchRequest searchRequest = new SearchRequest(getActivity());
        registerRequest(searchRequest);
        searchRequest.limit = 20;
        searchRequest.geo = preferences.getBoolean(getString(R.string.cache_profile_filter_geo), false);
        searchRequest.online = preferences.getBoolean(getString(R.string.cache_profile_filter_online), false);
        searchRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final Search userList = new Search(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isAddition) {
                            mUserSearchList.addAll(userList);
                        } else {
                            mUserSearchList.clear();
                            mUserSearchList.addAll(userList);
                            onUpdateSuccess(isAddition);
                            showNextUser();
                        }

                        unlockControls();
                    }
                });

            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        onUpdateFail(isAddition);
                        unlockControls();
                    }
                });
            }
        }).exec();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loDatingResources: {
                startActivity(new Intent(getActivity(), BuyingActivity.class));
            }
            break;
            case R.id.btnDatingLove: {
                SearchUser currentSearch = getCurrentUser();
                if (currentSearch != null) {
                    if (mCurrentUserPos > mUserSearchList.size() - 1) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        mRateController.onRate(currentSearch.id, 10, currentSearch.mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL);
                    }
//                    currentSearch.rated = true;
                }
            }
            break;
            case R.id.btnDatingSympathy: {
                SearchUser currentSearch = getCurrentUser();
                if (currentSearch != null) {
                    if (mCurrentUserPos > mUserSearchList.size() - 1) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        mRateController.onRate(currentSearch.id, 9, currentSearch.mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL);
                    }
                    currentSearch.rated = true;
                }
            }
            break;
            case R.id.btnDatingSkip: {
                skipUser();
                SearchUser currentSearch = getCurrentUser();
                if (currentSearch != null) {
                    currentSearch.skipped = true;
                }

            }
            break;
            case R.id.btnDatingPrev: {
                prevUser();
            }

            break;
            case R.id.btnDatingProfile: {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.INTENT_USER_ID, mUserSearchList.get(mCurrentUserPos).id);
                intent.putExtra(UserProfileActivity.INTENT_USER_NAME, mUserSearchList.get(mCurrentUserPos).first_name);
                intent.putExtra(UserProfileActivity.INTENT_PREV_ENTITY, DatingFragment.this.getClass().getSimpleName());
                startActivity(intent);
            }
            break;
            case R.id.btnDatingChat: {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.INTENT_USER_ID, mUserSearchList.get(mCurrentUserPos).id);
                intent.putExtra(ChatActivity.INTENT_USER_NAME, mUserSearchList.get(mCurrentUserPos).first_name);
                intent.putExtra(ChatActivity.INTENT_USER_SEX, mUserSearchList.get(mCurrentUserPos).sex);
                intent.putExtra(ChatActivity.INTENT_USER_AGE, mUserSearchList.get(mCurrentUserPos).age);
                intent.putExtra(ChatActivity.INTENT_USER_CITY, mUserSearchList.get(mCurrentUserPos).city.name);
                intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, DatingFragment.this.getClass().getSimpleName());
                startActivity(intent);
            }
            break;
            case R.id.btnDatingSwitchNext: {
                mViewFlipper.setDisplayedChild(1);
            }
            break;
            case R.id.btnDatingSwitchPrev: {
                mViewFlipper.setDisplayedChild(0);
            }
            break;
            case R.id.btnUpdate: {
                updateData(false);
                mRetryBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
            default:
        }
    }

    private void showNextUser() {
        if (mCurrentUserPos < mUserSearchList.size() - 1) {
            ++mCurrentUserPos;
            fillUserInfo(getCurrentUser());
        }
        if (mCurrentUserPos == mUserSearchList.size() - 1 || mUserSearchList.size() - 6 <= mCurrentUserPos) {
            if (mCurrentUserPos < 0) {
                updateData(false);
            } else {
                updateData(true);
            }
        }
        mPreloadManager.preloadPhoto(mUserSearchList.get(mCurrentUserPos+1));

        //showNewbie(); // NEWBIE
    }

    private void prevUser() {
        if (mCurrentUserPos > 0) {
            --mCurrentUserPos;
            fillUserInfo(mUserSearchList.get(mCurrentUserPos));
        }
        showNewbie(); // NEWBIE
    }

    private void fillUserInfo(SearchUser currUser) {
        // User Info
        lockControls();
        mUserInfoCity.setText(currUser.city.name);
        mUserInfoStatus.setText(currUser.status);
        mUserInfoName.setText(currUser.first_name + ", " + currUser.age);
        if (currUser.online)
            mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.im_online), null, null, null);
        else
            mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.im_offline), null, null, null);

        if (currUser.sex == Static.BOY) {
            mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.dating_man_selector), null, null);
        } else if (currUser.sex == Static.GIRL) {
            mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    getResources().getDrawable(R.drawable.dating_woman_selector), null, null);
        }

        // buttons drawables
        mMutualBtn.setCompoundDrawablesWithIntrinsicBounds(null, currUser.mutual ? doubleMutual : singleMutual, null, null);
        mDelightBtn.setCompoundDrawablesWithIntrinsicBounds(null, currUser.mutual ? doubleDelight : singleDelight, null, null);

        //photos
        mImageSwitcher.setData(currUser.photos);
        mImageSwitcher.setCurrentItem(0, true);
        mCurrentPhotoPrevPos = 0;
        setCounter(mCurrentPhotoPrevPos);
    }

    private void skipUser() {

        SearchUser currentSearch = getCurrentUser();

        if (currentSearch != null && !currentSearch.skipped) {
            SkipRateRequest skipRateRequest = new SkipRateRequest(getActivity());
            registerRequest(skipRateRequest);
            skipRateRequest.userid = currentSearch.id;
            skipRateRequest.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    SkipRate skipRate = SkipRate.parse(response);
                    if (skipRate.completed) {
                        CacheProfile.power = skipRate.power;
                        CacheProfile.money = skipRate.money;
                        updateUI(new Runnable() {
                            @Override
                            public void run() {
                                mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
                                mResourcesPower.setText(CacheProfile.power + "%");
                                mResourcesMoney.setText("" + CacheProfile.money);
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {

                }
            }).exec();
        }
        showNextUser();
    }

    private void showNewbie() {
        mNewbieView.setVisibility(View.INVISIBLE);

        if (mNewbie.isDatingCompleted())
            return;

        SharedPreferences.Editor editor = mPreferences.edit();

        if (!mNewbie.free_energy && CacheProfile.isNewbie) {
            mNewbie.free_energy = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_FREE_ENERGY, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_free_energy);
            mNewbieView.setOnClickListener(mOnNewbieClickListener);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (!mNewbie.rate_it) {
            mNewbie.rate_it = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_RATE_IT, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_rate_it);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);
            mNewbieView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewbieView.setVisibility(View.INVISIBLE);
                }
            });

        } else if (!mNewbie.buy_energy && CacheProfile.power <= 30) {
            mNewbie.buy_energy = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_BUY_ENERGY, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_buy_energy);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);
            mNewbieView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewbieView.setVisibility(View.INVISIBLE);
                }
            });
        }

        editor.commit();
    }

    public void setCounter(int position) {
        SearchUser currentSearch = getCurrentUser();
        if (currentSearch != null) {
            mCounter.setText((position + 1) + "/" + currentSearch.photos.size());
            mCounter.setVisibility(View.VISIBLE);
        } else {
            mCounter.setText("-/-");
            mCounter.setVisibility(View.INVISIBLE);
        }
    }

//    public void switchRateBtnsGroups() {
//        mFirstRateButtons.setVisibility(mFirstRateButtons.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//        mSecondRateButtons.setVisibility(mSecondRateButtons.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
//    }

    private SearchUser getCurrentUser() {
        try {
            return mUserSearchList.get(mCurrentUserPos);
        } catch (Exception e) {
            Debug.log(e.toString());
            return null;
        }
    }

    @Override
    public void lockControls() {
        mProgressBar.setVisibility(View.VISIBLE);
        mUserInfoName.setVisibility(View.INVISIBLE);
        mUserInfoCity.setVisibility(View.INVISIBLE);
        mUserInfoStatus.setVisibility(View.INVISIBLE);
        mMutualBtn.setEnabled(false);
        mDelightBtn.setEnabled(false);
        mSkipBtn.setEnabled(false);
        mPrevBtn.setEnabled(false);
        mProfileBtn.setEnabled(false);
        mChatBtn.setEnabled(false);
        mDatingLoveBtnLayout.setEnabled(false);
        mSwitchNextBtn.setEnabled(false);
        mSwitchPrevBtn.setEnabled(false);
    }

    @Override
    public void unlockControls() {
        SearchUser currentUser = getCurrentUser();

        mProgressBar.setVisibility(View.GONE);
        mUserInfoName.setVisibility(currentUser != null ? View.VISIBLE : View.INVISIBLE);
        mUserInfoCity.setVisibility(currentUser != null ? View.VISIBLE : View.INVISIBLE);
        mUserInfoStatus.setVisibility(View.VISIBLE);

        boolean enabled = false;
        if (!mUserSearchList.isEmpty() && mCurrentUserPos < mUserSearchList.size() && currentUser != null) {
            enabled = !currentUser.rated;
        }
        mMutualBtn.setEnabled(enabled);
        mDelightBtn.setEnabled(enabled);

        mSkipBtn.setEnabled(true);
        mPrevBtn.setEnabled(mCurrentUserPos > 0);

        enabled = (currentUser != null);
        mProfileBtn.setEnabled(enabled);
        mChatBtn.setEnabled(enabled);

        mDatingLoveBtnLayout.setEnabled(true);
        mSwitchNextBtn.setEnabled(true);
        mSwitchPrevBtn.setEnabled(true);
        mCounter.setVisibility(View.VISIBLE);
    }

    @Override
    public void showControls() {
        mNavigationHeader.setVisibility(View.VISIBLE);
        mDatingGroup.setVisibility(View.VISIBLE);
        mIsHide = false;
    }

    @Override
    public void hideControls() {
        mDatingGroup.setVisibility(View.INVISIBLE);
        mNavigationHeader.setVisibility(View.INVISIBLE);
        mIsHide = true;
    }

    @Override
    public void successRate() {
        getCurrentUser().rated = true;
        showNextUser();
    }

    @Override
    public void failRate() {
        unlockControls();
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mProgressBar.setVisibility(View.GONE);
            mRetryBtn.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            lockControls();
            updateData(false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean mIsHide;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsHide) {
                showControls();
            } else {
                hideControls();
            }
        }
    };

    private Handler mUnlockHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            unlockControls();
        }
    };

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {

            if (position == 1 && mCurrentPhotoPrevPos == 0) {
                hideControls();
            } else if (position == 0 && mCurrentPhotoPrevPos > 0) {
                showControls();
            }
            mCurrentPhotoPrevPos = position;
            setCounter(mCurrentPhotoPrevPos);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };


    private View.OnClickListener mOnNewbieClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mNewbieView.setVisibility(View.INVISIBLE);
            mResourcesPower.setBackgroundResource(R.anim.battery);
            mResourcesPower.setText("");
            final AnimationDrawable mailAnimation = (AnimationDrawable) mResourcesPower.getBackground();
            mResourcesPower.post(new Runnable() {
                public void run() {
                    if (mailAnimation != null)
                        mailAnimation.start();
                }
            });
            NovicePowerRequest novicePowerRequest = new NovicePowerRequest(getActivity());
            registerRequest(novicePowerRequest);
            novicePowerRequest.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    NovicePower novicePower = NovicePower.parse(response);
                    CacheProfile.power = (int) (novicePower.power * 0.01);
                    updateUI(new Runnable() {
                        @Override
                        public void run() {
                            mResourcesPower.setText("+100%");
                        }
                    });
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                }
            }).exec();
        }
    };

}
