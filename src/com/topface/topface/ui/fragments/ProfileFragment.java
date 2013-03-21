package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.ProfilePageAdapter;
import com.topface.topface.ui.edit.EditProfileActivity;
import com.topface.topface.ui.profile.*;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.http.ProfileBackgrounds;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {
    public final static int TYPE_MY_PROFILE = 1;
    public final static int TYPE_USER_PROFILE = 2;
    private static final String ARG_TAG_PROFILE_TYPE = "profile_type";
    private static final String ARG_TAG_PROFILE_ID = "profile_id";
    private static final String ARG_TAG_INIT_BODY_PAGE = "profile_start_body_class";
    private static final String ARG_TAG_INIT_HEADER_PAGE = "profile_start_header_class";
    public static final String ARG_FEED_ITEM_ID = "item_id";
    public static final String DEFAULT_ACTIVATED_COLOR = "#AAAAAA";
    public static final String DEFAULT_NON_ACTIVATED = "#FFFFFF";

    ArrayList<String> BODY_PAGES_TITLES = new ArrayList<String>();
    ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<String>();
    ArrayList<String> HEADER_PAGES_CLASS_NAMES = new ArrayList<String>();

    private HeaderMainFragment mHeaderMainFragment;
    private HeaderStatusFragment mHeaderStatusFragment;
    private UserPhotoFragment mUserPhotoFragment;
    private UserFormFragment mUserFormFragment;

    private Profile mUserProfile = null;
    private static int mProfileType;
    private int mProfileId;

    private TextView mTitle;
    private View mLoaderView;
    private RateController mRateController;
    //    protected NavigationBarController mNavBarController;
    private RelativeLayout mLockScreen;
    private RetryView mRetryBtn;
    private ViewPager mBodyPager;
    private ProfilePageAdapter mBodyPagerAdapter;
    private ViewPager mHeaderPager;
    private ProfilePageAdapter mHeaderPagerAdapter;
    private GiftsFragment mGiftFragment;

    private String mBodyStartPageClassName;
    private String mHeaderStartPageClassName;
    private int mStartBodyPage = 0;
    private int mStartHeaderPage = 0;
    private BroadcastReceiver mUpdateBlackListState;
    private BroadcastReceiver mUpdateProfileReceiver;

    private TabPageIndicator mTabIndicator;
    private ActionBar mActionBar;
    private LinearLayout mUserActions;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //init views
        View root = inflater.inflate(R.layout.ac_profile, null);

        mActionBar = getActionBar(root);

        mLoaderView = root.findViewById(R.id.llvProfileLoading);
        mRateController = new RateController(getActivity());

        if (getArguments().getInt(ARG_FEED_ITEM_ID, -1) != -1) {
            Intent intent = new Intent(ChatFragment.MAKE_ITEM_READ);
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, getArguments().getInt(ARG_FEED_ITEM_ID, -1));
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }

        restoreState();
        mUserActions = (LinearLayout) root.findViewById(R.id.mUserActions);
        mUserActions.findViewById(R.id.acGift).setOnClickListener(this);
        mUserActions.findViewById(R.id.acSympathy).setOnClickListener(this);
        mUserActions.findViewById(R.id.acDelight).setOnClickListener(this);
        mUserActions.findViewById(R.id.acChat).setOnClickListener(this);
        mUserActions.findViewById(R.id.acBlock).setOnClickListener(this);
