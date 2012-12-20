package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.*;
import com.topface.topface.requests.*;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.profile.*;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.ProfileActionsControl;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public final static int TYPE_MY_PROFILE = 1;
    public final static int TYPE_USER_PROFILE = 2;
    private static final String ARG_TAG_PROFILE_TYPE = "profile_type";
    private static final String ARG_TAG_PROFILE_ID = "profile_id";
    private static final String ARG_TAG_INIT_BODY_PAGE = "profile_start_body_class";
    private static final String ARG_TAG_INIT_HEADER_PAGE = "profile_start_header_class";

    ArrayList<String> BODY_PAGES_TITLES = new ArrayList<String>();
    ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<String>();
    ArrayList<String> HEADER_PAGES_CLASS_NAMES = new ArrayList<String>();

    private HeaderMainFragment mHeaderMainFragment;
    private HeaderStatusFragment mHeaderStatusFragment;
    private UserPhotoFragment mUserPhotoFragment;
    private UserFormFragment mUserFormFragment;

    private Profile mUserProfile = null;
    private int mProfileType;
    private int mProfileId;

    private ImageView mOnline;
    private TextView mTitle;
    private View mLoaderView;
    private RateController mRateController;
    protected NavigationBarController mNavBarController;
    private RelativeLayout mLockScreen;
    private RetryView mRetryBtn;
    private ViewPager mBodyPager;
    private ProfilePageAdapter mBodyPagerAdapter;
    private ViewPager mHeaderPager;
    private ProfilePageAdapter mHeaderPagerAdapter;
    private ProfileActionsControl mActionsControl;
    private GiftsFragment mGiftFragment;

    private String mBodyStartPageClassName;
    private String mHeaderStartPageClassName;
    private int mStartBodyPage = 0;
    private int mStartHeaderPage = 0;
    private BroadcastReceiver mMUpdateBlackListState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //init views
        View root = inflater.inflate(R.layout.ac_profile, null);

        mLoaderView = root.findViewById(R.id.llvProfileLoading);
        mActionsControl = (ProfileActionsControl) root.findViewById(R.id.profileActionsControl);
        mRateController = new RateController(getActivity());

        restoreState();

        mNavBarController = new NavigationBarController((ViewGroup) root.findViewById(R.id.loNavigationBar));
        root.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        mTitle = (TextView) root.findViewById(R.id.tvNavigationTitle);

        initHeaderPages(root);

        initBodyPages(root);

        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryBtn = new RetryView(getActivity().getApplicationContext());
        mRetryBtn.setErrorMsg(getString(R.string.general_profile_error));
        mRetryBtn.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile();
                mLockScreen.setVisibility(View.GONE);
            }
        });
        mLockScreen.addView(mRetryBtn);

        mActionsControl.setType(mProfileType);
        mActionsControl.setOnClickListener(this);

        if (mProfileType == TYPE_MY_PROFILE) {
            mActionsControl.setOnCheckChangedListener(this);
            mTitle.setText(R.string.profile_header_title);
            Button editButton = (Button) root.findViewById(R.id.btnNavigationRightWithText);
            editButton.setVisibility(View.VISIBLE);
            editButton.setText(getResources().getString(R.string.general_edit_button));
            editButton.setOnClickListener(this);
        } else if (mProfileType == TYPE_USER_PROFILE) {
            mOnline = (ImageView) root.findViewById(R.id.ivOnline);
        }

        // start pages initialization
        int startBodyPage = mBodyPagerAdapter.getFragmentIndexByClassName(mBodyStartPageClassName);
        if (startBodyPage != -1) {
            mStartBodyPage = startBodyPage;
        }
        int startHeaderPage = mHeaderPagerAdapter.getFragmentIndexByClassName(mHeaderStartPageClassName);
        if (startHeaderPage != -1) {
            mStartHeaderPage = startHeaderPage;
        }


        mHeaderPager.setCurrentItem(mStartHeaderPage);
        mBodyPager.setCurrentItem(mStartBodyPage);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mProfileType == TYPE_MY_PROFILE) {
            mUserProfile = CacheProfile.getProfile();
        } else {
            if (mUserProfile == null) getUserProfile();
        }

        mMUpdateBlackListState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mUserProfile != null) {
                    mUserProfile.inBlackList = intent.getBooleanExtra(ProfileBlackListControlFragment.BLACK_LIST_STATUS, false);
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMUpdateBlackListState, new IntentFilter(ProfileBlackListControlFragment.UPDATE_ACTION));
        setProfile(mUserProfile);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMUpdateBlackListState);
    }

    private void setProfile(Profile profile) {
        if (mHeaderMainFragment != null) mHeaderMainFragment.setProfile(profile);
        if (mHeaderStatusFragment != null) mHeaderStatusFragment.setProfile(profile);
        if (mGiftFragment != null) mGiftFragment.setProfile(profile);
        if (mUserPhotoFragment != null && profile instanceof User) mUserPhotoFragment.setUserData((User) profile);
        if (mUserFormFragment != null && profile instanceof User) mUserFormFragment.setUserData((User) profile);
    }

    private void getUserProfile() {
        mLoaderView.setVisibility(View.VISIBLE);
        if (mProfileId < 1) {
            mLoaderView.setVisibility(View.INVISIBLE);
            mRetryBtn.showOnlyMessage(true);
            mLockScreen.setVisibility(View.VISIBLE);
            return;
        }
        UserRequest userRequest = new UserRequest(mProfileId, getActivity().getApplicationContext());
        userRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                try {
                    Object test = response.jsonResult.get("profiles");
                    if (test.equals(new JSONArray("[]"))) mLockScreen.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Debug.error(e);
                }
                final User user = User.parse(mProfileId, response);
                mUserProfile = user;
                mRateController.setOnRateControllerListener(mRateControllerListener);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (user.mutual) {
                            //TODO: manipulations with like & admiration buttons
                        }
                        //set info into views for user
                        mTitle.setText(user.getNameAndAge());
                        mOnline.setVisibility(user.online ? View.VISIBLE : View.INVISIBLE);
                        setProfile(user);
                        mLoaderView.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        mLoaderView.setVisibility(View.GONE);
                        mLockScreen.setVisibility(View.VISIBLE);
                        mRetryBtn.showOnlyMessage(false);
                    }
                });
            }
        }).exec();
    }

    private void restoreState() {
        mProfileId = getArguments().getInt(ARG_TAG_PROFILE_ID);
        mProfileType = getArguments().getInt(ARG_TAG_PROFILE_TYPE);
        mBodyStartPageClassName = getArguments().getString(ARG_TAG_INIT_BODY_PAGE);
        mHeaderStartPageClassName = getArguments().getString(ARG_TAG_INIT_HEADER_PAGE);
    }

    private void initHeaderPages(View root) {
        addHeaderPage(HeaderMainFragment.class.getName());
        addHeaderPage(HeaderStatusFragment.class.getName());

        ViewPager headerPager = (ViewPager) root.findViewById(R.id.vpHeaderFragments);
        mHeaderPagerAdapter = new ProfilePageAdapter(getActivity().getSupportFragmentManager(), HEADER_PAGES_CLASS_NAMES);
        headerPager.setAdapter(mHeaderPagerAdapter);
        //Tabs for header
        CirclePageIndicator circleIndicator = (CirclePageIndicator) root.findViewById(R.id.cpiHeaderTabs);
        circleIndicator.setViewPager(headerPager);
        circleIndicator.setSnap(true);

        mHeaderPager = headerPager;
    }

    private void initBodyPages(View root) {
        if (mProfileType == TYPE_MY_PROFILE) {
            addBodyPage(ProfilePhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
            addBodyPage(ProfileFormFragment.class.getName(), getResources().getString(R.string.profile_form));
            addBodyPage(VipBuyFragment.class.getName(), getResources().getString(R.string.profile_vip_status));
            addBodyPage(ServicesFragment.class.getName(), getResources().getString(R.string.profile_services));
            addBodyPage(GiftsFragment.class.getName(), getResources().getString(R.string.profile_gifts));
        } else {
            addBodyPage(UserPhotoFragment.class.getName(), getResources().getString(R.string.profile_photo));
            addBodyPage(UserFormFragment.class.getName(), getResources().getString(R.string.profile_form));
            addBodyPage(GiftsFragment.class.getName(), getResources().getString(R.string.profile_gifts));
            addBodyPage(ProfileBlackListControlFragment.class.getName(), getResources().getString(R.string.profile_services));
        }

        ViewPager bodyPager = (ViewPager) root.findViewById(R.id.vpFragments);
        mBodyPagerAdapter = new ProfilePageAdapter(getActivity().getSupportFragmentManager(), BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES);
        bodyPager.setAdapter(mBodyPagerAdapter);
        //Tabs for Body
        TabPageIndicator tabIndicator = (TabPageIndicator) root.findViewById(R.id.tpiTabs);
        final Animation fadeOutAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        final Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);

        fadeInAnimation.setAnimationListener(new FadeAnimationListener(false));
        fadeOutAnimation.setAnimationListener(new FadeAnimationListener(true));

        tabIndicator.setViewPager(bodyPager);
        tabIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private boolean shouldAnimate;

            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int i) {
                if (((ProfilePageAdapter) mBodyPager.getAdapter()).getClassNameByFragmentIndex(i).equals(VipBuyFragment.class.getName())) {
                    shouldAnimate = true;
                    mActionsControl.startAnimation(fadeOutAnimation);
                } else {
                    if (shouldAnimate) {
                        mActionsControl.startAnimation(fadeInAnimation);
                        shouldAnimate = false;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        mBodyPager = bodyPager;
    }

    private void addHeaderPage(String className) {
        HEADER_PAGES_CLASS_NAMES.add(className);
    }

    private void addBodyPage(String className, String pageTitle) {
        BODY_PAGES_TITLES.add(pageTitle);
        BODY_PAGES_CLASS_NAMES.add(className);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNavigationRightWithText:
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
                break;
            case R.id.actionDelight:
                if (v.isEnabled()) {
                    mRateController.onRate(mUserProfile.uid, 10, ((User) mUserProfile).mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL);
                    v.setEnabled(false);
                    //noinspection deprecation
                    ((ImageButton) v).setAlpha(80);
                }
                break;
            case R.id.actionSympathy:
                if (v.isEnabled()) {
                    mRateController.onRate(mUserProfile.uid, 9, ((User) mUserProfile).mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL);
                    v.setEnabled(false);
                    //noinspection deprecation
                    ((ImageButton) v).setAlpha(80);
                }
                break;
            case R.id.actionGift:
                if (mGiftFragment != null && mGiftFragment.getActivity() != null) {
                    mGiftFragment.sendGift();
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            GiftsActivity.class);
                    startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
                }
                break;
            case R.id.actionChat:
                openChat();
                break;
            default:
                break;
        }
    }

    public void openChat() {
        if (mUserProfile != null) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra(ChatActivity.INTENT_USER_ID, mUserProfile.uid);
            intent.putExtra(ChatActivity.INTENT_USER_NAME, mUserProfile.first_name);
            intent.putExtra(ChatActivity.INTENT_USER_SEX, mUserProfile.sex);
            intent.putExtra(ChatActivity.INTENT_USER_AGE, mUserProfile.age);
            intent.putExtra(ChatActivity.INTENT_USER_CITY, mUserProfile.city_name);
            intent.putExtra(ChatActivity.INTENT_PROFILE_INVOKE, true);
            intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, ProfileFragment.this.getClass().getSimpleName());
            getActivity().startActivityForResult(intent, ChatActivity.INTENT_CHAT_REQUEST);
        }
    }

    public static ProfileFragment newInstance(int id, int type) {
        ProfileFragment fragment = new ProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }

    public static ProfileFragment newInstance(int id, int type, String startBodyPageClassName) {
        ProfileFragment fragment = new ProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        args.putString(ARG_TAG_INIT_BODY_PAGE, startBodyPageClassName);
        fragment.setArguments(args);

        return fragment;
    }

    public static ProfileFragment newInstance(int id, int type, String initBodyClassName, String initHeaderClassName) {
        ProfileFragment fragment = new ProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        args.putString(ARG_TAG_INIT_BODY_PAGE, initBodyClassName);
        args.putString(ARG_TAG_INIT_HEADER_PAGE, initHeaderClassName);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            int index = mBodyPagerAdapter.getFragmentIndexByClassName(VipBuyFragment.class.getName());
            mBodyPager.setCurrentItem(index);
            buttonView.setChecked(false);
        }
    }

    public class ProfilePageAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> mFragmentsClasses = new ArrayList<String>();
        private ArrayList<String> mFragmentsTitles = new ArrayList<String>();
        private HashMap<Integer, Fragment> mFragmentCache = new HashMap<Integer, Fragment>();

        public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses) {
            super(fm);
            mFragmentsClasses = fragmentsClasses;
        }

        public ProfilePageAdapter(FragmentManager fm, ArrayList<String> fragmentsClasses, ArrayList<String> fragmentTitles) {
            super(fm);
            mFragmentsClasses = fragmentsClasses;
            mFragmentsTitles = fragmentTitles;
        }

        public int getFragmentIndexByClassName(String className) {
            for (int i = 0; i < mFragmentsClasses.size(); i++) {
                if (mFragmentsClasses.get(i).equals(className)) {
                    return i;
                }
            }
            return -1;
        }

        public String getClassNameByFragmentIndex(int i) {
            if (mFragmentsClasses.isEmpty()) {
                return "";
            }
            return mFragmentsClasses.get(i);
        }

        @Override
        public int getCount() {
            return mFragmentsClasses.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (!mFragmentsTitles.isEmpty())
                return mFragmentsTitles.get(position);

            return super.getPageTitle(position);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (mFragmentCache.containsKey(position)) {
                return mFragmentCache.get(position);
            }
            try {
                String fragmentClassName = mFragmentsClasses.get(position);

                //create fragments
                if (fragmentClassName.equals(HeaderMainFragment.class.getName())) {
                    fragment = HeaderMainFragment.newInstance(mUserProfile);
                } else if (fragmentClassName.equals(HeaderStatusFragment.class.getName())) {
                    fragment = HeaderStatusFragment.newInstance(mUserProfile);
                } else {
                    Class fragmentClass = Class.forName(fragmentClassName);
                    if (fragmentClassName.equals(ProfileBlackListControlFragment.class.getName())) {
                        fragment = ProfileBlackListControlFragment.newInstance(mUserProfile.uid, mUserProfile.inBlackList);
                    } else {
                        fragment = (Fragment) fragmentClass.newInstance();
                    }
                }
                //save variables for setting user data
                if (fragment instanceof HeaderMainFragment) {
                    mHeaderMainFragment = (HeaderMainFragment) fragment;
                } else if (fragment instanceof HeaderStatusFragment) {
                    mHeaderStatusFragment = (HeaderStatusFragment) fragment;
                } else if (fragment instanceof UserPhotoFragment) {
                    mUserPhotoFragment = (UserPhotoFragment) fragment;
                } else if (fragment instanceof UserFormFragment) {
                    mUserFormFragment = (UserFormFragment) fragment;
                } else if (fragment instanceof GiftsFragment) {
                    mGiftFragment = (GiftsFragment) fragment;
                }
                setProfile(mUserProfile);
            } catch (Exception ex) {
                Debug.error(ex);
            }
            mFragmentCache.put(position, fragment);
            return fragment;
        }
    }

    @Override
    public void clearContent() {
        mHeaderPager.setCurrentItem(0);
        mTitle.setText(Static.EMPTY);
        mOnline.setVisibility(View.INVISIBLE);
        mLoaderView.setVisibility(View.VISIBLE);
        if (mHeaderMainFragment != null) mHeaderMainFragment.clearContent();
        if (mHeaderStatusFragment != null) mHeaderStatusFragment.clearContent();
        if (mUserPhotoFragment != null) mUserPhotoFragment.clearContent();
        if (mUserFormFragment != null) mUserFormFragment.clearContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                Bundle extras = data.getExtras();
                final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
                final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
                final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);

                if (mUserProfile != null) {
                    final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
                    registerRequest(sendGift);
                    sendGift.giftId = id;
                    sendGift.userId = mUserProfile.uid;
                    final FeedGift sendedGift = new FeedGift();
                    sendedGift.gift = new Gift(sendGift.giftId, Gift.PROFILE_NEW, url, 0);
                    sendGift.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) throws NullPointerException {
                            SendGiftAnswer answer = SendGiftAnswer.parse(response);
                            CacheProfile.power = answer.power;
                            CacheProfile.money = answer.money;

                            ArrayList<Gift> gifts = new ArrayList<Gift>();
                            gifts.add(sendedGift.gift);
                            gifts.addAll(mUserProfile.gifts);

                            mUserProfile.gifts = gifts;
                        }

                        @Override
                        public void fail(int codeError, final ApiResponse response)
                                throws NullPointerException {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (response.code == ApiResponse.PAYMENT) {
                                        Intent intent = new Intent(getActivity()
                                                .getApplicationContext(), BuyingActivity.class);
                                        intent.putExtra(BuyingActivity.INTENT_USER_COINS, price
                                                - CacheProfile.money);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }).exec();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static class HeaderMainFragment extends BaseFragment {
        private static final String ARG_TAG_AVATAR = "avatar";
        private static final String ARG_TAG_NAME = "name";
        private static final String ARG_TAG_CITY = "city";
        private static final String ARG_TAG_BACKGROUND = "background";

        private ImageViewRemote mAvatarView;
        private Photo mAvatarVal;
        private TextView mNameView;
        private String mNameVal;
        private TextView mCityView;
        private String mCityVal;
        private ImageView mBackgroundView;
        private int mBackgroundVal;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            restoreState();

            View root = inflater.inflate(R.layout.fragment_profile_header_main, null);
            mAvatarView = (ImageViewRemote) root.findViewById(R.id.ivUserAvatar);
            mNameView = (TextView) root.findViewById(R.id.tvName);
            mCityView = (TextView) root.findViewById(R.id.tvCity);
            mBackgroundView = (ImageView) root.findViewById(R.id.ivProfileBackground);

            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshViews();
        }

        public void setProfile(Profile profile) {
            if (profile != null) {
                initState(profile);
                saveState(this, profile);
            }
            refreshViews();
        }

        private void refreshViews() {
            updateUI(new Runnable() {
                @Override
                public void run() {
                    mAvatarView.setPhoto(mAvatarVal);
                    mNameView.setText(mNameVal);
                    mCityView.setText(mCityVal);
                    mBackgroundView.setImageResource(ProfileBackgrounds.getBackgroundResource(getActivity().getApplicationContext(), mBackgroundVal));
                }
            });
        }

        private void restoreState() {
            if (getArguments() != null) {
                mAvatarVal = getArguments().getParcelable(ARG_TAG_AVATAR);
                mNameVal = getArguments().getString(ARG_TAG_NAME);
                mCityVal = getArguments().getString(ARG_TAG_CITY);
                mBackgroundVal = getArguments().getInt(ARG_TAG_BACKGROUND);
            }
        }

        private void initState(Profile profile) {
            mAvatarVal = profile.photo;
            mNameVal = profile.getNameAndAge();
            mCityVal = profile.city_name;
            mBackgroundVal = profile.background;
        }

        private static void saveState(Fragment fragment, Profile profile) {
            if (!fragment.isVisible()) {
                if (fragment.getArguments() == null) {
                    Bundle args = new Bundle();
                    fragment.setArguments(args);
                }
                fragment.getArguments().putParcelable(ARG_TAG_AVATAR, profile.photo);
                fragment.getArguments().putString(ARG_TAG_NAME, profile.getNameAndAge());
                fragment.getArguments().putString(ARG_TAG_CITY, profile.city_name);
                fragment.getArguments().putInt(ARG_TAG_BACKGROUND, profile.background);
            }
        }

        public static Fragment newInstance(Profile profile) {
            HeaderMainFragment fragment = new HeaderMainFragment();
            if (profile == null) return fragment;
            saveState(fragment, profile);
            return fragment;
        }

        @Override
        public void clearContent() {
            mAvatarView.setPhoto(null);
            mNameView.setText(Static.EMPTY);
            mCityView.setText(Static.EMPTY);
        }

        @Override
        public boolean isTrackable() {
            return false;
        }
    }

    public static class HeaderStatusFragment extends BaseFragment {
        private static final String ARG_TAG_STATUS = "status";

        private TextView mStatusView;
        private String mStatusVal;

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            restoreState();

            //init views
            View root = inflater.inflate(R.layout.fragment_profile_header_status, null);
            mStatusView = (TextView) root.findViewById(R.id.tvStatus);
            return root;
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshViews();
        }

        public void setProfile(Profile profile) {
            if (profile != null) {
                initState(profile);
                saveState(this, profile);
            }
            refreshViews();
        }

        private void refreshViews() {
            updateUI(new Runnable() {
                @Override
                public void run() {
                    mStatusView.setText(mStatusVal);
                }
            });
        }

        private void restoreState() {
            if (getArguments() != null) {
                mStatusVal = getArguments().getString(ARG_TAG_STATUS);
            }
        }

        private void initState(Profile profile) {
            mStatusVal = profile.status;
        }

        private static void saveState(Fragment fragment, Profile profile) {
            if (!fragment.isVisible()) {
                Bundle args = new Bundle();
                if (fragment.getArguments() == null) {
                    fragment.setArguments(args);
                }
                fragment.getArguments().putString(ARG_TAG_STATUS, profile.status);
            }
        }

        public static Fragment newInstance(Profile profile) {
            HeaderStatusFragment fragment = new HeaderStatusFragment();
            if (profile == null) return fragment;
            saveState(fragment, profile);
            return fragment;
        }

        @Override
        public void clearContent() {
            mStatusView.setText(Static.EMPTY);
        }
    }

    RateController.OnRateControllerListener mRateControllerListener = new RateController.OnRateControllerListener() {
        @Override
        public void successRate() {
            //TODO:
//            mUserProfile.rated = true;
//            mUserDelight.setEnabled(!mUser.rated);
//            mUserMutual.setEnabled(!mUser.rated);
        }

        @Override
        public void failRate() {
            //TODO:
//            mUserUser.rated = false;
//            mUserDelight.setEnabled(!mUser.rated);
//            mUserMutual.setEnabled(!mUser.rated);
        }
    };

    private class FadeAnimationListener implements Animation.AnimationListener {
        private boolean mShouldHide;

        public FadeAnimationListener(boolean shouldHide) {
            mShouldHide = shouldHide;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (mShouldHide) {
                mActionsControl.setVisibility(View.GONE);
            } else {
                mActionsControl.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
