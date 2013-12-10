package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedGift;
import com.topface.topface.data.Gift;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddManyRequest;
import com.topface.topface.requests.BlackListDeleteManyRequest;
import com.topface.topface.requests.BookmarkAddRequest;
import com.topface.topface.requests.BookmarkDeleteManyRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendGiftRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.ProfilePageAdapter;
import com.topface.topface.ui.dialogs.LeadersDialog;
import com.topface.topface.ui.profile.AddPhotoHelper;
import com.topface.topface.ui.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.profile.ProfileFormFragment;
import com.topface.topface.ui.profile.ProfilePhotoFragment;
import com.topface.topface.ui.profile.UserFormFragment;
import com.topface.topface.ui.profile.UserPhotoFragment;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.UserActions;
import com.topface.topface.utils.Utils;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;

public class ProfileFragment extends BaseFragment implements View.OnClickListener {
    public final static int TYPE_MY_PROFILE = 1;
    public final static int TYPE_USER_PROFILE = 2;
    private static final String ARG_TAG_PROFILE_TYPE = "profile_type";
    private static final String ARG_TAG_PROFILE_ID = "profile_id";
    private static final String ARG_TAG_INIT_BODY_PAGE = "profile_start_body_class";
    private static final String ARG_TAG_INIT_HEADER_PAGE = "profile_start_header_class";
    private static final String ARG_TAG_CALLING_CLASS = "intent_profile_calling_fragment";
    public static final String ARG_FEED_ITEM_ID = "item_id";
    public static final String DEFAULT_ACTIVATED_COLOR = "#AAAAAA";
    public static final String DEFAULT_NON_ACTIVATED = "#FFFFFF";
    public static final String INTENT_UID = "intent_profile_uid";
    public static final String INTENT_TYPE = "intent_profile_type";
    public static final String INTENT_ITEM_ID = "intent_profile_item_id";
    public static final String INTENT_CALLING_FRAGMENT = "intent_profile_calling_fragment";
    public static final String ADD_PHOTO_INTENT = "com.topface.topface.ADD_PHOTO_INTENT";

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
    private String mCallingClass;

    private View mLoaderView;
    private RateController mRateController;
    private RelativeLayout mLockScreen;
    private RetryViewCreator mRetryView;
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
    private LinearLayout mUserActions;
    private RelativeLayout bmBtn;
    private TextView mBookmarkAction;

    private ProgressBar giftsLoader;
    private ImageView giftsIcon;
    private AddPhotoHelper mAddPhotoHelper;
    private BroadcastReceiver addPhotoReceiver;

    private OnGiftReceivedListener giftsReceivedListener = new OnGiftReceivedListener() {
        @Override
        public void onReceived() {
            if (giftsIcon != null) {
                giftsIcon.setVisibility(View.VISIBLE);
            }
            if (giftsLoader != null) {
                giftsLoader.setVisibility(View.INVISIBLE);
            }
        }
    };
    private RelativeLayout mBlocked;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //init views
        final View root = inflater.inflate(R.layout.ac_profile, null);

        mLoaderView = root.findViewById(R.id.llvProfileLoading);
        final FragmentActivity activity = getActivity();
        mRateController = new RateController(activity, SendLikeRequest.Place.FROM_PROFILE);

