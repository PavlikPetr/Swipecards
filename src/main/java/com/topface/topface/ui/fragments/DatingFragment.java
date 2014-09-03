package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.data.AlbumPhotos;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.NoviceLikes;
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Photos;
import com.topface.topface.data.search.CachableSearchList;
import com.topface.topface.data.search.OnUsersListEventsListener;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.FilterRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.NoviceLikesRequest;
import com.topface.topface.requests.SearchRequest;
import com.topface.topface.requests.SendLikeRequest;
import com.topface.topface.requests.SkipRateRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.ui.INavigationFragmentsListener;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.edit.EditAgeFragment;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.FilterFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.KeyboardListenerLayout;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.AnimationHelper;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.LocaleConfig;
import com.topface.topface.utils.Novice;
import com.topface.topface.utils.PreloadManager;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.controllers.DatingInstantMessageController;
import com.topface.topface.utils.social.AuthToken;

import java.util.concurrent.atomic.AtomicBoolean;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker,
        RateController.OnRateControllerListener {

    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 2;

    private TextView mResourcesLikes;
    private TextView mResourcesMoney;
    private Button mDelightBtn;
    private Button mMutualBtn;
    private Button mSkipBtn;
    private Button mProfileBtn;
    private TextView mUserInfoName;
    private TextView mUserInfoCity;
    private TextView mUserInfoStatus;
    private TextView mDatingCounter;
    private TextView mDatingLovePrice;
    private View mDatingResources;
    private View mDatingButtons;
    private View mUserInfo;

    private RateController mRateController;
    private ImageSwitcher mImageSwitcher;
    private CachableSearchList<SearchUser> mUserSearchList;
    private ProgressBar mProgressBar;
    private Novice mNovice;
    private AlphaAnimation mAlphaAnimation;
    private RelativeLayout mDatingLoveBtnLayout;
    private RetryViewCreator mRetryView;
    private ImageButton mRetryBtn;
    private PreloadManager<SearchUser> mPreloadManager;
    private DatingInstantMessageController mDatingInstantMessageController;

    private Drawable singleMutual;
    private Drawable singleDelight;
    private Drawable doubleMutual;
    private Drawable doubleDelight;

    private NoviceLayout mNoviceLayout;

    private boolean hasOneSympathyOrDelight = false;
    private boolean mCanSendAlbumReq = true;
    private SearchUser mCurrentUser;
    /**
     * Флаг того, что запущено обновление поиска и запускать дополнительные обновления не нужно
     */
    private boolean mUpdateInProcess;
    private BroadcastReceiver mProfileReceiver;
    private boolean mNeedMore;
    private int mLoadedCount;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mPreloadManager != null) {
                mPreloadManager.checkConnectionType(intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, 0));
            }
        }
    };

    private BroadcastReceiver mBalanceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateResources();
        }
    };

    private BroadcastReceiver mRateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int likedUserId = intent.getExtras().getInt(RateController.USER_ID_EXTRA);
            if (mCurrentUser != null && likedUserId == mCurrentUser.id) {
                mDelightBtn.setEnabled(false);
                mMutualBtn.setEnabled(false);
                mCurrentUser.rated = true;
            } else if (mUserSearchList != null) {
                for (SearchUser searchUser : mUserSearchList) {
                    if (searchUser.id == likedUserId) {
                        searchUser.rated = true;
                        break;
                    }
                }
            }
        }
    };

    private INavigationFragmentsListener mFragmentSwitcherListener;
    private AnimationHelper mAnimationHelper;

    private void startDatingFilterActivity() {
        Intent intent = new Intent(getActivity().getApplicationContext(),
                EditContainerActivity.class);
        startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER);
    }

    private AtomicBoolean moneyDecreased = new AtomicBoolean(false);

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreloadManager = new PreloadManager<>();
        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);
        initMutualDrawables();
        // Rate Controller
        mRateController = new RateController(getActivity(), SendLikeRequest.Place.FROM_SEARCH);
        mRateController.setOnRateControllerUiListener(this);
        new BackgroundThread() {
            @Override
            public void execute() {
                inBackroundThread();
            }
        };
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mRateReceiver, new IntentFilter(RateController.USER_RATED));
    }

    protected void inBackroundThread() {
        mNovice = App.getNovice();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        KeyboardListenerLayout root = (KeyboardListenerLayout) inflater.inflate(R.layout.fragment_dating, null);

        initViews(root);
        initActionBar();
        initEmptySearchDialog(root);
        initImageSwitcher(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mBalanceReceiver, new IntentFilter(CountersManager.UPDATE_BALANCE));
        LocalBroadcastManager.getInstance(getActivity())
                .registerReceiver(mProfileReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        setHighRatePrice();
        setActionBarTitles(getTitle(), getSubtitle());
        updateResources();
        refreshActionBarTitles();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRateReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRetryView.isVisible()) {
            EasyTracker.sendEvent("EmptySearch", "DismissScreen", "", 0L);
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBalanceReceiver);
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

    private void initViews(final KeyboardListenerLayout root) {
        mRetryBtn = (ImageButton) root.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);

        // Dating controls
        mDatingLoveBtnLayout = (RelativeLayout) root.findViewById(R.id.loDatingLove);

        // User Info
        mUserInfo = root.findViewById(R.id.loUserInfo);
        mUserInfoName = ((TextView) mUserInfo.findViewById(R.id.tvDatingUserName));
        mUserInfoCity = ((TextView) mUserInfo.findViewById(R.id.tvDatingUserCity));
        mUserInfoStatus = ((TextView) mUserInfo.findViewById(R.id.tvDatingUserStatus));

        // Counter
        mDatingCounter = ((TextView) root.findViewById(R.id.tvDatingCounter));

        // Progress
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsDatingLoading);

        initResources(root);
        initControlButtons(root);

        mAnimationHelper = new AnimationHelper(getActivity(), R.anim.fade_in, R.anim.fade_out);
        mAnimationHelper.addView(mDatingCounter);
        mAnimationHelper.addView(mDatingResources);
        mAnimationHelper.addView(mUserInfo);

        mDatingLovePrice = (TextView) root.findViewById(R.id.tvDatingLovePrice);

        mDatingButtons = root.findViewById(R.id.vfDatingButtons);

        Options.InstantMessageFromSearch instantMessageFromSearch = CacheProfile.getOptions().instantMessageFromSearch;
        if (instantMessageFromSearch.enabled) {
            mDatingInstantMessageController = new DatingInstantMessageController(getActivity(), root,
                    this, this, instantMessageFromSearch.text,
                    mDatingButtons, mUserInfoStatus);
        }
    }

    private void initMutualDrawables() {
        if (isAdded()) {
            singleMutual = getResources().getDrawable(R.drawable.dating_like_selector);
            singleDelight = getResources().getDrawable(R.drawable.dating_admiration_selector);

            doubleMutual = getResources().getDrawable(R.drawable.dating_mutual_selector);
            doubleDelight = getResources().getDrawable(R.drawable.dating_dbl_admiration_selector);
        }
    }

    private void setHighRatePrice() {
        // Dating Love Price
        final int delightPrice = CacheProfile.getOptions().priceAdmiration;
        if (delightPrice > 0) {
            mDatingLovePrice.setText(Integer.toString(CacheProfile.getOptions().priceAdmiration));
        } else {
            mDatingLovePrice.setVisibility(View.GONE);
        }
    }

    private void initImageSwitcher(View view) {
        // Dating Album
        mImageSwitcher = ((ImageSwitcher) view.findViewById(R.id.glrDatingAlbum));
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
        mDelightBtn = (Button) view.findViewById(R.id.btnDatingAdmiration);
        mDelightBtn.setOnClickListener(this);
        mMutualBtn = (Button) view.findViewById(R.id.btnDatingSympathy);
        mMutualBtn.setOnClickListener(this);
        mSkipBtn = (Button) view.findViewById(R.id.btnDatingSkip);
        mSkipBtn.setOnClickListener(this);
        mProfileBtn = (Button) view.findViewById(R.id.btnDatingProfile);
        mProfileBtn.setOnClickListener(this);
    }

    private void initActionBar() {
        // Navigation Header
        setActionBarTitles(getTitle(), getSubtitle());
    }

    protected String getTitle() {
        if (CacheProfile.dating != null) {
            int age = CacheProfile.dating.ageEnd == DatingFilter.MAX_AGE ?
                    EditAgeFragment.absoluteMax : CacheProfile.dating.ageEnd;
            String headerText = getString(CacheProfile.dating.sex == Static.BOY ?
                            R.string.dating_header_guys : R.string.dating_header_girls,
                    CacheProfile.dating.ageStart, age
            );
            String plus = CacheProfile.dating.ageEnd == DatingFilter.MAX_AGE ? "+" : "";
            return headerText + plus;
        }
        return Static.EMPTY;
    }

    protected String getSubtitle() {
        if (CacheProfile.dating != null) {
            String cityString = CacheProfile.dating.city == null || CacheProfile.dating.city.isEmpty() ?
                    getString(R.string.filter_cities_all) : CacheProfile.dating.city.name;
            String onlineString = DatingFilter.getOnlyOnlineField() ? getString(R.string.dating_online_only) : "%s";
            return String.format(onlineString, cityString);
        }
        return Static.EMPTY;
    }

    private void initEmptySearchDialog(View view) {
        mRetryView = RetryViewCreator.createDefaultRetryView(
                getActivity(),
                /* Первая кнопка - "Попробовать еще раз" */
                getString(R.string.general_search_null_response_error),
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EasyTracker.sendEvent("EmptySearch", "ClickTryAgain", "", 0L);
                        updateData(false);
                    }
                },
                /* Вторая кнопка - "Изменить фильтр" */
                getString(R.string.change_filters),
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EasyTracker.sendEvent("EmptySearch", "ClickChangeFilter", "", 0L);
                        startDatingFilterActivity();
                    }
                },
                LinearLayout.VERTICAL
        );

        hideEmptySearchDialog();
        ((RelativeLayout) view.findViewById(R.id.ac_dating_container)).addView(mRetryView.getView());

        mProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isAdded()) {
                    updateFilterData();
                    setHighRatePrice();
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
        setActionBarTitles(getTitle(), getSubtitle());
    }

    private void updateData(final boolean isAddition) {
        if (!mUpdateInProcess) {
            lockControls();
            hideEmptySearchDialog();
            if (!isAddition) {
                onUpdateStart(false);
            }

            mUpdateInProcess = true;

            UsersList.log("Update start: " + (isAddition ? "addition" : "replace"));

            getSearchRequest().callback(new DataApiHandler<UsersList>() {

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
                        } else if (!mUserSearchList.isEnded()) {
                            unlockControls();
                        } else {
                            showEmptySearchDialog();
                        }

                        //Скрываем кнопку отправки повтора
                        mRetryBtn.setVisibility(View.GONE);
                    } else {
                        mProgressBar.setVisibility(View.GONE);
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

            updateFilterData();
        }
    }

    private SearchRequest getSearchRequest() {
        SearchRequest searchRequest = new SearchRequest(getFilterOnlyOnline(), getActivity());
        registerRequest(searchRequest);
        return searchRequest;
    }

    private boolean getFilterOnlyOnline() {
        return DatingFilter.getOnlyOnlineField();
    }

    AtomicBoolean isAdmirationFailed = new AtomicBoolean(false);

    @Override
    public void onClick(View view) {
        if (!CacheProfile.isLoaded()) {
            return;
        }
        switch (view.getId()) {
            case R.id.loDatingResources: {
                EasyTracker.sendEvent("Dating", "BuyClick", "", 1L);
                startActivity(PurchasesActivity.createBuyingIntent("Dating"));
            }
            break;
            case R.id.btnDatingAdmiration: {
                if (mCurrentUser != null) {
                    lockControls();
                    isAdmirationFailed.set(false);
                    boolean canSendAdmiration = mRateController.onAdmiration(
                            mCurrentUser.id,
                            mCurrentUser.isMutualPossible ?
                                    SendLikeRequest.DEFAULT_MUTUAL
                                    : SendLikeRequest.DEFAULT_NO_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @Override
                                public void onRateCompleted(int mutualId) {
                                    isAdmirationFailed.set(true);
                                    EasyTracker.sendEvent("Dating", "Rate",
                                            "AdmirationSend" + (mutualId == SendLikeRequest.DEFAULT_MUTUAL ? "mutual" : ""),
                                            (long) CacheProfile.getOptions().priceAdmiration);
                                }

                                @Override
                                public void onRateFailed(int userId, int mutualId) {
                                    if (moneyDecreased.get()) {
                                        moneyDecreased.set(false);
                                        CacheProfile.money += CacheProfile.getOptions().priceAdmiration;
                                        updateResources();
                                        new SendLikeRequest(getActivity(),
                                                userId,
                                                mutualId,
                                                SendLikeRequest.Place.FROM_SEARCH).callback(new SimpleApiHandler() {
                                            @Override
                                            public void fail(int codeError, IApiResponse response) {
                                                super.fail(codeError, response);
                                                unlockControls();
                                            }
                                        }).exec();
                                    } else {
                                        isAdmirationFailed.set(true);
                                        unlockControls();
                                    }
                                }
                            }
                    );
                    if (canSendAdmiration && !isAdmirationFailed.get()) {
                        CacheProfile.money = CacheProfile.money - CacheProfile.getOptions().priceAdmiration;
                        moneyDecreased.set(true);
                        updateResources();
                    }
                }
            }
            break;
            case R.id.btnDatingSympathy: {
                if (mCurrentUser != null) {
                    lockControls();
                    mRateController.onLike(mCurrentUser.id,
                            mCurrentUser.isMutualPossible ?
                                    SendLikeRequest.DEFAULT_MUTUAL
                                    : SendLikeRequest.DEFAULT_NO_MUTUAL,
                            new RateController.OnRateRequestListener() {
                                @Override
                                public void onRateCompleted(int mutualId) {

                                }

                                @Override
                                public void onRateFailed(int userId, int mutualId) {
                                    mCurrentUser.rated = false;
                                    unlockControls();
                                }
                            }
                    );
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
                    Intent intent = UserProfileActivity.createIntent(mCurrentUser.id, DatingFragment.class, getActivity());
                    intent.putExtra(UserProfileFragment.USER_RATED_EXTRA, mCurrentUser.rated);
                    startActivityForResult(intent, UserProfileActivity.INTENT_USER_PROFILE);
                    EasyTracker.sendEvent("Dating", "Additional", "Profile", 1L);
                }
            }
            break;
            case R.id.btnUpdate: {
                updateData(false);
                mRetryBtn.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            }
            break;
            case R.id.btnSend: {
                mDatingInstantMessageController.instantSend(mCurrentUser);
            }
            break;
            case R.id.send_gift_button: {
                startActivityForResult(
                        GiftsActivity.getSendGiftIntent(getActivity(), mCurrentUser.id),
                        GiftsActivity.INTENT_REQUEST_GIFT
                );
                EasyTracker.sendEvent("Dating", "SendGiftClick", "", 1L);
            }
            break;
            case R.id.chat_btn: {
                mDatingInstantMessageController.openChat(getActivity(), mCurrentUser);
            }
            break;
            default:
        }
    }



    private void showUser(SearchUser user) {
        if (user != null) {
            hideEmptySearchDialog();
            fillUserInfo(user);
            unlockControls();
            showNovice();
            hasOneSympathyOrDelight = true;
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
        lockControls();

        if (currUser.city != null) {
            mUserInfoCity.setText(currUser.city.name);
        }
        //Устанавливаем статус пользователя.
        mUserInfoStatus.setText(currUser.getStatus());
        //Имя и возраст пользователя
        mUserInfoName.setText(currUser.getNameAndAge());

        Resources res = getResources();

        setUserOnlineStatus(currUser, res);
        setUserSex(currUser, res);
        setLikeButtonDrawables(currUser);
        setUserPhotos(currUser);

        mImageSwitcher.setData(currUser.photos);
        mImageSwitcher.setCurrentItem(0, true);
        setCounter(0);
    }

    private void setUserOnlineStatus(SearchUser currUser, Resources res) {
        if (currUser.online) {
            mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(
                    res.getDrawable(R.drawable.im_online), null, null, null);
        } else {
            mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(
                    res.getDrawable(R.drawable.im_offline), null, null, null);
        }
    }

    private void setUserSex(SearchUser currUser, Resources res) {
        if (currUser.sex == Static.BOY) {
            mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, res
                    .getDrawable(R.drawable.dating_man_selector), null, null);
        } else if (currUser.sex == Static.GIRL) {
            mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, res
                    .getDrawable(R.drawable.dating_woman_selector), null, null);
        }
    }

    private void setLikeButtonDrawables(SearchUser currUser) {
        // buttons drawables
        mMutualBtn.setCompoundDrawablesWithIntrinsicBounds(null, currUser.isMutualPossible ? doubleMutual
                : singleMutual, null, null);
        mMutualBtn.setText(currUser.isMutualPossible ? App.getContext().getString(R.string.general_mutual)
                : App.getContext().getString(R.string.general_sympathy));

        mDelightBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                currUser.isMutualPossible ? doubleDelight : singleDelight, null, null);
    }

    private void setUserPhotos(SearchUser currUser) {
        // photos
        mLoadedCount = currUser.photos.getRealPhotosCount();
        mNeedMore = currUser.photosCount > mLoadedCount;
        int rest = currUser.photosCount - currUser.photos.size();

        for (int i = 0; i < rest; i++) {
            currUser.photos.add(new Photo());
        }
    }

    private void skipUser(SearchUser currentSearch) {
        if (currentSearch != null && !currentSearch.skipped && !currentSearch.rated) {
            SkipRateRequest skipRateRequest = new SkipRateRequest(getActivity());
            registerRequest(skipRateRequest);
            skipRateRequest.userid = currentSearch.id;
            skipRateRequest.callback(new SimpleApiHandler() {

                @Override
                public void success(IApiResponse response) {
                    mCurrentUser.skipped = true;
                }
            }).exec();
        }
    }

    private void showNovice() {
        if (mNovice == null) return;
        if (!isAdded()) return;

        if (mNovice.isDatingCompleted())
            return;

        if (mDatingInstantMessageController != null) {
            mDatingInstantMessageController.setEnabled(false);
        }

        if (mNoviceLayout == null) {
            mNoviceLayout = new NoviceLayout(getActivity());
            mNoviceLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mNoviceLayout.setVisibility(View.GONE);
            ((ViewGroup) getView().findViewById(R.id.ac_dating_container)).addView(mNoviceLayout);
        }

        if (mNovice.isShowSympathy()) {
            showControls();
            OnClickListener completeShowSympathylistener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNovice.completeShowSympathy();
                    if (mDatingInstantMessageController != null) {
                        mDatingInstantMessageController.setEnabled(true);
                    }
                }
            };
            mNoviceLayout.setLayoutRes(R.layout.novice_sympathy, completeShowSympathylistener,
                    completeShowSympathylistener);
            mNoviceLayout.startAnimation(mAlphaAnimation);
        } else if (mNovice.isShowSympathiesBonus()) {
            NoviceLikesRequest noviceLikesRequest = new NoviceLikesRequest(getActivity());
            registerRequest(noviceLikesRequest);
            noviceLikesRequest.callback(new DataApiHandler<NoviceLikes>() {

                @Override
                protected void success(NoviceLikes noviceLikes, IApiResponse response) {
                    if (noviceLikes.increment > 0) {
                        showControls();
                        Novice.giveNoviceLikesQuantity = noviceLikes.increment;
                        updateResources();
                        final String text = String.format(
                                getResources().getString(R.string.novice_sympathies_bonus),
                                Novice.giveNoviceLikesQuantity,
                                Novice.giveNoviceLikesQuantity
                        );
                        OnClickListener completeShowSympathiesBonusListener = new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mNovice.completeShowNoviceSympathiesBonus();
                                if (mDatingInstantMessageController != null) {
                                    mDatingInstantMessageController.setEnabled(true);
                                }
                            }
                        };
                        mNoviceLayout.setLayoutRes(
                                R.layout.novice_sympathies_bonus,
                                completeShowSympathiesBonusListener,
                                completeShowSympathiesBonusListener,
                                text
                        );
                        mNoviceLayout.startAnimation(mAlphaAnimation);
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
        } else if (hasOneSympathyOrDelight
                && CacheProfile.likes <= Novice.MIN_LIKES_QUANTITY
                && mNovice.isShowBuySympathies()) {
            showControls();
            mNoviceLayout.setLayoutRes(
                    R.layout.novice_buy_sympathies,
                    new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mNovice.completeShowBuySympathies();
                            mDatingResources.performClick();
                            if (mDatingInstantMessageController != null) {
                                mDatingInstantMessageController.setEnabled(true);
                            }
                        }
                    }, new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mNovice.completeShowBuySympathies();
                            if (mDatingInstantMessageController != null) {
                                mDatingInstantMessageController.setEnabled(true);
                            }
                        }
                    }
            );
            mNoviceLayout.startAnimation(mAlphaAnimation);
        }
    }

    public void setCounter(int position) {
        if (mCurrentUser != null) {
            mDatingCounter.setText((position + 1) + "/" + mCurrentUser.photos.size());
            if (!mIsHide) mDatingCounter.setVisibility(View.VISIBLE);
        } else {
            mDatingCounter.setText("-/-");
            mDatingCounter.setVisibility(View.GONE);
        }
    }

    @Override
    public void lockControls() {
        mProgressBar.setVisibility(View.VISIBLE);
        if (!mIsHide) mDatingCounter.setVisibility(View.GONE);
        mUserInfoName.setVisibility(View.GONE);
        mUserInfoCity.setVisibility(View.GONE);
        mUserInfoStatus.setVisibility(View.GONE);
        mMutualBtn.setEnabled(false);
        mDelightBtn.setEnabled(false);
        mSkipBtn.setEnabled(false);
        mProfileBtn.setEnabled(false);
        mDatingLoveBtnLayout.setEnabled(false);
        if (mDatingInstantMessageController != null) {
            mDatingInstantMessageController.setEnabled(false);
        }
    }

    @Override
    public void unlockControls() {
        mProgressBar.setVisibility(View.GONE);
        if (!mIsHide) mDatingCounter.setVisibility(View.VISIBLE);
        mUserInfoName.setVisibility(mCurrentUser != null ? View.VISIBLE : View.GONE);
        mUserInfoCity.setVisibility(mCurrentUser != null ? View.VISIBLE : View.GONE);
        mUserInfoStatus.setVisibility(View.VISIBLE);

        boolean enabled = false;
        if (mCurrentUser != null) {
            enabled = !mCurrentUser.rated;
        }
        mMutualBtn.setEnabled(enabled);
        mDelightBtn.setEnabled(enabled);

        mSkipBtn.setEnabled(true);

        enabled = (mCurrentUser != null);
        mProfileBtn.setEnabled(enabled);

        mDatingLoveBtnLayout.setEnabled(true);

        if (mNoviceLayout == null || mNoviceLayout.getVisibility() == View.GONE) {
            if (mDatingInstantMessageController != null) {
                mDatingInstantMessageController.setEnabled(true);
            }
        }
    }

    @Override
    public void showControls() {
        mAnimationHelper.animateIn();
        mFragmentSwitcherListener.onShowActionBar();
        mIsHide = false;
    }

    @Override
    public void hideControls() {
        mAnimationHelper.animateOut();
        mFragmentSwitcherListener.onHideActionBar();
        mIsHide = true;
    }

    @Override
    public void successRate() {
        moneyDecreased.set(false);
        if (CacheProfile.getOptions().isActivityAllowed) {
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
            updateResources();
        }
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mProgressBar.setVisibility(View.VISIBLE);
            mImageSwitcher.setVisibility(View.GONE);
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
            hideEmptySearchDialog();
            if (data != null && data.getExtras() != null) {
                final DatingFilter filter = data.getExtras().getParcelable(FilterFragment.INTENT_DATING_FILTER);
                FilterRequest filterRequest = new FilterRequest(filter, getActivity());
                registerRequest(filterRequest);
                filterRequest.callback(new DataApiHandler<DatingFilter>() {

                    @Override
                    protected void success(DatingFilter filter, IApiResponse response) {
                        CacheProfile.dating = filter;
                        updateFilterData();
                        updateData(false);
                    }

                    @Override
                    protected DatingFilter parseResponse(ApiResponse response) {
                        return new DatingFilter(response.getJsonResult());
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Toast.makeText(getActivity(), R.string.general_server_error, Toast.LENGTH_LONG).show();
                        unlockControls();
                    }
                }).exec();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void hideEmptySearchDialog() {
        mRetryView.setVisibility(View.GONE);
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

            if (position + DEFAULT_PRELOAD_ALBUM_RANGE == (mLoadedCount - 1)) {
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

    private void sendAlbumRequest(final Photos data) {
        sendAlbumRequest(data, true);
    }

    private void sendAlbumRequest(final Photos data, boolean defaultLoading) {
        if (mUserSearchList == null)
            return;
        if ((mLoadedCount - 1) >= data.size())
            return;
        if (data.get(mLoadedCount - 1) == null)
            return;

        int loadedPosition = data.get(mLoadedCount - 1).getPosition() + 1;
        final SearchUser currentSearchUser = mUserSearchList.getCurrentUser();
        if (currentSearchUser != null) {
            int limit = defaultLoading ? ViewUsersListFragment.PHOTOS_LIMIT : getCurrentPhotosLimit();
            AlbumRequest request = new AlbumRequest(getActivity(), currentSearchUser.id,
                    limit, loadedPosition, AlbumRequest.MODE_SEARCH);
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

                        if (mImageSwitcher.getSelectedPosition() > mLoadedCount + DEFAULT_PRELOAD_ALBUM_RANGE) {
                            sendAlbumRequest(data, false);
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

    private int getCurrentPhotosLimit() {
        int limit = mImageSwitcher.getSelectedPosition() - mLoadedCount + DEFAULT_PRELOAD_ALBUM_RANGE;
        return limit > AlbumRequest.DEFAULT_PHOTOS_LIMIT ? AlbumRequest.DEFAULT_PHOTOS_LIMIT : limit;
    }

    private void updateResources() {
        mResourcesLikes.setText(Integer.toString(CacheProfile.likes));
        mResourcesMoney.setText(Integer.toString(CacheProfile.money));
    }

    private OnUsersListEventsListener mSearchListener = new OnUsersListEventsListener() {
        @Override
        public void onEmptyList(UsersList usersList) {
            lockControls();
            updateData(false);

        }

        @Override
        public void onPreload(UsersList usersList) {
            updateData(true);
        }

    };

    private void showEmptySearchDialog() {
        Debug.log("Search:: showEmptySearchDialog");
        EasyTracker.sendEvent("EmptySearch", "Show", "", 0L);
        mProgressBar.setVisibility(View.GONE);
        mImageSwitcher.setVisibility(View.GONE);
        mRetryView.setVisibility(View.VISIBLE);
        mFragmentSwitcherListener.onShowActionBar();
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
}