//
//        mNavBarController = new NavigationBarController((ViewGroup) root.findViewById(R.id.loNavigationBar));
        if (mProfileType == TYPE_USER_PROFILE) {
            mActionBar.showBackButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        } else {
            mActionBar.showHomeButton((NavigationActivity) getActivity());
        }

        mTitle = (TextView) root.findViewById(R.id.tvNavigationTitle);

        initHeaderPages(root);

        initBodyPages(root);

        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryBtn = new RetryView(getActivity().getApplicationContext());
        mRetryBtn.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile();
                mLockScreen.setVisibility(View.GONE);
            }
        });
        mLockScreen.addView(mRetryBtn);

        if (mProfileType == TYPE_MY_PROFILE) {
            mTitle.setText(R.string.profile_header_title);
            mActionBar.showEditButton(this);
        } else if (mProfileType == TYPE_USER_PROFILE) {

            mActionBar.showUserActionsButton(new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {

                                                     double density = getResources().getDisplayMetrics().density;
                                                     TranslateAnimation ta = new TranslateAnimation(0, 0, 0, mUserActions.getHeight() + mActionBar.getHeight() + (int) (5 * density));
                                                     ta.setDuration(500);
                                                     ta.setFillAfter(true);
                                                     ta.setAnimationListener(new Animation.AnimationListener() {
                                                         @Override
                                                         public void onAnimationStart(Animation animation) {
                                                             mActionBar.disableActionsButton(true);
                                                         }

                                                         @Override
                                                         public void onAnimationEnd(Animation animation) {
                                                             mUserActions.clearAnimation();
                                                             RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                             params.setMargins(0, 5, 5, 0);
                                                             params.addRule(RelativeLayout.BELOW, R.id.loNavigationBar);
                                                             params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                                             mUserActions.setLayoutParams(params);
                                                             mActionBar.disableActionsButton(false);
                                                         }

                                                         @Override
                                                         public void onAnimationRepeat(Animation animation) {

                                                         }
                                                     });
                                                     mUserActions.startAnimation(ta);
                                                 }
                                             }, new View.OnClickListener() {
                                                 @Override
                                                 public void onClick(View view) {
                                                     double density = getResources().getDisplayMetrics().density;
                                                     TranslateAnimation ta = new TranslateAnimation(0, 0, 0, (int) (-270 * density));
                                                     ta.setDuration(500);
                                                     ta.setFillAfter(true);
                                                     ta.setAnimationListener(new Animation.AnimationListener() {
                                                         @Override
                                                         public void onAnimationStart(Animation animation) {
                                                             mActionBar.disableActionsButton(true);
                                                         }

                                                         @Override
                                                         public void onAnimationEnd(Animation animation) {

                                                             mUserActions.clearAnimation();
                                                             RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                             params.setMargins(0, 5, 5, 0);
                                                             params.addRule(RelativeLayout.ABOVE, R.id.loNavigationBar);
                                                             params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                                                             mUserActions.setLayoutParams(params);
                                                             mActionBar.disableActionsButton(false);
                                                         }

                                                         @Override
                                                         public void onAnimationRepeat(Animation animation) {
                                                         }
                                                     });
                                                     mUserActions.startAnimation(ta);
                                                 }
                                             }
            );
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

        mUpdateBlackListState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mUserProfile != null) {
                    mUserProfile.inBlackList = intent.getBooleanExtra(ProfileBlackListControlFragment.BLACK_LIST_STATUS, false);
                }
            }
        };
        mUpdateProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mProfileType == TYPE_MY_PROFILE) {
                    mUserProfile = CacheProfile.getProfile();
                    setProfile(mUserProfile);
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateBlackListState, new IntentFilter(ProfileBlackListControlFragment.UPDATE_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateProfileReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        setProfile(mUserProfile);

        mActionBar.refreshNotificators();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateBlackListState);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateProfileReceiver);

        //Вручную прокидываем событие onPause() в ViewPager, т.к. на onPause() мы отписываемся от событий
        if (mBodyPagerAdapter != null) {
            for (Fragment fragment : mBodyPagerAdapter.getFragmentCache().values()) {
                if (fragment != null) {
                    fragment.onPause();
                }
            }
        }

        if (mHeaderPagerAdapter != null) {
            for (Fragment fragment : mHeaderPagerAdapter.getFragmentCache().values()) {
                if (fragment != null) {
                    fragment.onPause();
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mTabIndicator != null) {
            mTabIndicator.setOnPageChangeListener(null);
            mTabIndicator.removeAllViews();
            mTabIndicator = null;
        }

        mBodyPager = null;
        mHeaderPager = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        UserRequest userRequest = new UserRequest(mProfileId, getActivity());
        registerRequest(userRequest);
        userRequest.callback(new DataApiHandler<User>() {

            @Override
            protected void success(User data, ApiResponse response) {
                mUserProfile = data;
                mRateController.setOnRateControllerListener(mRateControllerListener);
                //set info into views for user
                mTitle.setText(data.getNameAndAge());

                setProfile(data);
                if (mHeaderMainFragment != null) {
                    mHeaderMainFragment.setOnline(data.online);
                }
                mLoaderView.setVisibility(View.INVISIBLE);
            }

            @Override
            protected User parseResponse(ApiResponse response) {
                return User.parse(mProfileId, response);
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                if (mRetryBtn != null && isAdded()) {
                    mLoaderView.setVisibility(View.GONE);
                    mLockScreen.setVisibility(View.VISIBLE);
                    mRetryBtn.setErrorMsg(getString(R.string.general_profile_error));
                    mRetryBtn.showOnlyMessage(false);
                }
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
        headerPager.setSaveEnabled(false);
        mHeaderPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(),
                HEADER_PAGES_CLASS_NAMES, mProfileUpdater);
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
            addBodyPage(ProfileBlackListControlFragment.class.getName(), getResources().getString(R.string.profile_actions));
        }

        ViewPager bodyPager = (ViewPager) root.findViewById(R.id.vpFragments);
        mBodyPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(), BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES, mProfileUpdater);
        bodyPager.setAdapter(mBodyPagerAdapter);
        //Tabs for Body
        mTabIndicator = (TabPageIndicator) root.findViewById(R.id.tpiTabs);

        mTabIndicator.setViewPager(bodyPager);

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
    public void onClick(final View v) {

        switch (v.getId()) {
            case R.id.btnEdit:
                startActivity(new Intent(getActivity().getApplicationContext(), EditProfileActivity.class));
                break;
            case R.id.acDelight:
                if (v.isEnabled()) {
                    v.setSelected(true);
                    TextView view = (TextView) v;
                    view.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));

                    v.setEnabled(false);
                    mRateController.onRate(mUserProfile.uid, 10, ((User) mUserProfile).mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL, new RateController.OnRateListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateCompleted() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.sympathy_sended, 1500).show();

                            }
                        }

                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateFailed() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.general_server_error, 1500).show();
                                v.setEnabled(true);
                                v.setSelected(false);
                                TextView view = (TextView) v;
                                view.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                            }
                        }
                    });

                    //noinspection deprecation
