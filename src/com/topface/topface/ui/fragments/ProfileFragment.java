package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.data.User;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.ProfilePageAdapter;
import com.topface.topface.ui.profile.*;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
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
    public static final String INTENT_UID = "intent_profile_uid";
    public static final String INTENT_TYPE = "intent_profile_type";
    public static final String INTENT_ITEM_ID = "intent_profile_item_id";

    ArrayList<String> BODY_PAGES_TITLES = new ArrayList<String>();
    ArrayList<String> BODY_PAGES_CLASS_NAMES = new ArrayList<String>();
    ArrayList<String> HEADER_PAGES_CLASS_NAMES = new ArrayList<String>();

    private HeaderMainFragment mHeaderMainFragment;
    private HeaderStatusFragment mHeaderStatusFragment;
    private UserPhotoFragment mUserPhotoFragment;
    private UserFormFragment mUserFormFragment;

    private Profile mUserProfile = null;
    public int mProfileType;
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
    private BroadcastReceiver mUpdateProfileReceiver;

    private TabPageIndicator mTabIndicator;
    private ActionBar mActionBar;
    private LinearLayout mUserActions;
    private RelativeLayout bmBtn;
    private TextView mBookmarkAction;

    private int mUserActionsPanelHeight;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //init views
        View root = inflater.inflate(R.layout.ac_profile, null);

        mActionBar = getActionBar(root);

        mLoaderView = root.findViewById(R.id.llvProfileLoading);
        final FragmentActivity activity = getActivity();
        mRateController = new RateController(activity);

        String itemId = getArguments().getString(ARG_FEED_ITEM_ID);
        if (itemId != null) {
            Intent intent = new Intent(ChatFragment.MAKE_ITEM_READ);
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, itemId);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }

        restoreState();
        mUserActions = (LinearLayout) root.findViewById(R.id.mUserActions);
        mUserActions.findViewById(R.id.acGift).setOnClickListener(this);
        mUserActions.findViewById(R.id.acSympathy).setOnClickListener(this);
        mUserActions.findViewById(R.id.acDelight).setOnClickListener(this);
        mUserActions.findViewById(R.id.acChat).setOnClickListener(this);
        mUserActions.findViewById(R.id.acBlock).setOnClickListener(this);
        bmBtn = (RelativeLayout) mUserActions.findViewById(R.id.acBookmark);
        mBookmarkAction = (TextView) mUserActions.findViewById(R.id.favTV);
        bmBtn.setOnClickListener(this);
        if (mProfileType == TYPE_USER_PROFILE) {
            mActionBar.showBackButton(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (activity != null) {
                        activity.onBackPressed();
                    }
                }
            });
        } else if (activity instanceof NavigationActivity) {
            mActionBar.showHomeButton((NavigationActivity) activity);
        }
        mUserActions.setVisibility(View.GONE);

        mTitle = (TextView) root.findViewById(R.id.tvNavigationTitle);

        initHeaderPages(root);

        initBodyPages(root);

        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryBtn = new RetryView(activity.getApplicationContext());
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
            mActionBar.showUserActionsButton(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            initActionsPanelHeight();
                            TranslateAnimation ta = new TranslateAnimation(0, 0, - (mUserActions.getHeight() + mUserActionsPanelHeight), 0);
                            ta.setDuration(500);
                            ta.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    mActionBar.disableActionsButton(true);
                                    mUserActions.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    mUserActions.clearAnimation();
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
                            initActionsPanelHeight();
                            TranslateAnimation ta = new TranslateAnimation(0, 0, 0, - (mUserActions.getHeight() + mUserActionsPanelHeight));
                            ta.setDuration(500);
                            ta.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    mActionBar.disableActionsButton(true);
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    mUserActions.clearAnimation();
                                    mActionBar.disableActionsButton(false);
                                    mUserActions.setVisibility(View.GONE);
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

    private void initActionsPanelHeight() {
        if(mUserActionsPanelHeight == 0) {
            int actualHeight = mActionBar.getHeight();
            double density = getResources().getDisplayMetrics().density;
            mUserActionsPanelHeight = actualHeight == 0 ? actualHeight : (int) (270 * density);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mProfileType == TYPE_MY_PROFILE) {
            mUserProfile = CacheProfile.getProfile();
        } else {
            if (mUserProfile == null) getUserProfile();
        }

        mUpdateProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mProfileType == TYPE_MY_PROFILE) {
                    mUserProfile = CacheProfile.getProfile();
                    setProfile(mUserProfile);
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateProfileReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        setProfile(mUserProfile);

        mActionBar.refreshNotificators();
    }

    @Override
    public void onPause() {
        super.onPause();
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
                if (mUserProfile == null) {
                    showRetryBtn();
                } else if (data.banned) {
                    showForBanned();
                } else if (data.deleted) {
                    showForDeleted();
                } else {
                    if (data.bookmarked) {
                        mBookmarkAction.setText(App.getContext().getString(R.string.general_bookmarks_delete));
                    } else {
                        mBookmarkAction.setText(App.getContext().getString(R.string.general_bookmarks_add));
                    }
                    mRateController.setOnRateControllerListener(mRateControllerListener);
                    //set info into views for user
                    mTitle.setText(mUserProfile.getNameAndAge());

                    setProfile(data);
                    if (mHeaderMainFragment != null) {
                        mHeaderMainFragment.setOnline(data.online);
                    }
                    mLoaderView.setVisibility(View.INVISIBLE);

                    if (mProfileType == TYPE_USER_PROFILE) {
                        String status = mUserProfile.getStatus();
                        if (status == null || TextUtils.isEmpty(status)) {
                            mHeaderPagerAdapter.removeItem(HeaderStatusFragment.class.getName());
                        }
                    }
                }
            }

            @Override
            protected User parseResponse(ApiResponse response) {
                return User.parse(mProfileId, response);
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                showRetryBtn();
            }
        }).exec();
    }

    private void showForBanned() {
        showLockWithText(getString(R.string.user_baned));
    }

    private void showForDeleted() {
        showLockWithText(getString(R.string.user_is_deleted));
    }

    private void showLockWithText(String text) {
        if (mRetryBtn != null && isAdded()) {
            mLoaderView.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.VISIBLE);
            mRetryBtn.setErrorMsg(text);
            mRetryBtn.showOnlyMessage(true);
            mActionBar.hideUserActionButton();
        }
    }

    private void showRetryBtn() {
        if (mRetryBtn != null && isAdded()) {
            mLoaderView.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.VISIBLE);
            mRetryBtn.setErrorMsg(getString(R.string.general_profile_error));
            mRetryBtn.showOnlyMessage(false);
        }
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

        mHeaderPager = (ViewPager) root.findViewById(R.id.vpHeaderFragments);
        //Мы отключаем сохранеие state у фрагментов, т.к. мы устанавливаем данные в методе getItem() адаптера,
        //что приводит к пустым фрагментам. Поэтому мы не пытаемся сохранять и восстанавливать состояние фрагмента
        mHeaderPager.setSaveEnabled(false);
        mHeaderPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(),
                HEADER_PAGES_CLASS_NAMES, mProfileUpdater);
        mHeaderPager.setAdapter(mHeaderPagerAdapter);
        //Tabs for header
        CirclePageIndicator circleIndicator = (CirclePageIndicator) root.findViewById(R.id.cpiHeaderTabs);
        circleIndicator.setViewPager(mHeaderPager);
        circleIndicator.setSnap(true);
        mHeaderPagerAdapter.setPageIndicator(circleIndicator);

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
        }

        mBodyPager = (ViewPager) root.findViewById(R.id.vpFragments);
        mBodyPagerAdapter = new ProfilePageAdapter(getChildFragmentManager(), BODY_PAGES_CLASS_NAMES,
                BODY_PAGES_TITLES, mProfileUpdater);
        mBodyPager.setAdapter(mBodyPagerAdapter);
        //Мы отключаем сохранеие state у фрагментов, т.к. мы устанавливаем данные в методе getItem() адаптера,
        //что приводит к пустым фрагментам. Поэтому мы не пытаемся сохранять и восстанавливать состояние фрагмента
        mBodyPager.setSaveEnabled(false);
        //Tabs for Body
        mTabIndicator = (TabPageIndicator) root.findViewById(R.id.tpiTabs);
        mTabIndicator.setViewPager(mBodyPager);

        mBodyPagerAdapter.setPageIndicator(mTabIndicator);
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
                startActivity(ContainerActivity.getNewIntent(ContainerActivity.INTENT_SETTINGS_FRAGMENT));
                break;
            case R.id.acDelight:
                if (v.isEnabled()) {
                    v.setSelected(true);
                    TextView textView = (TextView) v.findViewById(R.id.delTV);
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.delPrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.delIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);

                    textView.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                    v.findViewById(R.id.delPrBar).setVisibility(View.VISIBLE);
                    v.setEnabled(false);
                    mRateController.onRate(mUserProfile.uid, 10, ((User) mUserProfile).mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL, new RateController.OnRateListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateCompleted() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(App.getContext(), R.string.sympathy_sended, 1500).show();
                                loader.setVisibility(View.INVISIBLE);
                                icon.setVisibility(View.VISIBLE);

                            }
                        }

                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateFailed() {
                            if (v != null && getActivity() != null) {
                                loader.setVisibility(View.INVISIBLE);
                                icon.setVisibility(View.VISIBLE);
                                Toast.makeText(App.getContext(), R.string.general_server_error, 1500).show();
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
                    TextView textView = (TextView) v.findViewById(R.id.likeTV);
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.likePrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.likeIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);
                    textView.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                    v.setEnabled(false);
                    mRateController.onRate(mUserProfile.uid, 9, ((User) mUserProfile).mutual ? RateRequest.DEFAULT_MUTUAL : RateRequest.DEFAULT_NO_MUTUAL, new RateController.OnRateListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateCompleted() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(App.getContext(), R.string.sympathy_sended, 1500).show();
                                loader.setVisibility(View.INVISIBLE);
                                icon.setVisibility(View.VISIBLE);
                            }
                        }

                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onRateFailed() {
                            if (v != null && getActivity() != null) {
                                Toast.makeText(App.getContext(), R.string.general_server_error, 1500).show();
                                loader.setVisibility(View.INVISIBLE);
                                icon.setVisibility(View.VISIBLE);
                                v.setEnabled(true);
                                v.setSelected(false);
                                if (v instanceof TextView) {
                                    TextView view = (TextView) v;
                                    view.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                                }
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
                    if (mUserProfile.uid > 0) {
                        final TextView textView = (TextView) v.findViewById(R.id.blockTV);
                        final ProgressBar loader = (ProgressBar) v.findViewById(R.id.blockPrBar);
                        final ImageView icon = (ImageView) v.findViewById(R.id.blockIcon);

                        loader.setVisibility(View.VISIBLE);
                        icon.setVisibility(View.GONE);
                        BlackListAddRequest blackListAddRequest = new BlackListAddRequest(mUserProfile.uid, getActivity());
                        blackListAddRequest.callback(new VipApiHandler() {
                            @Override
                            public void success(ApiResponse response) {
                                super.success(response);
                                if (isAdded()) {
                                    v.setEnabled(false);
                                    loader.setVisibility(View.INVISIBLE);
                                    icon.setVisibility(View.VISIBLE);
                                    textView.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                                }
                            }

                            @Override
                            public void fail(int codeError, ApiResponse response) {
                                super.fail(codeError, response);
                                if (isAdded()) {
                                    loader.setVisibility(View.INVISIBLE);
                                    icon.setVisibility(View.VISIBLE);
                                }
                            }
                        }).exec();
                    }
                } else {
                    Intent intent = new Intent(getActivity(), ContainerActivity.class);
                    intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                    startActivity(intent);
                }
                break;
            case R.id.acBookmark:
                final TextView textView = (TextView) v.findViewById(R.id.favTV);
                final ProgressBar loader = (ProgressBar) v.findViewById(R.id.favPrBar);
                final ImageView icon = (ImageView) v.findViewById(R.id.favIcon);

                loader.setVisibility(View.VISIBLE);
                icon.setVisibility(View.GONE);
                ApiRequest request;

                if (mUserProfile instanceof User && ((User) mUserProfile).bookmarked) {
                    request = new BookmarkDeleteRequest(getActivity(), mUserProfile.uid);
                } else {
                    request = new BookmarkAddRequest(getActivity(), mUserProfile.uid);
                }

                request.callback(new SimpleApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        super.success(response);
//                        Toast.makeText(App.getContext(), getString(R.string.general_user_bookmarkadd), 1500).show();
                        if (mUserProfile != null) {
                            textView.setText(App.getContext().getString(((User) mUserProfile).bookmarked ? R.string.general_bookmarks_add : R.string.general_bookmarks_delete));
                            ((User) mUserProfile).bookmarked = !((User) mUserProfile).bookmarked;
                        }

                        loader.setVisibility(View.INVISIBLE);
                        icon.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void always(ApiResponse response) {
                        super.always(response);
                        if (isAdded()) {
                            loader.setVisibility(View.INVISIBLE);
                            icon.setVisibility(View.VISIBLE);
                        }
                    }
                }).exec();
                break;
            default:
                break;
        }
    }

    public void openChat() {
        if (mUserProfile != null) {
            Intent intent = new Intent(getActivity(), ContainerActivity.class);
            intent.putExtra(ChatFragment.INTENT_USER_ID, mUserProfile.uid);
            intent.putExtra(ChatFragment.INTENT_USER_NAME, mUserProfile.first_name == null ?
                    mUserProfile.first_name : Static.EMPTY);
            intent.putExtra(ChatFragment.INTENT_USER_SEX, mUserProfile.sex);
            intent.putExtra(ChatFragment.INTENT_USER_AGE, mUserProfile.age);
            intent.putExtra(ChatFragment.INTENT_USER_CITY, mUserProfile.city == null ? "" : mUserProfile.city.name);
            intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
            getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
        }
    }

    /**
     * @param id   пользователя, которому пренадлежит профиль
     * @param type тип профиля (свой или чужой)
     * @return фрагмент профиля
     */
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
    public static ProfileFragment newInstance(String itemId, int id, int type) {
        ProfileFragment fragment = new ProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        args.putString(ARG_FEED_ITEM_ID, itemId);
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
        }
    }

    public void resultToNestedFragments(int requestCode, int resultCode, Intent data) {
        HashMap<Integer, Fragment> mBodyFragments = mBodyPagerAdapter.getFragmentCache();
        for (Fragment fragment : mBodyFragments.values()) {
            fragment.onActivityResult(requestCode, resultCode, data);
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

        @Override
        public int getProfileType() {
            return mProfileType;
        }
    };

    public interface ProfileUpdater {
        void update();

        void bindFragment(Fragment fragment);

        Profile getProfile();

        int getProfileType();
    }

}
