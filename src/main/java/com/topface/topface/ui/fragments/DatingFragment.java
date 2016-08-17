package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryDialog;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.Ssid;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.BalanceData;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.NoviceLikes;
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.data.search.CachableSearchList;
import com.topface.topface.data.search.OnUsersListEventsListener;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FilterRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.NoviceLikesRequest;
import com.topface.topface.requests.ResetFilterRequest;
import com.topface.topface.requests.SearchRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.SkipRateRequest;
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.state.TopfaceAppState;
import com.topface.topface.statistics.TakePhotoStatistics;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.dialogs.TakePhotoPopup;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.filter.model.FilterData;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.KeyboardListenerLayout;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.AddPhotoHelper;
import com.topface.topface.utils.AnimationHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.FlurryManager;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.PreloadManager;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.controllers.DatingInstantMessageController;
import com.topface.topface.utils.loadcontollers.AlbumLoadController;
import com.topface.topface.utils.social.AuthToken;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
import static com.topface.topface.ui.edit.filter.view.FilterFragment.INTENT_DATING_FILTER;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker,
        RateController.OnRateControllerListener {

    private static final String CURRENT_USER = "current_user";
    private static final String PAGE_NAME = "Dating";
    private static final String IS_SOFT_KEYBOARD_SHOW = "is_soft_keyboard_show";
    private static final String FULLSCREEN_STATE = "fullscreen_state";

    @Inject
    TopfaceAppState mAppState;
    AtomicBoolean isAdmirationFailed = new AtomicBoolean(false);
    private BalanceData mBalanceData;
    private KeyboardListenerLayout mRoot;
    private TextView mResourcesLikes;
    private TextView mResourcesMoney;
    private Button mDelightBtn;
    private Button mMutualBtn;
    private Button mSkipBtn;
    private Button mProfileBtn;
    private TextView mUserInfoStatus;
    private TextView mDatingCounter;
    private TextView mDatingLovePrice;
    private View mDatingResources;
    private View mDatingButtons;
    private RateController mRateController;
    private ImageSwitcher mImageSwitcher;
    private CachableSearchList<SearchUser> mUserSearchList;
    private RelativeLayout mDatingLoveBtnLayout;
    private RetryViewCreator mRetryView;
    private ImageButton mRetryBtn;
    private PreloadManager<SearchUser> mPreloadManager;
    private boolean mKeyboardWasShown = false; // по умолчанию клава закрыта

    private AddPhotoHelper mAddPhotoHelper;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AddPhotoHelper.handlePhotoMessage(msg);
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPreloadManager != null) {
                mPreloadManager.checkConnectionType();
            }
        }
    };
    private DatingInstantMessageController mDatingInstantMessageController;
    private boolean mCanSendAlbumReq = true;
    private SearchUser mCurrentUser;
    private int mCurrentStatusBarColor;
    private Action1<BalanceData> mBalanceAction = new Action1<BalanceData>() {
        @Override
        public void call(BalanceData balanceData) {
            mBalanceData = balanceData;
            mDatingLovePrice.setVisibility(mBalanceData.premium ? View.GONE : View.VISIBLE);
            updateResources(balanceData);
        }
    };

    private BroadcastReceiver mUpdateActionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BlackListAndBookmarkHandler.ActionTypes type = (BlackListAndBookmarkHandler.ActionTypes) intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE);
            if (type != null) {
                switch (type) {
                    case BLACK_LIST:
                        skipUser(mCurrentUser);
                        showNextUser();
                        break;
                    case SYMPATHY:
                        if (null != mMutualBtn) {
                            mMutualBtn.setEnabled(false);
                        }
                        if (null != mDelightBtn) {
                            mDelightBtn.setEnabled(false);
                        }
                        if (mCurrentUser != null) {
                            mCurrentUser.rated = true;
                        }
                        break;
                }
            }
        }
    };

    private Action1<Options> mOptionsAction = new Action1<Options>() {
        @Override
        public void call(Options options) {
            UserConfig userConfig = App.getUserConfig();
            Options.InstantMessageFromSearch message = options.instantMessageFromSearch;
            String instantMessage = userConfig.getDatingMessage();
            // в приоритете данные из кэша, но если там пусто, то возьмем данные из опций
            message.setText(!TextUtils.isEmpty(instantMessage) ? instantMessage : !TextUtils.isEmpty(message.getText()) ? message.getText() : Utils.EMPTY);
            if (mDatingInstantMessageController != null) {
                mDatingInstantMessageController.setInstantMessageText(message.getText());
            }
            // чтобы лишний раз не дергать перезапись shared preff проверяем на equals
            if (!message.getText().equals(instantMessage)) {
                userConfig.setDatingMessage(message.getText());
                userConfig.saveConfig();
            }
        }
    };
    /**
     * Флаг того, что запущено обновление поиска и запускать дополнительные обновления не нужно
     */
    private boolean mUpdateInProcess;
    private BroadcastReceiver mProfileReceiver;
    private boolean mNeedMore;
    private int mLoadedCount;
    private boolean isHideAdmirations;
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {

            if (position + mController.getItemsOffsetByConnectionType() == (mLoadedCount - 1)) {
                final Photos data = ((ImageSwitcher.ImageSwitcherAdapter) mImageSwitcher.getAdapter()).getData();

                if (mNeedMore && mCanSendAlbumReq) {
                    mCanSendAlbumReq = false;
                    sendAlbumRequest(data);
                }
            }

            setCounter(mImageSwitcher.getSelectedPosition());
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };
    private CompositeSubscription mDatingSubscriptions = new CompositeSubscription();

    @Override
    protected int getStatusBarColor() {
        return mCurrentStatusBarColor;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    private boolean mNewFilter;
    private OnUsersListEventsListener mSearchListener = new OnUsersListEventsListener() {
        @Override
        public void onEmptyList(UsersList usersList) {
            if (!mNewFilter) {
                lockControls();
                updateData(false);
            }
        }

        @Override
        public void onPreload(UsersList usersList) {
            if (!mNewFilter) {
                updateData(true);
            }
        }

    };
    private INavigationFragmentsListener mFragmentSwitcherListener;
    private AnimationHelper mAnimationHelper;
    private AlbumLoadController mController;
    private AtomicBoolean moneyDecreased = new AtomicBoolean(false);
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

    private void startDatingFilterActivity() {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                EditContainerActivity.class);
        startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof INavigationFragmentsListener) {
            mFragmentSwitcherListener = (INavigationFragmentsListener) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mFragmentSwitcherListener = null;
        if (getTitleSetter() != null) {
            getTitleSetter().setOnline(false);
        }
    }

    private boolean isValidUserCache() {
        UserConfig config = App.getUserConfig();
        if (config.isUserCityChanged()) {
            config.setUserCityChanged(false);
            config.saveConfig();
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mKeyboardWasShown = savedInstanceState != null && savedInstanceState.getBoolean(IS_SOFT_KEYBOARD_SHOW);
        getActivity().getWindow().setSoftInputMode(mKeyboardWasShown ? SOFT_INPUT_STATE_ALWAYS_VISIBLE | SOFT_INPUT_ADJUST_RESIZE : SOFT_INPUT_STATE_ALWAYS_HIDDEN | SOFT_INPUT_ADJUST_RESIZE);
        super.onCreate(savedInstanceState);
        App.from(getActivity()).inject(this);
        if (mUserSearchList == null) {
            mUserSearchList = new CachableSearchList<>(SearchUser.class);
        }
        if (!isValidUserCache()) {
            mUserSearchList.clear();
        }
        if (savedInstanceState != null) {
            mCurrentUser = savedInstanceState.getParcelable(CURRENT_USER);
        }
        mIsHide = savedInstanceState != null && savedInstanceState.getBoolean(FULLSCREEN_STATE);
        mPreloadManager = new PreloadManager<>();
        mController = new AlbumLoadController(AlbumLoadController.FOR_PREVIEW);
        // Rate Controller
        mRateController = new RateController(this, SendLikeRequest.FROM_SEARCH);
        mRateController.setOnRateControllerUiListener(this);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUpdateActionsReceiver, new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(CURRENT_USER, mCurrentUser);
        outState.setClassLoader(SearchUser.class.getClassLoader());
        outState.putBoolean(IS_SOFT_KEYBOARD_SHOW, mDatingInstantMessageController != null && mDatingInstantMessageController.isKeyboardShown());
        outState.putBoolean(FULLSCREEN_STATE, mIsHide);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        isHideAdmirations = App.from(getActivity()).getOptions().isHideAdmirations;
        mRoot = (KeyboardListenerLayout) inflater.inflate(R.layout.fragment_dating, null);
        initViews(mRoot);
        mDatingSubscriptions.add(mAppState.getObservable(BalanceData.class).subscribe(mBalanceAction));
        mDatingSubscriptions.add(mAppState.getObservable(Options.class).subscribe(mOptionsAction));
        initEmptySearchDialog(mRoot);
        initImageSwitcher(mRoot);
        if (mCurrentUser != null) {
            fillUserInfo(mCurrentUser);
        }
        if (isHideAdmirations) {
            mDatingCounter.setVisibility(View.GONE);
            mDatingResources.setVisibility(View.GONE);
        }
        mAddPhotoHelper = new AddPhotoHelper(this, null);
        mAddPhotoHelper.setOnResultHandler(mHandler);
        return mRoot;
    }

    @Override
    public void onResume() {
        if (mIsHide) {
            setDarkStatusBarColor();
            hideControls();
        } else {
            setMainStatusBarColor();
            showControls();
        }
        super.onResume();
        if (getTitleSetter() != null) {
            getTitleSetter().setOnline(mCurrentUser != null && mCurrentUser.online);
        }
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mProfileReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAddPhotoHelper != null) {
            mAddPhotoHelper.releaseHelper();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mDatingSubscriptions && !mDatingSubscriptions.isUnsubscribed()) {
            mDatingSubscriptions.unsubscribe();
        }
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUpdateActionsReceiver);
        if (mRateController != null) {
            mRateController.destroyController();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRetryView.isVisible()) {
            EasyTracker.sendEvent("EmptySearch", "DismissScreen", "", 0L);
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileReceiver);
        //При выходе из фрагмента сохраняем кэш поиска
        if (mUserSearchList != null) {
            if (LocaleConfig.localeChangeInitiated) {
                mUserSearchList.removeAllUsers();
                mUserSearchList.saveCache();
            } else {
                mUserSearchList.saveCache();
            }
        }
    }

    private void setDarkStatusBarColor() {
        mCurrentStatusBarColor = R.color.status_bar_dating_screen_hide_mode_color;
        setStatusBarColor();
    }

    private void setMainStatusBarColor() {
        mCurrentStatusBarColor = Utils.getColorPrimaryDark(getActivity());
        setStatusBarColor();
    }

    private void initViews(final KeyboardListenerLayout root) {
        mRetryBtn = (ImageButton) root.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);
        // User Info
        mUserInfoStatus = (TextView) root.findViewById(R.id.tvDatingUserStatus);
        // Counter
        mDatingCounter = (TextView) root.findViewById(R.id.tvDatingCounter);
        initResources(root);

        mAnimationHelper = new AnimationHelper(getActivity(), R.anim.fade_in, R.anim.fade_out);
        mAnimationHelper.addView(mDatingCounter);
        mAnimationHelper.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (!mIsHide) {
                    setMainStatusBarColor();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIsHide) {
                    setDarkStatusBarColor();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        ViewStub stub = (ViewStub) root.findViewById(R.id.vfDatingButtons);
        stub.setLayoutResource(isHideAdmirations ? R.layout.hide_admiration_dating_buttons : R.layout.dating_buttons);
        mDatingButtons = stub.inflate();
        initControlButtons(root);
        initInstantMessageController(mRoot);
        if (!isHideAdmirations) {
            // Dating controls
            mDatingLoveBtnLayout = (RelativeLayout) root.findViewById(R.id.loDatingLove);
            mDatingLovePrice = (TextView) root.findViewById(R.id.tvDatingLovePrice);

            mAnimationHelper.addView(mDatingResources);
        }
    }

    private void setHighRatePrice() {
        // Dating Love Price
        final int delightPrice = App.from(getActivity()).getOptions().priceAdmiration;
        if (null != mDatingLovePrice) {
            if (delightPrice > 0) {
                mDatingLovePrice.setVisibility(View.VISIBLE);
                mDatingLovePrice.setText(String.valueOf(delightPrice));
            } else {
                mDatingLovePrice.setVisibility(View.GONE);
            }
        }
    }

    private void initImageSwitcher(View view) {
        // Dating Album
        mImageSwitcher = ((ImageSwitcher) view.findViewById(R.id.glrDatingAlbum));
        mImageSwitcher.needAnimateLoader(false);
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);
        mImageSwitcher.setUpdateHandler(mUnlockHandler);
    }

    private void initResources(View view) {
        // Resources
        mDatingResources = view.findViewById(R.id.loDatingResources);
        mDatingResources.setOnClickListener(this);
        mResourcesLikes = (TextView) view.findViewById(R.id.tvResourcesLikes);
        mResourcesMoney = (TextView) view.findViewById(R.id.tvResourcesMoney);
    }

    private void initControlButtons(View view) {
        // Control Buttons
        if (!isHideAdmirations) {
            mDelightBtn = (Button) view.findViewById(R.id.btnDatingAdmiration);
            mDelightBtn.setOnClickListener(this);
        }
        mMutualBtn = (Button) view.findViewById(R.id.btnDatingSympathy);
        mMutualBtn.setOnClickListener(this);
        mSkipBtn = (Button) view.findViewById(R.id.btnDatingSkip);
        mSkipBtn.setOnClickListener(this);
        mProfileBtn = (Button) view.findViewById(R.id.btnDatingProfile);
        mProfileBtn.setOnClickListener(this);
    }

    protected String getTitle() {
        if (mCurrentUser != null) {
            return mCurrentUser.getNameAndAge();
        }
        return getString(R.string.general_dating);
    }

    protected String getSubtitle() {
        if (mCurrentUser != null && mCurrentUser.city != null) {
            return mCurrentUser.city.getName();
        }
        return null;
    }

    private void initEmptySearchDialog(View view) {
        RetryViewCreator.Builder rvcBuilder = new RetryViewCreator.Builder(getActivity(), new OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.sendEvent("EmptySearch", "ClickTryAgain", "", 0L);
                updateData(false, true);
            }
        }).setImageVisibility(View.GONE).message(getString(R.string.general_search_null_response_error))
                .setMessageTextColor(Color.parseColor("#FFFFFF"))
                .orientation(LinearLayout.VERTICAL)
                .button(getString(R.string.reset_filter), new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EasyTracker.sendEvent("EmptySearch", "ClickResetFilter", "", 0L);
                        ResetFilterRequest resetRequest = new ResetFilterRequest(getActivity());
                        registerRequest(resetRequest);
                        hideEmptySearchDialog();
                        resetRequest.callback(new FilterHandler() {
                            @Override
                            protected void success(DatingFilter filter, IApiResponse response) {
                                DatingFilter.setOnlyOnlineField(false);
                                super.success(filter, response);
                            }
                        }).exec();
                    }
                });
        mRetryView = rvcBuilder.build();

        hideEmptySearchDialog();
        ((RelativeLayout) view.findViewById(R.id.ac_dating_container)).addView(mRetryView.getView());

        mProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isAdded()) {
                    updateFilterData();
                    setHighRatePrice();
                    Activity activity = getActivity();
                    if (mDatingInstantMessageController != null
                            && activity instanceof NavigationActivity
                            && activity.getIntent().hasExtra(DatingInstantMessageController.DEFAULT_MESSAGE)) {
                        mDatingInstantMessageController.updateMessageIfNeed();
                    }
                }
            }
        };
    }

    private void updateFilterData() {
        //Если изменился фильтр, то мы ставим новую подпись фильтра,
        //если она отличается от той, что в поиске, то поиск будет очищен
        if (mUserSearchList != null) {
            mUserSearchList.updateSignatureAndUpdate();
        }
    }

    private void updateData(final boolean isAddition) {
        updateData(isAddition, false);
    }


    private void updateData(final boolean isAddition, boolean isNeedRefresh) {
        if (!mUpdateInProcess) {
            lockControls();
            hideEmptySearchDialog();
            if (!isAddition) {
                onUpdateStart(false);
            }
            if (isNeedRefresh) {
                mUserSearchList.clear();
                mCurrentUser = null;
            }

            mUpdateInProcess = true;

            UsersList.log("Update start: " + (isAddition ? "addition" : "replace"));

            getSearchRequest(isNeedRefresh).callback(new DataApiHandler<UsersList>() {

                @Override
                protected void success(UsersList usersList, IApiResponse response) {
                    UsersList.log("load success. Loaded " + usersList.size() + " users");
                    if (usersList.size() != 0) {
                        mImageSwitcher.setVisibility(View.VISIBLE);
                        //Добавляем новых пользователей
                        mUserSearchList.addAndUpdateSignature(usersList);
                        //если список был пуст, то просто показываем нового пользователя
                        SearchUser currentUser = mUserSearchList.getCurrentUser();
                        //NOTE: Если в поиске никого нет, то мы показываем следующего юзера
                        //Но нужно учитывать, что такое происходит при смене фильтра не через приложение,
                        //Когда чистится поиск, если фильтр поменялся удаленно,
                        //из-за чего происходит автоматический переход на следующего юзера
                        //От этого эффекта можно избавиться, если заменить на такое условие:
                        //<code>if (!isAddition && mCurrentUser != currentUser || mCurrentUser == null)</code>
                        //Но возникает странный эффект, когда в поиске написано одно, а у юзера другое,
                        //В связи с чем, все работает так как работает
                        if (currentUser != null && mCurrentUser != currentUser) {
                            showUser(currentUser);
                            unlockControls();
                        } else if (mUserSearchList.isEmpty() || mUserSearchList.isEnded()) {
                            showEmptySearchDialog();
                        } else {
                            unlockControls();
                        }

                        //Скрываем кнопку отправки повтора
                        mRetryBtn.setVisibility(View.GONE);
                    } else {
                        if (!isAddition || mUserSearchList.isEmpty()) {
                            showEmptySearchDialog();
                        }
                    }
                }

                @SuppressWarnings("unchecked")
                @Override
                protected UsersList parseResponse(ApiResponse response) {
                    return new UsersList(response, SearchUser.class);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        UsersList.log("load error: " + response.getErrorMessage());
                        onUpdateFail(isAddition);
                        unlockControls();
                    }
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    mUpdateInProcess = false;
                }
            }).exec();
        }
    }

    @Override
    protected void onLoadProfile() {
        super.onLoadProfile();
        if (Ssid.isLoaded() && !AuthToken.getInstance().isEmpty()) {
            //Показываем последнего пользователя
            if (mUserSearchList == null) {
                mUserSearchList = new CachableSearchList<>(SearchUser.class);
            }
            if (mCurrentUser == null) {
                SearchUser currentUser = mUserSearchList.getCurrentUser();
                mUserSearchList.setOnEmptyListListener(mSearchListener);
                if (currentUser != null) {
                    showUser(currentUser);
                } else {
                    showNextUser();
                }
            } else {
                //Сделано для того, чтобы не показывалось сообщение о том, что пользователи не найдены.
                //Иначе при старте приложения, пока список пользователей не запросился показывается сообщение об ошибки
                mUserSearchList.setOnEmptyListListener(mSearchListener);
            }
            setLikesForNovice();
            updateFilterData();
        }

        if (mRoot != null && mDatingInstantMessageController == null) {
            initInstantMessageController(mRoot);
        }

        setHighRatePrice();
    }

    private void initInstantMessageController(KeyboardListenerLayout root) {
        mDatingInstantMessageController = new DatingInstantMessageController(getActivity(), root,
                this, this, mDatingButtons, mUserInfoStatus, new DatingInstantMessageController.SendLikeAction() {
            @Override
            public void sendLike() {
                sendSympathy();
            }
        }, new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    mDatingInstantMessageController.instantSend(mCurrentUser);
                }
                return false;
            }
        });
        mDatingInstantMessageController.setKeyboardShown(mKeyboardWasShown);
    }

    private SearchRequest getSearchRequest(boolean isNeedRefresh) {
        SearchRequest searchRequest = new SearchRequest(getFilterOnlyOnline(), getActivity(), isNeedRefresh);
        registerRequest(searchRequest);
        return searchRequest;
    }

    private boolean getFilterOnlyOnline() {
        return DatingFilter.getOnlyOnlineField();
    }

    @Override
    public void onClick(View view) {
        if (!CacheProfile.isLoaded()) {
            return;
        }
        final Options options = App.from(getActivity()).getOptions();
        switch (view.getId()) {
            case R.id.loDatingResources: {
                EasyTracker.sendEvent("Dating", "BuyClick", "", 1L);
                startActivity(PurchasesActivity.createBuyingIntent("Dating", options.topfaceOfferwallRedirect));
            }
            break;
            case R.id.btnDatingAdmiration: {
                if (!takePhotoIfNeed(TakePhotoStatistics.PLC_DATING_ADMIRATION)) {
                    if (mCurrentUser != null) {
                        lockControls();
                        isAdmirationFailed.set(false);
                        boolean canSendAdmiration = mRateController.onAdmiration(mBalanceData,
                                mCurrentUser.id,
                                mCurrentUser.isMutualPossible ?
                                        SendLikeRequest.DEFAULT_MUTUAL
                                        : SendLikeRequest.DEFAULT_NO_MUTUAL,
                                new RateController.OnRateRequestListener() {
                                    @Override
                                    public void onRateCompleted(int mutualId, int ratedUserId) {
                                        isAdmirationFailed.set(true);
                                        EasyTracker.sendEvent("Dating", "Rate",
                                                "AdmirationSend" + (mutualId == SendLikeRequest.DEFAULT_MUTUAL ? "mutual" : ""),
                                                (long) options.priceAdmiration);
                                    }

                                    @Override
                                    public void onRateFailed(int userId, int mutualId) {
                                        if (moneyDecreased.get()) {
                                            moneyDecreased.set(false);
                                            updateResources(mBalanceData);
                                        } else {
                                            isAdmirationFailed.set(true);
                                            unlockControls();
                                        }
                                    }
                                }, options
                        );
                        if (canSendAdmiration && !isAdmirationFailed.get()) {
                            BalanceData balance = new BalanceData(mBalanceData.premium, mBalanceData.likes, mBalanceData.money);
                            balance.money = balance.money - options.priceAdmiration;
                            moneyDecreased.set(true);
                            updateResources(balance);
                        }
                    }
                }
            }
            break;
            case R.id.btnDatingSympathy: {
                if (!takePhotoIfNeed(TakePhotoStatistics.PLC_DATING_LIKE)) {
                    sendSympathy();
                }
            }
            break;
            case R.id.skip_btn:
            case R.id.btnDatingSkip: {
                skipUser(mCurrentUser);
                showNextUser();
            }
            break;
            case R.id.btnDatingProfile: {
                if (mCurrentUser != null && getActivity() != null) {
                    Intent intent = UserProfileActivity.createIntent(null, mCurrentUser.photo, mCurrentUser.id, null, isChatAvailable()
                            , isAddToFavoritsAvailable(), mCurrentUser.firstName + ", " + mCurrentUser.age, mCurrentUser.city.name);
                    startActivityForResult(intent, UserProfileActivity.INTENT_USER_PROFILE);
                    EasyTracker.sendEvent("Dating", "Additional", "Profile", 1L);
                }
            }
            break;
            case R.id.btnUpdate: {
                updateData(false);
                mRetryBtn.setVisibility(View.GONE);
            }
            break;
            case R.id.btnSend: {
                if (!takePhotoIfNeed(TakePhotoStatistics.PLC_DATING_SEND)) {
                    mDatingInstantMessageController.instantSend(mCurrentUser);
                }
            }
            break;
            case R.id.send_gift_button: {
                if (mCurrentUser != null) {
                    startActivityForResult(
                            GiftsActivity.getSendGiftIntent(getActivity(), mCurrentUser.id, false),
                            GiftsActivity.INTENT_REQUEST_GIFT
                    );
                    EasyTracker.sendEvent("Dating", "SendGiftClick", "", 1L);
                }
            }
            break;
            case R.id.chat_btn: {
                if (!takePhotoIfNeed(TakePhotoStatistics.PLC_DATING_CHAT)) {
                    mDatingInstantMessageController.openChat(getActivity(), mCurrentUser);
                }
            }
            break;
            default:
        }
    }

    private boolean takePhotoIfNeed(String plc) {
        if (!App.getConfig().getUserConfig().isUserAvatarAvailable() && App.get().getProfile().photo == null) {
            if (mAddPhotoHelper != null) {
                TakePhotoPopup.newInstance(plc).show(getActivity().getSupportFragmentManager(), TakePhotoPopup.TAG);
                return true;
            }
        }
        return false;
    }

    private boolean isChatAvailable() {
        return !(!App.from(getActivity()).getProfile().premium
                && App.from(getActivity()).getOptions().blockChatNotMutual && !mCurrentUser.isMutualPossible);
    }

    private boolean isAddToFavoritsAvailable() {
        return App.from(getActivity()).getProfile().premium;
    }

    private void sendSympathy() {
        if (mCurrentUser != null) {
            if (!mCurrentUser.rated) {
                lockControls();
                mRateController.onLike(mCurrentUser.id,
                        mCurrentUser.isMutualPossible ?
                                SendLikeRequest.DEFAULT_MUTUAL
                                : SendLikeRequest.DEFAULT_NO_MUTUAL,
                        new RateController.OnRateRequestListener() {
                            @Override
                            public void onRateCompleted(int mutualId, int ratedUserId) {
                                if (mCurrentUser != null && ratedUserId == mCurrentUser.id) {
                                    if (null != mDelightBtn) {
                                        mDelightBtn.setEnabled(false);
                                    }
                                    mMutualBtn.setEnabled(false);
                                    mCurrentUser.rated = true;
                                } else if (mUserSearchList != null) {
                                    for (SearchUser searchUser : mUserSearchList) {
                                        if (searchUser.id == ratedUserId) {
                                            searchUser.rated = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onRateFailed(int userId, int mutualId) {
                                if (mCurrentUser != null) {
                                    mCurrentUser.rated = false;
                                }
                                unlockControls();
                            }
                        }, App.from(getActivity()).getOptions().blockUnconfirmed
                );
            } else {
                showNextUser();
            }
        }
    }

    private void showUser(SearchUser user) {
        if (user != null) {
            hideEmptySearchDialog();
            fillUserInfo(user);
            unlockControls();
            if (mDatingInstantMessageController != null) {
                mDatingInstantMessageController.displayMessageField();
            }
        }

        mPreloadManager.preloadPhoto(mUserSearchList);
    }

    private void showNextUser() {
        if (mUserSearchList != null) {
            showUser(mUserSearchList.nextUser());
            if (mDatingInstantMessageController != null) {
                mDatingInstantMessageController.reset();
            }
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void prevUser() {
        if (mUserSearchList != null) {
            fillUserInfo(mUserSearchList.prevUser());
        }
        unlockControls();
    }

    private void fillUserInfo(SearchUser currUser) {
        // User Info
        mCurrentUser = currUser;
        if (currUser == null || !isAdded()) {
            return;
        }
        refreshActionBarTitles();
        lockControls();
        //Устанавливаем статус пользователя.
        mUserInfoStatus.setText(Profile.normilizeStatus(currUser.getStatus()));

        Resources res = getResources();

        setUserOnlineStatus(currUser);
        if (!isHideAdmirations) {
            setUserSex(currUser, res);
            setLikeButtonDrawables(currUser);
        }
        setUserPhotos(currUser);

        mImageSwitcher.setData(currUser.photos);
        mImageSwitcher.setCurrentItem(0, true);
        setCounter(0);
    }

    private void setUserOnlineStatus(SearchUser currUser) {
        if (getTitleSetter() != null) {
            getTitleSetter().setOnline(currUser != null && currUser.online);
        }
    }

    @SuppressWarnings("deprecation")
    private void setUserSex(SearchUser currUser, Resources res) {
        if (currUser.sex == Profile.BOY) {
            mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, res
                    .getDrawable(R.drawable.dating_man_selector), null, null);
        } else if (currUser.sex == Profile.GIRL) {
            mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, res
                    .getDrawable(R.drawable.dating_woman_selector), null, null);
        }
    }

    private void setLikeButtonDrawables(SearchUser currUser) {
        // buttons drawables
        mMutualBtn.setCompoundDrawablesWithIntrinsicBounds(0, currUser.isMutualPossible ? R.drawable.dating_mutual_selector
                : R.drawable.dating_like_selector, 0, 0);
        mMutualBtn.setText(currUser.isMutualPossible ? App.getContext().getString(R.string.general_mutual)
                : App.getContext().getString(R.string.general_sympathy));
        if (null != mDelightBtn) {
            mDelightBtn.setCompoundDrawablesWithIntrinsicBounds(0,
                    currUser.isMutualPossible ? R.drawable.dating_dbl_admiration_selector : R.drawable.dating_admiration_selector, 0, 0);
        }
    }

    private void setUserPhotos(SearchUser currUser) {
        // photos
        mLoadedCount = currUser.photos.getRealPhotosCount();
        mNeedMore = currUser.photosCount > mLoadedCount;
        int rest = currUser.photosCount - currUser.photos.size();
        for (int i = 0; i < rest; i++) {
            currUser.photos.add(Photo.createFakePhoto());
        }
    }

    private void skipUser(SearchUser currentSearch) {
        if (currentSearch != null && !currentSearch.skipped && !currentSearch.rated) {
            if (App.isOnline()) {
                getSkipRateRequest(currentSearch).exec();
            } else {
                if (mUserSearchList.isCurrentUserLast()) {
                    showRetryDialog(getSkipRateRequest(currentSearch));
                }
            }
        }
    }

    private ApiRequest getSkipRateRequest(SearchUser currentSearch) {
        final SkipRateRequest skipRateRequest = new SkipRateRequest(getActivity());
        registerRequest(skipRateRequest);
        skipRateRequest.userid = currentSearch.id;
        skipRateRequest.callback(new SimpleApiHandler() {

            @Override
            public void success(IApiResponse response) {
                for (SearchUser user : mUserSearchList) {
                    if (user.id == skipRateRequest.userid) {
                        user.skipped = true;
                        return;
                    }
                }
            }
        });
        return skipRateRequest;
    }

    private void showRetryDialog(ApiRequest request) {
        RetryDialog retryDialog = new RetryDialog(getActivity().getString(R.string.general_internet_off), getActivity(), request);
        try {
            retryDialog.show();
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    private void setEnableInputButtons(boolean b) {
        if (mDatingInstantMessageController != null) {
            mDatingInstantMessageController.setEnabled(b);
        }
    }

    public void setCounter(int position) {
        if (mCurrentUser != null) {
            mDatingCounter.setText(String.format(App.getCurrentLocale(),"%d/%d", (position + 1), mCurrentUser.photos.size()));
            if (!mIsHide) mDatingCounter.setVisibility(View.VISIBLE);
        } else {
            mDatingCounter.setText("-/-");
            mDatingCounter.setVisibility(View.GONE);
        }
    }

    private void setLikesForNovice() {
        if (App.from(getActivity()).getProfile().giveNoviceLikes) {
            NoviceLikesRequest noviceLikesRequest = new NoviceLikesRequest(getActivity());
            registerRequest(noviceLikesRequest);
            noviceLikesRequest.callback(new DataApiHandler<NoviceLikes>() {

                @Override
                protected void success(NoviceLikes noviceLikes, IApiResponse response) {
                    if (noviceLikes.increment > 0) {
                        showControls();
                        CacheProfile.completeSetNoviceSympathiesBonus(getActivity());
                        setEnableInputButtons(true);
                    }
                }

                @Override
                protected NoviceLikes parseResponse(ApiResponse response) {
                    return NoviceLikes.parse(response);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                }
            }).exec();
        }
    }

    @Override
    public void lockControls() {
        if (!mIsHide) mDatingCounter.setVisibility(View.GONE);
        mUserInfoStatus.setVisibility(View.GONE);
        mMutualBtn.setEnabled(false);
        if (null != mDelightBtn) {
            mDelightBtn.setEnabled(false);
        }
        mSkipBtn.setEnabled(false);
        mProfileBtn.setEnabled(false);
        if (null != mDatingLoveBtnLayout) {
            mDatingLoveBtnLayout.setEnabled(false);
        }
        setEnableInputButtons(false);
    }

    @Override
    public void unlockControls() {
        if (!mIsHide && !isHideAdmirations) {
            mDatingCounter.setVisibility(View.VISIBLE);
            mUserInfoStatus.setVisibility(View.VISIBLE);
        } else {
            mUserInfoStatus.setVisibility(View.GONE);
        }

        boolean enabled = false;
        if (mCurrentUser != null) {
            enabled = !mCurrentUser.rated;
        }
        mMutualBtn.setEnabled(enabled);
        if (null != mDelightBtn) {
            mDelightBtn.setEnabled(enabled);
        }

        mSkipBtn.setEnabled(true);

        enabled = (mCurrentUser != null);
        mProfileBtn.setEnabled(enabled);

        if (null != mDatingLoveBtnLayout) {
            mDatingLoveBtnLayout.setEnabled(true);
        }

        setEnableInputButtons(true);
    }

    @Override
    public void showControls() {
        mAnimationHelper.animateIn();
        if (mFragmentSwitcherListener != null) {
            mFragmentSwitcherListener.onShowActionBar();
        }
        mIsHide = false;
    }

    @Override
    public void hideControls() {
        mAnimationHelper.animateOut();
        if (mFragmentSwitcherListener != null) {
            mFragmentSwitcherListener.onHideActionBar();
        }
        mIsHide = true;
    }

    @Override
    public void successRate() {
        moneyDecreased.set(false);
        if (App.from(getActivity()).getOptions().isActivityAllowed) {
            if (mCurrentUser != null) {
                mCurrentUser.rated = true;
            }
            showNextUser();
        }
    }

    @Override
    public void failRate() {
        unlockControls();
        if (moneyDecreased.get()) {
            moneyDecreased.set(false);
            updateResources(mBalanceData);
        }
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mImageSwitcher.setVisibility(View.GONE);
            mCurrentUser = null;
            refreshActionBarTitles();
            getTitleSetter().setOnline(false);
            mUserInfoStatus.setText(Utils.EMPTY);
        }
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mRetryBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            lockControls();
            hideEmptySearchDialog();
            if (data != null && data.getExtras() != null) {
                final FilterData filter = data.getParcelableExtra(INTENT_DATING_FILTER);
                FilterRequest filterRequest = new FilterRequest(filter, getActivity());
                registerRequest(filterRequest);
                filterRequest.callback(new FilterHandler()).exec();
                mNewFilter = true;
                FlurryManager.getInstance().sendFilterChangedEvent();
            }
            // открываем чат с пользователем в случае успешной отправки подарка с экрана знакомств
        } else if (resultCode == Activity.RESULT_OK && requestCode == GiftsActivity.INTENT_REQUEST_GIFT) {
            if (mDatingInstantMessageController != null) {
                // открываем чат с пустой строкой в footer
                mDatingInstantMessageController.openChat(getActivity(), mCurrentUser, data != null ? (SendGiftAnswer) data.getParcelableExtra(GiftsActivity.INTENT_SEND_GIFT_ANSWER) : null);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (mAddPhotoHelper != null) {
                mAddPhotoHelper.processActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void hideEmptySearchDialog() {
        mRetryView.setVisibility(View.GONE);
    }

    private void sendAlbumRequest(final Photos data) {
        if (mUserSearchList == null)
            return;
        if ((mLoadedCount - 1) >= data.size())
            return;
        if (data.get(mLoadedCount - 1) == null)
            return;

        int loadedPosition = data.get(mLoadedCount - 1).getPosition() + 1;
        final SearchUser currentSearchUser = mUserSearchList.getCurrentUser();
        if (currentSearchUser != null) {
            AlbumRequest request = new AlbumRequest(getActivity(), currentSearchUser.id, loadedPosition, AlbumRequest.MODE_SEARCH, AlbumLoadController.FOR_PREVIEW);
            final int uid = currentSearchUser.id;
            request.callback(new DataApiHandler<AlbumPhotos>() {
                @Override
                public void success(AlbumPhotos newPhotos, IApiResponse response) {
                    if (uid == mUserSearchList.getCurrentUser().id) {
                        mNeedMore = newPhotos.more;
                        int i = 0;
                        for (Photo photo : newPhotos) {
                            if (mLoadedCount + i < data.size()) {
                                data.set(mLoadedCount + i, photo);
                                i++;
                            }
                        }
                        mLoadedCount += newPhotos.size();

                        if (mImageSwitcher.getSelectedPosition() > mLoadedCount + mController.getItemsOffsetByConnectionType()) {
                            sendAlbumRequest(data);
                        }

                        if (mImageSwitcher != null && mImageSwitcher.getAdapter() != null) {
                            mImageSwitcher.getAdapter().notifyDataSetChanged();
                        }
                    }
                    mCanSendAlbumReq = true;
                }

                @Override
                protected AlbumPhotos parseResponse(ApiResponse response) {
                    return new AlbumPhotos(response);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    mCanSendAlbumReq = true;
                }
            }).exec();
        }
    }

    private void updateResources(BalanceData balance) {
        if (null != mResourcesLikes) {
            mResourcesLikes.setText(String.valueOf(balance.likes));
        }
        if (null != mResourcesMoney) {
            mResourcesMoney.setText(String.valueOf(balance.money));
        }
    }

    private void showEmptySearchDialog() {
        Debug.log("Search:: showEmptySearchDialog");
        EasyTracker.sendEvent("EmptySearch", "Show", "", 0L);
        FlurryManager.getInstance().sendEmptyDatingListEvent();
        mImageSwitcher.setVisibility(View.GONE);
        mRetryView.setVisibility(View.VISIBLE);
        setActionBarTitles(getString(R.string.general_dating));
        getTitleSetter().setOnline(false);
        mFragmentSwitcherListener.onShowActionBar();
        mDatingCounter.setVisibility(View.GONE);
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_dating;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_dating_filter:
                startDatingFilterActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FilterHandler extends DataApiHandler<DatingFilter> {

        @Override
        protected void success(DatingFilter filter, IApiResponse response) {
            App.from(getActivity()).getProfile().dating = filter;
            updateFilterData();
            updateData(false);
        }

        @Override
        protected DatingFilter parseResponse(ApiResponse response) {
            return new DatingFilter(response.getJsonResult());
        }

        @Override
        public void fail(int codeError, IApiResponse response) {
            showEmptySearchDialog();
            Utils.showToastNotification(R.string.general_server_error, Toast.LENGTH_LONG);
        }

        @Override
        public void always(IApiResponse response) {
            super.always(response);
            mNewFilter = false;
        }

        @Override
        public void cancel() {
            super.cancel();
            if (mCurrentUser != null) {
                unlockControls();
            }
        }
    }
}