//                    ((TextView) v).setAlpha(80);
                }
                break;
            case R.id.acSympathy:
                if (v.isEnabled()) {
                    v.setSelected(true);
                    TextView view = (TextView) v;
                    view.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                    v.setEnabled(false);
                    mRateController.onRate(mUserProfile.uid, 9, ((User) mUserProfile).mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL, new RateController.OnRateListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateCompleted() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.sympathy_sended, 1500).show();

                            }
                        }

                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateFailed() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(getActivity(), R.string.general_server_error, 1500).show();
                                v.setEnabled(true);
                                v.setSelected(false);
                                TextView view = (TextView) v;
                                view.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                            }
                        }
                    });


                    //noinspection deprecation
//                    ((TextView) v).setAlpha(80);
                }
                break;
            case R.id.acGift:
                if (mGiftFragment != null && mGiftFragment.getActivity() != null) {
                    mGiftFragment.sendGift();
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            GiftsActivity.class);
                    startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
                }
                break;
            case R.id.acChat:
                openChat();
                break;
            case R.id.acBlock:
                if (CacheProfile.premium) {
                    BlackListAddRequest blaRequest = new BlackListAddRequest(mUserProfile.uid, getActivity());
                    blaRequest.callback(new VipApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            super.success(response);
                            v.setEnabled(false);
                            TextView view = (TextView) v;
                            view.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                        }
                    }
                    ).exec();
                } else {
                    Intent intent = new Intent(getActivity(), ContainerActivity.class);
                    intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    public void openChat() {
        if (mUserProfile != null) {
            Intent intent = new Intent(getActivity(), ContainerActivity.class);
            intent.putExtra(ChatFragment.INTENT_USER_ID, mUserProfile.uid);
            intent.putExtra(ChatFragment.INTENT_USER_NAME, mUserProfile.first_name);
            intent.putExtra(ChatFragment.INTENT_USER_SEX, mUserProfile.sex);
            intent.putExtra(ChatFragment.INTENT_USER_AGE, mUserProfile.age);
            intent.putExtra(ChatFragment.INTENT_USER_CITY, mUserProfile.city == null ? "" : mUserProfile.city.name);
            intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
            getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
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

    //Этот метод добавлен для того, чтобы можно было отметить элемент ленты прочитанным
    public static ProfileFragment newInstance(int id, int type, int itemId) {
        ProfileFragment fragment = new ProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        args.putInt(ARG_FEED_ITEM_ID, itemId);
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
    public void clearContent() {
        if (mHeaderPager != null) {
            mHeaderPager.setCurrentItem(0);
        }
        if (mTitle != null && mLoaderView != null) {
            mTitle.setText(Static.EMPTY);
            mLoaderView.setVisibility(View.VISIBLE);
        }
        if (mHeaderMainFragment != null) mHeaderMainFragment.clearContent();
        if (mHeaderStatusFragment != null) mHeaderStatusFragment.clearContent();
        if (mUserPhotoFragment != null) mUserPhotoFragment.clearContent();
        if (mUserFormFragment != null) mUserFormFragment.clearContent();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            resultToNestedFragments(requestCode, resultCode, data);
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
                    sendGift.callback(new DataApiHandler<SendGiftAnswer>() {

                        @Override
                        protected void success(SendGiftAnswer answer, ApiResponse response) {
                            CacheProfile.likes = answer.likes;
                            CacheProfile.money = answer.money;

                            ArrayList<Gift> gifts = new ArrayList<Gift>();
                            gifts.add(sendedGift.gift);
                            gifts.addAll(mUserProfile.gifts);

                            mUserProfile.gifts = gifts;
                        }

                        @Override
                        protected SendGiftAnswer parseResponse(ApiResponse response) {
                            return SendGiftAnswer.parse(response);
                        }

                        @Override
                        public void fail(int codeError, final ApiResponse response) throws NullPointerException {
                            if (response.code == ApiResponse.PAYMENT) {
                                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                                intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                                intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                                intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                                startActivity(intent);
                            }
                        }
                    }).exec();
                }
            }
        }
    }

    public void resultToNestedFragments(int requestCode, int resultCode, Intent data) {
        HashMap<Integer, Fragment> mBodyFragments = mBodyPagerAdapter.getFragmentCache();
        for (Fragment fragment : mBodyFragments.values()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
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
        private ImageView mOnline;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);

            restoreState();

            View root = inflater.inflate(R.layout.fragment_profile_header_main, null);
            mAvatarView = (ImageViewRemote) root.findViewById(R.id.ivUserAvatar);
            mNameView = (TextView) root.findViewById(R.id.tvName);
            mCityView = (TextView) root.findViewById(R.id.tvCity);
            mBackgroundView = (ImageView) root.findViewById(R.id.ivProfileBackground);
            mOnline = (ImageView) root.findViewById(R.id.ivOnline);

            return root;
        }

        public void setOnline(boolean online) {
            mOnline.setVisibility(online ? View.VISIBLE : View.GONE);
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
                    setCity(mCityVal);
                    mBackgroundView.setImageResource(ProfileBackgrounds.getBackgroundResource(getActivity().getApplicationContext(), mBackgroundVal));
                }
            });
        }

        private void setCity(String city) {
            mCityView.setText(city);
            mCityView.setVisibility(
                    TextUtils.isEmpty(city) ?
                            View.INVISIBLE :
                            View.VISIBLE
            );
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
            if (profile != null) {
                mAvatarVal = profile.photo;
                mNameVal = profile.getNameAndAge();
                if (profile.city != null) {
                    mCityVal = profile.city.name;
                }
                mBackgroundVal = profile.background;
            }
        }

        private static void saveState(Fragment fragment, Profile profile) {
            if (!fragment.isVisible()) {
                if (fragment.getArguments() == null && !fragment.isAdded()) {
                    Bundle args = new Bundle();
                    fragment.setArguments(args);
                }
                fragment.getArguments().putParcelable(ARG_TAG_AVATAR, profile.photo);
                fragment.getArguments().putString(ARG_TAG_NAME, profile.getNameAndAge());
                fragment.getArguments().putString(ARG_TAG_CITY, profile.city.name);
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
            setCity(Static.EMPTY);
        }

        @Override
        public boolean isTrackable() {
            return false;
        }

        @Override
        public void onPause() {
            super.onPause();
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

        @Override
        public void onPause() {
            super.onPause();
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


    ProfileUpdater mProfileUpdater = new ProfileUpdater() {
        @Override
        public void update() {
            setProfile(getProfile());
        }

        public void bindFragment(Fragment fragment) {
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
        }

        public Profile getProfile() {
            return mUserProfile;
        }
    };

    public interface ProfileUpdater {
        void update();

        void bindFragment(Fragment fragment);

        Profile getProfile();
    }
}
