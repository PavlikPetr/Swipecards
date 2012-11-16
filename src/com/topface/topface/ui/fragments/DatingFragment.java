package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.topface.topface.Data;
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
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.utils.*;

import java.util.LinkedList;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker,
        RateController.OnRateControllerListener {

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
    private ImageSwitcher mImageSwitcher;
    private LinkedList<SearchUser> mUserSearchList;
    private ProgressBar mProgressBar;
    private Novice mNovice;
    private AlphaAnimation mAlphaAnimation;
    private RateController mRateController;
    private View mNavigationHeader;
    private View mNavigationHeaderShadow;
    private RelativeLayout mDatingLoveBtnLayout;
    private ViewFlipper mViewFlipper;

    private ImageButton mRetryBtn;
    private PreloadManager mPreloadManager;

    private Drawable singleMutual;
    private Drawable singleDelight;
    private Drawable doubleMutual;
    private Drawable doubleDelight;

    private NoviceLayout mNoviceLayout;
    private View mDatingResources;

    private boolean hasOneSympathyOrDelight = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        View view = inflater.inflate(R.layout.ac_dating, null);

        mRetryBtn = (ImageButton) view.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);

        mViewFlipper = (ViewFlipper) view.findViewById(R.id.vfFlipper);

        if (isAdded()) {
            singleMutual = getResources().getDrawable(R.drawable.dating_mutual_selector);
            singleDelight = getResources().getDrawable(R.drawable.dating_delight_selector);

            doubleMutual = getResources().getDrawable(R.drawable.dating_dbl_mutual_selector);
            doubleDelight = getResources().getDrawable(R.drawable.dating_dbl_delight_selector);
        }

        // Data
        if (Data.searchList != null) {
            mUserSearchList = Data.searchList;
        } else {
            mUserSearchList = new LinkedList<SearchUser>();
            // Храним список пользователей в статичном поле, дабы каждый раз не
            // перезапрашивать
            Data.searchList = mUserSearchList;
        }

        // Navigation Header
        mNavBarController = new NavigationBarController(
                (ViewGroup) view.findViewById(R.id.loNavigationBar));
        view.findViewById(R.id.btnNavigationHome).setOnClickListener(
                (NavigationActivity) getActivity());

        setHeader(view);

        mNavigationHeader = view.findViewById(R.id.loNavigationBar);
        ImageButton settingsButton = (ImageButton) view.findViewById(R.id.btnNavigationSettingsBar);
        settingsButton.setVisibility(View.VISIBLE);
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        EditContainerActivity.class);
                startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER);
            }
        });
        mNavigationHeaderShadow = view.findViewById(R.id.ivHeaderShadow);

        // Rate Controller
        mRateController = new RateController(getActivity());
        mRateController.setOnRateControllerListener(this);

        // Если мы вернулись в этот фрагмент, то декриментим позицию, что бы
        // оказаться на последнем пользователе
        Data.searchPosition--;

        // Dating controls
        mDatingGroup = view.findViewById(R.id.loDatingGroup);

        // Preferences
        SharedPreferences preferences = getActivity().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);

        // Resources
        mDatingResources = view.findViewById(R.id.loDatingResources);
        mDatingResources.setOnClickListener(this);
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

        // Newbie
        mNovice = new Novice(preferences);
        mNoviceLayout = (NoviceLayout) view.findViewById(R.id.loNovice);

        mPreloadManager = new PreloadManager(getActivity().getApplicationContext());
        showNextUser();
        return view;
    }

    private void updateData(final boolean isAddition) {
        lockControls();
        if (!isAddition)
            onUpdateStart(isAddition);
        Debug.log(this, "update");
        SharedPreferences preferences = getActivity().getSharedPreferences(
                Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        SearchRequest searchRequest = new SearchRequest(getActivity());
        registerRequest(searchRequest);
        searchRequest.limit = 20;
        searchRequest.geo = preferences.getBoolean(getString(R.string.cache_profile_filter_geo),
                false);
        searchRequest.online = preferences.getBoolean(
                getString(R.string.cache_profile_filter_online), false);
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
                        Toast.makeText(getActivity(), getString(R.string.general_data_error),
                                Toast.LENGTH_SHORT).show();
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
                    if (Data.searchPosition > mUserSearchList.size() - 1) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        mRateController.onRate(currentSearch.id, 10,
                                currentSearch.mutual ? RateRequest.DEFAULT_MUTUAL
                                        : RateRequest.DEFAULT_NO_MUTUAL);
                    }
                    // currentSearch.rated = true;
                }
            }
            break;
            case R.id.btnDatingSympathy: {
                SearchUser currentSearch = getCurrentUser();
                if (currentSearch != null) {
                    if (Data.searchPosition > mUserSearchList.size() - 1) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        mRateController.onRate(currentSearch.id, 9,
                                currentSearch.mutual ? RateRequest.DEFAULT_MUTUAL
                                        : RateRequest.DEFAULT_NO_MUTUAL);
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
                intent.putExtra(UserProfileActivity.INTENT_USER_ID,
                        mUserSearchList.get(Data.searchPosition).id);
                intent.putExtra(UserProfileActivity.INTENT_USER_NAME,
                        mUserSearchList.get(Data.searchPosition).first_name);
                intent.putExtra(UserProfileActivity.INTENT_PREV_ENTITY, DatingFragment.this.getClass()
                        .getSimpleName());
                startActivity(intent);
            }
            break;
            case R.id.btnDatingChat: {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.INTENT_USER_ID,
                        mUserSearchList.get(Data.searchPosition).id);
                intent.putExtra(ChatActivity.INTENT_USER_NAME,
                        mUserSearchList.get(Data.searchPosition).first_name);
                intent.putExtra(ChatActivity.INTENT_USER_SEX,
                        mUserSearchList.get(Data.searchPosition).sex);
                intent.putExtra(ChatActivity.INTENT_USER_AGE,
                        mUserSearchList.get(Data.searchPosition).age);
                intent.putExtra(ChatActivity.INTENT_USER_CITY,
                        mUserSearchList.get(Data.searchPosition).city.name);
                intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, DatingFragment.this.getClass()
                        .getSimpleName());
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
        if (Data.searchPosition < mUserSearchList.size() - 1) {
            ++Data.searchPosition;
            fillUserInfo(getCurrentUser());
            unlockControls();
        }
        if (Data.searchPosition == mUserSearchList.size() - 1
                || mUserSearchList.size() - 6 <= Data.searchPosition) {
            if (Data.searchPosition < 0) {
                updateData(false);
            } else {
                updateData(true);
            }
        }

        mPreloadManager.preloadPhoto(mUserSearchList, Data.searchPosition + 1);
        if (getCurrentUser() != null) {
            showNovice();
            hasOneSympathyOrDelight = true;
        }
    }

    private void prevUser() {
        if (Data.searchPosition > 0) {
            --Data.searchPosition;
            fillUserInfo(mUserSearchList.get(Data.searchPosition));
        }
    }

    private void fillUserInfo(SearchUser currUser) {
        // User Info
        if (currUser != null) {
            lockControls();
            if (currUser.city != null) {
                mUserInfoCity.setText(currUser.city.name);
            }
            mUserInfoStatus.setText(currUser.status);
            mUserInfoName.setText(currUser.first_name + ", " + currUser.age);
            if (isAdded()) {
                if (currUser.online) {
                    mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.im_online), null, null, null);
                } else {
                    mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(
                            getResources().getDrawable(R.drawable.im_offline), null, null, null);
                }

                if (currUser.sex == Static.BOY) {
                    mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources()
                            .getDrawable(R.drawable.dating_man_selector), null, null);
                } else if (currUser.sex == Static.GIRL) {
                    mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, getResources()
                            .getDrawable(R.drawable.dating_woman_selector), null, null);
                }
            }

            // buttons drawables
            mMutualBtn.setCompoundDrawablesWithIntrinsicBounds(null, currUser.mutual ? doubleMutual
                    : singleMutual, null, null);
            mMutualBtn.setText(currUser.mutual ? getString(R.string.general_mutual)
                    : getString(R.string.general_sympathy));

            mDelightBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    currUser.mutual ? doubleDelight : singleDelight, null, null);

            // photos
            mImageSwitcher.setData(currUser.photos);
            mImageSwitcher.setCurrentItem(0, true);
            mCurrentPhotoPrevPos = 0;
            setCounter(mCurrentPhotoPrevPos);
        }
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
                                mResourcesPower.setBackgroundResource(Utils
                                        .getBatteryResource(CacheProfile.power));
                                mResourcesPower.setText(CacheProfile.power + "%");
                                mResourcesMoney.setText("" + CacheProfile.money);
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.general_server_error),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {

                }
            }).exec();
        }
        showNextUser();
    }

    private void showNovice() {
        if (mNovice.isDatingCompleted())
            return;

        if (mNovice.showSympathy) {
            mNoviceLayout.setLayoutRes(R.layout.novice_sympathy, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mMutualBtn.performClick();
                }
            });
            mNoviceLayout.startAnimation(mAlphaAnimation);
            mNovice.completeShowSympathy();
        } else if (mNovice.showBatteryBonus) {
            mNoviceLayout.setLayoutRes(R.layout.novice_battery_bonus, null,
                    mOnNewbieEnergyClickListener);
            mNoviceLayout.startAnimation(mAlphaAnimation);
            mNovice.completeShowBatteryBonus();
        } else if (mNovice.showEnergy && hasOneSympathyOrDelight && CacheProfile.power <= 10) {
            mNoviceLayout.setLayoutRes(R.layout.novice_energy, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDatingResources.performClick();
                }
            });
            mNoviceLayout.startAnimation(mAlphaAnimation);
            mNovice.completeShowEnergy();
        }
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

    private SearchUser getCurrentUser() {
        try {
            return mUserSearchList.get(Data.searchPosition);
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
        if (!mUserSearchList.isEmpty() && Data.searchPosition < mUserSearchList.size()
                && currentUser != null) {
            enabled = !currentUser.rated;
        }
        mMutualBtn.setEnabled(enabled);
        mDelightBtn.setEnabled(enabled);

        mSkipBtn.setEnabled(true);
        mPrevBtn.setEnabled(Data.searchPosition > 0);

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
        mNavigationHeaderShadow.setVisibility(View.VISIBLE);
        mDatingGroup.setVisibility(View.VISIBLE);
        mIsHide = false;
    }

    @Override
    public void hideControls() {
        mDatingGroup.setVisibility(View.INVISIBLE);
        mNavigationHeader.setVisibility(View.INVISIBLE);
        mNavigationHeaderShadow.setVisibility(View.INVISIBLE);
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
        if (resultCode == Activity.RESULT_OK
                && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            lockControls();
            updateData(false);
            View view = getView();
            setHeader(view);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setHeader(View view) {
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getString(
                CacheProfile.dating_sex == Static.BOY ? R.string.dating_header_guys
                        : R.string.dating_header_girls, CacheProfile.dating_age_start,
                CacheProfile.dating_age_end));

        TextView subTitle = (TextView) view.findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);
        subTitle.setText(CacheProfile.dating_city_name != null
                && !CacheProfile.dating_city_name.equals("") ? CacheProfile.dating_city_name
                : getString(R.string.filter_cities_all)

        );
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

    private Handler mUnlockHandler;

    {
        mUnlockHandler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                unlockControls();
            }
        };
    }

    public void onDialogCancel() {
        unlockControls();
    }

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

    private View.OnClickListener mOnNewbieEnergyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mResourcesPower.setBackgroundResource(R.anim.battery);
            mResourcesPower.setText("");
            final AnimationDrawable mailAnimation = (AnimationDrawable) mResourcesPower
                    .getBackground();
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
                            mResourcesPower.setText("+" + CacheProfile.power + "%");
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