        String itemId = getArguments().getString(ARG_FEED_ITEM_ID);
        if (itemId != null) {
            Intent intent = new Intent(ChatFragment.MAKE_ITEM_READ);
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, itemId);
            LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
        }

        initUserActions(root);

        bmBtn = (RelativeLayout) mUserActions.findViewById(R.id.acBookmark);
        mBookmarkAction = (TextView) mUserActions.findViewById(R.id.favTV);
        mBlocked = (RelativeLayout) mUserActions.findViewById(R.id.acBlock);

        bmBtn.setOnClickListener(this);

        mUserActions.setVisibility(View.INVISIBLE);

        initHeaderPages(root);

        initBodyPages(root);

        mLockScreen = (RelativeLayout) root.findViewById(R.id.lockScreen);
        mRetryView = RetryViewCreator.createDefaultRetryView(getActivity(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserProfile();
                mLockScreen.setVisibility(View.GONE);
            }
        });
        mLockScreen.addView(mRetryView.getView());

        if (mProfileType == TYPE_MY_PROFILE) {
            setActionBarTitles(R.string.profile_header_title);
        } else if (mProfileType == TYPE_USER_PROFILE) {
            setActionBarTitles(R.string.general_profile);
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
        mAddPhotoHelper = new AddPhotoHelper(this, null);
        mAddPhotoHelper.setOnResultHandler(mHandler);
        addPhotoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mAddPhotoHelper != null) {
                    if (getActivity() != null) {

                        int id = intent.getIntExtra("btn_id", 0);

                        View view = new View(getActivity());
                        view.setId(id);
                        mAddPhotoHelper.getAddPhotoClickListener().onClick(view);
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(addPhotoReceiver, new IntentFilter(ADD_PHOTO_INTENT));

        mHeaderPager.setCurrentItem(mStartHeaderPage);
        mBodyPager.setCurrentItem(mStartBodyPage);
        return root;
    }

    @Override
    protected String getTitle() {
        if (mProfileType == TYPE_MY_PROFILE) {
            return getString(R.string.profile_header_title);
        } else if (mProfileType == TYPE_USER_PROFILE) {
            return getString(R.string.general_profile);
        } else {
            return getString(R.string.general_profile);
        }
    }

    private void animateProfileActions(final boolean isActive, int time) {
        TranslateAnimation ta;
        if (isActive) {
            ta = new TranslateAnimation(0, 0, 0, -mUserActions.getHeight());
        } else {
            ta = new TranslateAnimation(0, 0, -mUserActions.getHeight(), 0);
        }

        ta.setDuration(time);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mUserActions.clearAnimation();
                if (isActive) {
                    mUserActions.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (!isActive) {
            mUserActions.setVisibility(View.VISIBLE);
        }
        mUserActions.startAnimation(ta);
    }

    private void initUserActions(View root) {
        mUserActions = (LinearLayout) root.findViewById(R.id.mUserActions);

        ArrayList<UserActions.ActionItem> actions = new ArrayList<UserActions.ActionItem>();
        actions.add(new UserActions.ActionItem(R.id.acGift, this));
        actions.add(new UserActions.ActionItem(R.id.acSympathy, this));
        actions.add(new UserActions.ActionItem(R.id.acDelight, this));
        actions.add(new UserActions.ActionItem(R.id.acChat, this));
        actions.add(new UserActions.ActionItem(R.id.acBlock, this));
        actions.add(new UserActions.ActionItem(R.id.acComplain, this));
        actions.add(new UserActions.ActionItem(R.id.acBookmark, this));
        new UserActions(mUserActions, actions);
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

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateProfileReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        setProfile(mUserProfile);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateProfileReceiver);

        int key;
        Fragment fragment;
        SparseArrayCompat<Fragment> fragments;
        //Вручную прокидываем событие onPause() в ViewPager, т.к. на onPause() мы отписываемся от событий
        if (mBodyPagerAdapter != null) {
            fragments = mBodyPagerAdapter.getFragmentCache();
            for (int i = 0; i < fragments.size(); i++) {
                key = fragments.keyAt(i);
                fragment = fragments.get(key);
                if (fragment != null) {
                    fragment.onPause();
                }
            }
        }

        if (mHeaderPagerAdapter != null) {
            fragments = mHeaderPagerAdapter.getFragmentCache();
            for (int i = 0; i < fragments.size(); i++) {
                key = fragments.keyAt(i);
                fragment = fragments.get(key);
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
        if (mUserPhotoFragment != null && profile instanceof User)
            mUserPhotoFragment.setUserData((User) profile);
        if (mUserFormFragment != null && profile instanceof User)
            mUserFormFragment.setUserData((User) profile);
    }

    private void getUserProfile() {
        mLoaderView.setVisibility(View.VISIBLE);
        if (mProfileId < 1) {
            mLoaderView.setVisibility(View.INVISIBLE);
            mRetryView.showOnlyMessage(true);
            mLockScreen.setVisibility(View.VISIBLE);
            return;
        }
        UserRequest userRequest = new UserRequest(mProfileId, getActivity());
        registerRequest(userRequest);
        userRequest.callback(new DataApiHandler<User>() {

            @Override
            protected void success(User data, IApiResponse response) {
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

                    if (data.inBlackList) {
                        ((TextView) mBlocked.findViewById(R.id.blockTV)).setText(R.string.black_list_delete);
                    } else {
                        ((TextView) mBlocked.findViewById(R.id.blockTV)).setText(R.string.black_list_add_short);
                    }

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
            public void fail(final int codeError, IApiResponse response) {
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
        if (mRetryView != null && isAdded()) {
            mLoaderView.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.VISIBLE);
            mRetryView.setText(text);
            mRetryView.showOnlyMessage(true);
        }
    }

    private void showRetryBtn() {
        if (mRetryView != null && isAdded()) {
            mLoaderView.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.VISIBLE);
            mRetryView.setText(getString(R.string.general_profile_error));
            mRetryView.showOnlyMessage(false);
        }
    }

    @Override
    protected void restoreState() {
        mProfileId = getArguments().getInt(ARG_TAG_PROFILE_ID);
        mProfileType = getArguments().getInt(ARG_TAG_PROFILE_TYPE);
        mBodyStartPageClassName = getArguments().getString(ARG_TAG_INIT_BODY_PAGE);
        mHeaderStartPageClassName = getArguments().getString(ARG_TAG_INIT_HEADER_PAGE);
        mCallingClass = getArguments().getString(ARG_TAG_CALLING_CLASS);
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
            addBodyPage(VipBuyFragment.class.getName(), getResources().getString(R.string.vip_status));
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
                startSettingsActivity();
                break;
            case R.id.acDelight:
                if (v.isEnabled()) {
                    v.setSelected(true);
                    final TextView textView = (TextView) v.findViewById(R.id.delTV);
                    final ProgressBar loader = (ProgressBar) v.findViewById(R.id.delPrBar);
                    final ImageView icon = (ImageView) v.findViewById(R.id.delIcon);

                    loader.setVisibility(View.VISIBLE);
                    icon.setVisibility(View.GONE);

                    textView.setTextColor(Color.parseColor(DEFAULT_ACTIVATED_COLOR));
                    v.findViewById(R.id.delPrBar).setVisibility(View.VISIBLE);
                    v.setEnabled(false);
                    mRateController.onAdmiration(
                            mUserProfile.uid,
                            ((User) mUserProfile).mutual ?
                                    SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateCompleted() {
                                    if (v != null && getActivity() != null) {
                                        Toast.makeText(App.getContext(), R.string.admiration_sended, Toast.LENGTH_SHORT).show();
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
                                        if (CacheProfile.money > 0) {
                                            Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
                                        }
                                        v.setEnabled(true);
                                        v.setSelected(false);
                                        if (textView != null) {
                                            textView.setTextColor(Color.parseColor(DEFAULT_NON_ACTIVATED));
                                        }
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
                    mRateController.onLike(
                            mUserProfile.uid,
                            ((User) mUserProfile).mutual ?
                                    SendLikeRequest.DEFAULT_MUTUAL : SendLikeRequest.DEFAULT_NO_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateCompleted() {
                                    if (v != null && getActivity() != null) {
                                        Toast.makeText(App.getContext(), R.string.sympathy_sended, Toast.LENGTH_SHORT).show();
                                        loader.setVisibility(View.INVISIBLE);
                                        icon.setVisibility(View.VISIBLE);
                                    }
                                }

                                @SuppressWarnings("ConstantConditions")
                                @Override
                                public void onRateFailed() {
                                    if (v != null && getActivity() != null) {
                                        Toast.makeText(App.getContext(), R.string.general_server_error, Toast.LENGTH_SHORT).show();
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
                giftsLoader = (ProgressBar) v.findViewById(R.id.giftPrBar);
                giftsIcon = (ImageView) v.findViewById(R.id.giftIcon);
                giftsLoader.setVisibility(View.VISIBLE);
                giftsIcon.setVisibility(View.INVISIBLE);
                if (mGiftFragment != null && mGiftFragment.getActivity() != null) {
                    mGiftFragment.sendGift(giftsReceivedListener);
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            GiftsActivity.class);
                    startActivityForResult(intent, GiftsActivity.INTENT_REQUEST_GIFT);
                }
                break;
            case R.id.acChat:
                if (CacheProfile.premium || !CacheProfile.getOptions().block_chat_not_mutual) {
                    openChat();
                } else {
                    if (mCallingClass != null && mUserProfile != null && (mUserProfile instanceof User)) {
                        if (mCallingClass.equals(DatingFragment.class.getName()) || mCallingClass.equals(LeadersDialog.class.getName())) {
                            if (!((User) mUserProfile).mutual) {
                                startActivityForResult(
                                        ContainerActivity.getVipBuyIntent(getString(R.string.chat_block_not_mutual), "ProfileChatLock"),
                                        ContainerActivity.INTENT_BUY_VIP_FRAGMENT
                                );
                                break;
                            }
                        }
                    }
                    openChat();
                }
                break;
            case R.id.acBlock:
                if (CacheProfile.premium) {
                    if (mUserProfile.uid > 0) {
                        final TextView textView = (TextView) v.findViewById(R.id.blockTV);
                        final ProgressBar loader = (ProgressBar) v.findViewById(R.id.blockPrBar);
                        final ImageView icon = (ImageView) v.findViewById(R.id.blockIcon);

                        loader.setVisibility(View.VISIBLE);
                        icon.setVisibility(View.GONE);
                        ApiRequest request;
                        if (mUserProfile.inBlackList) {
                            request = new BlackListDeleteManyRequest(mUserProfile.uid, getActivity());
                        } else {
                            request = new BlackListAddManyRequest(mUserProfile.uid, getActivity());
                        }
                        request.callback(new VipApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                super.success(response);
                                if (isAdded()) {
                                    loader.setVisibility(View.INVISIBLE);
                                    icon.setVisibility(View.VISIBLE);
                                    mUserProfile.inBlackList = !mUserProfile.inBlackList;
                                    if (mUserProfile.inBlackList) {
                                        textView.setText(R.string.black_list_delete);
                                    } else {
                                        textView.setText(R.string.black_list_add_short);
                                    }
                                }
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                super.fail(codeError, response);
                                if (isAdded()) {
                                    loader.setVisibility(View.INVISIBLE);
                                    icon.setVisibility(View.VISIBLE);
                                }
                            }
                        }).exec();
                    }
                } else {
                    startActivityForResult(ContainerActivity.getVipBuyIntent(null, "ProfileSuperSkills"), ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
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
                    request = new BookmarkDeleteManyRequest(getActivity(), mUserProfile.uid);
                } else {
                    request = new BookmarkAddRequest(getActivity(), mUserProfile.uid);
                }

                request.callback(new SimpleApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
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
                    public void always(IApiResponse response) {
                        super.always(response);
                        if (isAdded()) {
                            loader.setVisibility(View.INVISIBLE);
                            icon.setVisibility(View.VISIBLE);
                        }
                    }
                }).exec();
                break;
            case R.id.acComplain:
                startActivity(ContainerActivity.getComplainIntent(mProfileId));
                break;
            default:
                break;
        }
    }

    private void startSettingsActivity() {
        startActivity(ContainerActivity.getNewIntent(ContainerActivity.INTENT_SETTINGS_FRAGMENT));
    }

    public void openChat() {
        if (mUserProfile != null) {
            Intent intent = new Intent(getActivity(), ContainerActivity.class);
            intent.putExtra(ChatFragment.INTENT_USER_ID, mUserProfile.uid);
            intent.putExtra(ChatFragment.INTENT_USER_NAME, mUserProfile.firstName != null ?
                    mUserProfile.firstName : Static.EMPTY);
            intent.putExtra(ChatFragment.INTENT_USER_SEX, mUserProfile.sex);
            intent.putExtra(ChatFragment.INTENT_USER_AGE, mUserProfile.age);
            intent.putExtra(ChatFragment.INTENT_USER_CITY, mUserProfile.city == null ? "" : mUserProfile.city.name);
            intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, ((Object) this).getClass().getSimpleName());
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

    public static ProfileFragment newInstance(String itemId, int id, int type, String className) {
        ProfileFragment fragment = new ProfileFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_TAG_PROFILE_ID, id);
        args.putInt(ARG_TAG_PROFILE_TYPE, type);
        args.putString(ARG_FEED_ITEM_ID, itemId);
        args.putString(ARG_TAG_CALLING_CLASS, className);
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
        if (mLoaderView != null) {
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
            if (requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
                if (mGiftFragment == null || !mGiftFragment.isAdded()) {
                    sendGift(data);
                    return;
                }
            }
            if ((requestCode == AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_CAMERA ||
                    requestCode == AddPhotoHelper.GALLERY_IMAGE_ACTIVITY_REQUEST_CODE_LIBRARY) &&
                    !((NavigationActivity) getActivity()).getDialogStarted()
                    ) {
                mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
            }
            resultToNestedFragments(requestCode, resultCode, data);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            giftsReceivedListener.onReceived();
        }
    }

    private void sendGift(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            final int id = extras.getInt(GiftsActivity.INTENT_GIFT_ID);
            final String url = extras.getString(GiftsActivity.INTENT_GIFT_URL);
            final int price = extras.getInt(GiftsActivity.INTENT_GIFT_PRICE);

            if (mUserProfile != null) {
                final SendGiftRequest sendGift = new SendGiftRequest(getActivity());
                registerRequest(sendGift);
                sendGift.giftId = id;
                sendGift.userId = mUserProfile.uid;
                final FeedGift sendedGift = new FeedGift();
                sendedGift.gift = new Gift(
                        sendGift.giftId,
                        Gift.PROFILE_NEW,
                        url,
                        0
                );
                sendGift.callback(new DataApiHandler<SendGiftAnswer>() {

                    @Override
                    protected void success(SendGiftAnswer data, IApiResponse response) {
                        CacheProfile.likes = data.likes;
                        CacheProfile.money = data.money;
                        if (mGiftFragment != null) {
                            mGiftFragment.addGift(sendedGift);
                        } else {
                            mUserProfile.gifts.add(0, sendedGift.gift);
                        }
                        Toast.makeText(getContext(), R.string.chat_gift_out, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected SendGiftAnswer parseResponse(ApiResponse response) {
                        return SendGiftAnswer.parse(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Utils.showErrorMessage(getContext());
                        if (response.isCodeEqual(ErrorCodes.PAYMENT)) {
                            FragmentActivity activity = getActivity();
                            if (activity != null) {
                                Intent intent = ContainerActivity.getBuyingIntent("Profile");
                                intent.putExtra(BuyingFragment.ARG_ITEM_TYPE, BuyingFragment.TYPE_GIFT);
                                intent.putExtra(BuyingFragment.ARG_ITEM_PRICE, price);
                                startActivity(intent);
                            }
                        }
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        giftsReceivedListener.onReceived();
                    }
                }).exec();
            }
        }
    }

    public void resultToNestedFragments(int requestCode, int resultCode, Intent data) {
        int key;
        Fragment fragment;
        SparseArrayCompat<Fragment> mBodyFragments = mBodyPagerAdapter.getFragmentCache();
        for (int i = 0; i < mBodyFragments.size(); i++) {
            key = mBodyFragments.keyAt(i);
            fragment = mBodyFragments.get(key);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }


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

    public interface OnGiftReceivedListener {
        public void onReceived();
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return mProfileType == TYPE_MY_PROFILE ? R.menu.actions_my_profile : R.menu.actions_user_profile;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettingsActivity();
                return true;
            case R.id.action_user_actions_list:
                boolean checked = !item.isChecked();
                item.setChecked(checked);
                animateProfileActions(!checked, 500);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_OK) {
                Photo photo = (Photo) msg.obj;
                // ставим фото на аватарку только если она едиснтвенная
                if (CacheProfile.photos.size() == 0) {
                    CacheProfile.photo = photo;
                }
                // добавляется фото в начало списка
                CacheProfile.photos.addFirst(photo);
                ArrayList<Photo> photosForAdd = new ArrayList<Photo>();
                photosForAdd.add(photo);
                Intent intent = new Intent(PhotoSwitcherActivity.DEFAULT_UPDATE_PHOTOS_INTENT);
                intent.putExtra(PhotoSwitcherActivity.INTENT_PHOTOS, photosForAdd);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                // оповещаем всех об изменениях
                CacheProfile.sendUpdateProfileBroadcast();
                Toast.makeText(App.getContext(), R.string.photo_add_or, Toast.LENGTH_SHORT).show();
            } else if (msg.what == AddPhotoHelper.ADD_PHOTO_RESULT_ERROR) {
                Toast.makeText(App.getContext(), R.string.photo_add_error, Toast.LENGTH_SHORT).show();
            }
        }
    };
}
