package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.*;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.RetryRequestReceiver;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.data.search.CachableSearchList;
import com.topface.topface.data.search.OnUsersListEventsListener;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.data.search.UsersList;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditAgeFragment;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.FilterFragment;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.*;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker,
        RateController.OnRateControllerListener {

    public static final int SEARCH_LIMIT = 30;
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 2;

    private TextView mResourcesLikes;
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
    private TextView mDatingLovePrice;
    private View mDatingGroup;
    private View mDatingResources;

    private RateController mRateController;
    private ImageSwitcher mImageSwitcher;
    private CachableSearchList<SearchUser> mUserSearchList;
    private ProgressBar mProgressBar;
    private Novice mNovice;
    private AlphaAnimation mAlphaAnimation;
    private RelativeLayout mDatingLoveBtnLayout;
    private ViewFlipper mViewFlipper;
    private RetryViewCreator mRetryView;
    private ImageButton mRetryBtn;
    private PreloadManager<SearchUser> mPreloadManager;

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

    private OnClickListener mSettingsListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(getActivity().getApplicationContext(),
                    EditContainerActivity.class);
            startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreloadManager = new PreloadManager<SearchUser>();
        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);
        initMutualDrawables();
        // Rate Controller
        mRateController = new RateController(getActivity());
        mRateController.setOnRateControllerListener(this);
    }

    @Override
    protected void inBackroundThread() {
        super.inBackroundThread();
        SharedPreferences preferences = getActivity().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        mNovice = Novice.getInstance(preferences);
        showPromoDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        View root = inflater.inflate(R.layout.ac_dating, null);

        getActionBar(root);
        initViews(root);
        initActionBar(root);
        initEmptySearchDialog(root, mSettingsListener);
        initImageSwitcher(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        setHighRatePrice();
        updateResources();
        refreshActionBarTitles(getView());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        //При выходе из фрагмента сохраняем кэш поиска
        if (mUserSearchList != null) {
            if (LocaleConfig.localeChangeInitiated) {
                mUserSearchList.saveCurrentInCache();
            } else {
                mUserSearchList.saveCache();
            }
        }
    }

    private void initViews(View root) {
        mRetryBtn = (ImageButton) root.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);

        mViewFlipper = (ViewFlipper) root.findViewById(R.id.vfFlipper);

        // Dating controls
        mDatingGroup = root.findViewById(R.id.loDatingGroup);
        mDatingLoveBtnLayout = (RelativeLayout) root.findViewById(R.id.loDatingLove);

        // User Info
        mUserInfoName = ((TextView) root.findViewById(R.id.tvDatingUserName));
        mUserInfoCity = ((TextView) root.findViewById(R.id.tvDatingUserCity));
        mUserInfoStatus = ((TextView) root.findViewById(R.id.tvDatingUserStatus));

        // Counter
        mCounter = ((TextView) root.findViewById(R.id.tvDatingCounter));

        // Progress
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsDatingLoading);

        initResources(root);
        initControlButtons(root);

        mDatingLovePrice = (TextView) root.findViewById(R.id.tvDatingLovePrice);
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
        final int delightPrice = CacheProfile.getOptions().price_highrate;
        if (delightPrice > 0) {
            mDatingLovePrice.setText(Integer.toString(CacheProfile.getOptions().price_highrate));
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
        updateResources();
    }

    private void initControlButtons(View view) {
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
    }

    private void initActionBar(View view) {
        // Navigation Header
        ActionBar actionBar = getActionBar(view);
        refreshActionBarTitles(view);
        final Activity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            actionBar.showHomeButton((NavigationActivity) activity);
        }
        actionBar.showSettingsButton(mSettingsListener, false);
    }

    private void refreshActionBarTitles(View view) {
        getActionBar(view).setTitleText(getTitle());
        getActionBar(view).setSubTitleText(getSubtitle());
    }

    private String getTitle() {
        if (CacheProfile.dating != null) {
            int age = CacheProfile.dating.age_end == DatingFilter.webAbsoluteMaxAge ?
                    EditAgeFragment.absoluteMax : CacheProfile.dating.age_end;
            String headerText = getString(CacheProfile.dating.sex == Static.BOY ?
                    R.string.dating_header_guys : R.string.dating_header_girls,
                    CacheProfile.dating.age_start, age);
            String plus = CacheProfile.dating.age_end == DatingFilter.webAbsoluteMaxAge ? "+" : "";
            return headerText + plus;
        }
        return Static.EMPTY;
    }

    private String getSubtitle() {
        if (CacheProfile.dating != null) {
            String cityString = CacheProfile.dating.city == null || CacheProfile.dating.city.isEmpty() ?
                    getString(R.string.filter_cities_all) : CacheProfile.dating.city.name;
            String onlineString = DatingFilter.getOnlineField() ? getString(R.string.dating_online_only) : "%s";
            return String.format(onlineString, cityString);
        }
        return Static.EMPTY;
    }

    private void initEmptySearchDialog(View view, OnClickListener settingsListener) {
        String text = getString(R.string.general_search_null_response_error);
        mRetryView = RetryViewCreator.createDefaultRetryView(getActivity(), text, new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(false);
            }
        }, getString(R.string.change_filters), settingsListener);

        hideEmptySearchDialog();
        ((RelativeLayout) view.findViewById(R.id.ac_dating_container)).addView(mRetryView.getView());

        mProfileReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateFilterData();
                updateResources();
            }
        };
    }

    private void updateFilterData() {
        //Если изменился фильтр, то мы ставим новую подпись фильтра,
        //если она отличается от той, что в поиске, то поиск будет очищен
        if (mUserSearchList != null) {
            mUserSearchList.updateSignatureAndUpdate();
        }
        refreshActionBarTitles(getView());
    }

    private void updateData(final boolean isAddition) {
        if (!mUpdateInProcess) {
            lockControls();
            hideEmptySearchDialog();
            if (!isAddition) {
                onUpdateStart(isAddition);
            }

            mUpdateInProcess = true;

            UsersList.log("Update start: " + (isAddition ? "addition" : "replace"));

            getSearchRequest().callback(new DataApiHandler<UsersList>() {

                @Override
                protected void success(UsersList usersList, ApiResponse response) {
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
                        } else if (!isAddition || mUserSearchList.isEmpty()) {
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
                public void fail(int codeError, ApiResponse response) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        UsersList.log("load error: " + response.message);
                        Toast.makeText(activity, App.getContext().getString(R.string.general_data_error),
                                Toast.LENGTH_SHORT).show();
                        onUpdateFail(isAddition);
                        unlockControls();
                    }
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    mUpdateInProcess = false;
                }
            }).exec();
        }
    }

    @Override
    protected void onLoadProfile() {
        if (mUserSearchList == null) {
            mUserSearchList = new CachableSearchList<SearchUser>(SearchUser.class);
            mUserSearchList.setOnEmptyListListener(mSearchListener);
        }
        //Показываем последнего пользователя
        if (mCurrentUser == null) {
            SearchUser currentUser = mUserSearchList.getCurrentUser();
            if (currentUser != null) {
                showUser(currentUser);
            } else {
                showNextUser();
            }
        }

        updateFilterData();
        updateResources();
    }

    private SearchRequest getSearchRequest() {
        SearchRequest searchRequest = new SearchRequest(getActivity());
        searchRequest.limit = SEARCH_LIMIT;
        searchRequest.online = getFilterOnline();
        registerRequest(searchRequest);
        return searchRequest;
    }

    private boolean getFilterOnline() {
        return DatingFilter.getOnlineField();
    }

    @Override
    public void onClick(View view) {
        if (!CacheProfile.isLoaded()) {
            return;
        }
        switch (view.getId()) {
            case R.id.loDatingResources: {
                EasyTracker.getTracker().trackEvent("Dating", "BuyClick", "", 1L);
                startActivity(ContainerActivity.getBuyingIntent("Dating"));
            }
            break;
            case R.id.btnDatingLove: {
                if (mCurrentUser != null) {
                    if (mUserSearchList == null || mUserSearchList.isEnded()) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        if (CacheProfile.money > 0) {
                            CacheProfile.money = CacheProfile.money - CacheProfile.getOptions().price_highrate;
                        }
                        mRateController.onRate(mCurrentUser.id, 10,
                                mCurrentUser.mutual ? RateRequest.DEFAULT_MUTUAL
                                        : RateRequest.DEFAULT_NO_MUTUAL, null);

                        EasyTracker.getTracker().trackEvent("Dating", "Rate",
                                "AdmirationSend" + (mCurrentUser.mutual ? "mutual" : ""),
                                (long) CacheProfile.getOptions().price_highrate);
                    }
                    //currentSearch.rated = true;
                }
            }
            break;
            case R.id.btnDatingSympathy: {
                if (mCurrentUser != null) {
                    if (mUserSearchList == null || mUserSearchList.isEnded()) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        mRateController.onRate(mCurrentUser.id, 9,
                                mCurrentUser.mutual ? RateRequest.DEFAULT_MUTUAL
                                        : RateRequest.DEFAULT_NO_MUTUAL, null);

                        EasyTracker.getTracker().trackEvent("Dating", "Rate",
                                "SympathySend" + (mCurrentUser.mutual ? "mutual" : ""), 0L);
                    }
                    //currentSearch.rated = true;
                }
            }
            break;
            case R.id.btnDatingSkip: {
                skipUser(mCurrentUser);
                if (mCurrentUser != null) {
                    mCurrentUser.skipped = true;

                    EasyTracker.getTracker().trackEvent("Dating", "Rate", "Skip", 0L);
                }
                showNextUser();
            }
            break;
            case R.id.btnDatingPrev: {
                prevUser();
                EasyTracker.getTracker().trackEvent("Dating", "Additional", "Prev", 0L);
            }

            break;
            case R.id.btnDatingProfile: {
                if (mCurrentUser != null && getActivity() != null) {
                    getActivity().startActivity(ContainerActivity.getProfileIntent(mCurrentUser.id, DatingFragment.class, getActivity()));
                    EasyTracker.getTracker().trackEvent("Dating", "Additional", "Profile", 1L);
                }
            }
            break;
            case R.id.btnDatingChat: {
                if (CacheProfile.premium || !CacheProfile.getOptions().block_chat_not_mutual) {
                    openChat(getActivity());
                } else {
                    chatBlockLogic();
                }
            }
            break;
            case R.id.btnDatingSwitchNext: {
                mViewFlipper.setDisplayedChild(1);
                EasyTracker.getTracker().trackEvent("Dating", "Additional", "Switch", 1L);
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
            break;
            default:
        }
    }

    private void chatBlockLogic() {
        if (mCurrentUser.mutual) {
            openChat(getActivity());
        } else {
            startActivityForResult(
                    ContainerActivity.getVipBuyIntent(
                            getString(R.string.chat_block_not_mutual),
                            "DatingChatLock"
                    ),
                    ContainerActivity.INTENT_BUY_VIP_FRAGMENT
            );
        }
    }

    private void openChat(FragmentActivity activity) {
        Intent intent = new Intent(activity, ContainerActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, mCurrentUser.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, mCurrentUser.first_name);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, mCurrentUser.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, mCurrentUser.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, mCurrentUser.city.name);
        intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, getClass().getSimpleName());
        activity.startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
        EasyTracker.getTracker().trackEvent("Dating", "Additional", "Chat", 1L);
    }

    private void showUser(SearchUser user) {
        if (user != null) {
            hideEmptySearchDialog();
            fillUserInfo(user);
            unlockControls();

            showNovice();
            hasOneSympathyOrDelight = true;
        }

        mPreloadManager.preloadPhoto(mUserSearchList);
        updateResources();
    }

    private void showNextUser() {
        showUser(mUserSearchList.nextUser());
    }

    private void prevUser() {
        fillUserInfo(mUserSearchList.prevUser());
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
        mMutualBtn.setCompoundDrawablesWithIntrinsicBounds(null, currUser.mutual ? doubleMutual
                : singleMutual, null, null);
        mMutualBtn.setText(currUser.mutual ? App.getContext().getString(R.string.general_mutual)
                : App.getContext().getString(R.string.general_sympathy));

        mDelightBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                currUser.mutual ? doubleDelight : singleDelight, null, null);
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
            skipRateRequest.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    SkipRate skipRate = SkipRate.parse(response);
                    if (skipRate.completed) {
                        CacheProfile.likes = skipRate.likes;
                        CacheProfile.money = skipRate.money;
                        updateResources();
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {

                }
            }).exec();
        }
    }

    private void showNovice() {
        if (mNovice == null) return;
        if (!isAdded()) return;

        if (mNovice.isDatingCompleted())
            return;

        if (mNoviceLayout == null) {
            mNoviceLayout = new NoviceLayout(getActivity());
            mNoviceLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mNoviceLayout.setVisibility(View.GONE);
            ((ViewGroup) getView().findViewById(R.id.ac_dating_container)).addView(mNoviceLayout);
        }

        if (mNovice.isShowEnergyToSympathies()) {
            mNoviceLayout.setLayoutRes(R.layout.novice_energy_to_sympathies, null,
                    getResources().getString(CacheProfile.sex == Static.BOY ?
                            R.string.novice_energy_to_sympathies_message_girls :
                            R.string.novice_energy_to_sympathies_message_boys));
            mNoviceLayout.startAnimation(mAlphaAnimation);
            mNovice.completeShowEnergyToSympathies();
        } else if (mNovice.isShowSympathy()) {
            mNoviceLayout.setLayoutRes(R.layout.novice_sympathy, null);
            mNoviceLayout.startAnimation(mAlphaAnimation);
            mNovice.completeShowSympathy();
        } else if (mNovice.isShowSympathiesBonus()) {
            mResourcesLikes.setText(getResources().getString(R.string.default_resource_value));
            NoviceLikesRequest noviceLikesRequest = new NoviceLikesRequest(getActivity());
            registerRequest(noviceLikesRequest);
            noviceLikesRequest.callback(new DataApiHandler<NoviceLikes>() {

                @Override
                protected void success(NoviceLikes noviceLikes, ApiResponse response) {
                    CacheProfile.likes = noviceLikes.likes;
                    if (noviceLikes.increment > 0) {
                        Novice.giveNoviceLikesQuantity = noviceLikes.increment;
                        final String text = String.format(
                                getResources().getString(R.string.novice_sympathies_bonus),
                                Novice.giveNoviceLikesQuantity,
                                Novice.giveNoviceLikesQuantity
                        );
                        mResourcesLikes.setText(Integer.toString(CacheProfile.likes));
                        mNoviceLayout.setLayoutRes(R.layout.novice_sympathies_bonus, null,
                                null, text);
                        mNoviceLayout.startAnimation(mAlphaAnimation);
                        mNovice.completeShowBatteryBonus();
                    }
                }

                @Override
                protected NoviceLikes parseResponse(ApiResponse response) {
                    return NoviceLikes.parse(response);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                }

            }).exec();
        } else if (mNovice.isShowBuySympathies() && hasOneSympathyOrDelight && CacheProfile.likes <= Novice.MIN_LIKES_QUANTITY) {
            mNoviceLayout.setLayoutRes(R.layout.novice_energy, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mDatingResources.performClick();
                }
            });
            mNoviceLayout.startAnimation(mAlphaAnimation);
            mNovice.completeShowBuySympathies();
        }
    }

    public void setCounter(int position) {
        if (mCurrentUser != null) {
            mCounter.setText((position + 1) + "/" + mCurrentUser.photos.size());
            mCounter.setVisibility(View.VISIBLE);
        } else {
            mCounter.setText("-/-");
            mCounter.setVisibility(View.GONE);
        }
    }

    @Override
    public void lockControls() {
        mProgressBar.setVisibility(View.VISIBLE);
        mCounter.setVisibility(View.GONE);
        mUserInfoName.setVisibility(View.GONE);
        mUserInfoCity.setVisibility(View.GONE);
        mUserInfoStatus.setVisibility(View.GONE);
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
        mProgressBar.setVisibility(View.GONE);
        mCounter.setVisibility(View.VISIBLE);
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
        mPrevBtn.setEnabled(mUserSearchList.isHasRated());

        enabled = (mCurrentUser != null);
        mProfileBtn.setEnabled(enabled);
        mChatBtn.setEnabled(enabled);

        mDatingLoveBtnLayout.setEnabled(true);
        mSwitchNextBtn.setEnabled(true);
        mSwitchPrevBtn.setEnabled(true);
    }

    @Override
    public void showControls() {
        getActionBar(getView()).show();
        mDatingGroup.setVisibility(View.VISIBLE);
        mIsHide = false;
    }

    @Override
    public void hideControls() {
        mDatingGroup.setVisibility(View.GONE);
        getActionBar(getView()).hide();
        mIsHide = true;
    }

    @Override
    public void successRate() {
        if (mCurrentUser != null) {
            mCurrentUser.rated = true;
        }
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
                filterRequest.callback(new ApiHandler() {

                    @Override
                    public void success(ApiResponse response) {
                        if (response.isCompleted()) {
                            CacheProfile.dating = new DatingFilter(response.getJsonResult().optJSONObject("dating"));
                            updateFilterData();
                            updateData(false);
                        } else {
                            fail(response.getResultCode(), response);
                        }
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
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
        private boolean isAfterLast = false;

        @Override
        public void onPageSelected(int position) {

            if (position + DEFAULT_PRELOAD_ALBUM_RANGE == (mLoadedCount - 1)) {
                final Photos data = ((ImageSwitcher.ImageSwitcherAdapter) mImageSwitcher.getAdapter()).getData();

                if (mNeedMore && mCanSendAlbumReq) {
                    mCanSendAlbumReq = false;
                    sendAlbumRequest(data);
                }
            }

            int currentPhotoPosition = mImageSwitcher.getPreviousSelectedPosition();
            if (position == 1 && currentPhotoPosition == 0) {
                hideControls();
            } else if (position == 0 && currentPhotoPosition > 0) {
                showControls();
            }
            setCounter(mImageSwitcher.getSelectedPosition());

            if (isAfterLast) {
                hideControls();
                isAfterLast = false;
            }

            if (position == ((ImageSwitcher.ImageSwitcherAdapter) mImageSwitcher.getAdapter()).getData().size() - 1) {
                showControls();
                isAfterLast = true;
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    private void sendAlbumRequest(final Photos data) {
        if (mUserSearchList == null)
            return;
        if ((mLoadedCount - 1) >= data.size())
            return;
        if (data.get(mLoadedCount - 1) == null)
            return;

        int position = data.get(mLoadedCount - 1).getPosition() + 1;
        final SearchUser currentSearchUser = mUserSearchList.getCurrentUser();
        if (currentSearchUser != null) {
            AlbumRequest request = new AlbumRequest(getActivity(), currentSearchUser.id,
                    ViewUsersListFragment.PHOTOS_LIMIT, position, AlbumRequest.MODE_SEARCH);
            final int uid = currentSearchUser.id;
            request.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    if (uid == mUserSearchList.getCurrentUser().id) {
                        Photos newPhotos = Photos.parse(response.jsonResult.optJSONArray("items"));
                        mNeedMore = response.jsonResult.optBoolean("more");
                        int i = 0;
                        for (Photo photo : newPhotos) {
                            if (mLoadedCount + i < data.size()) {
                                data.set(mLoadedCount + i, photo);
                                i++;
                            }
                        }
                        mLoadedCount += newPhotos.size();

                        if (mImageSwitcher != null && mImageSwitcher.getAdapter() != null) {
                            mImageSwitcher.getAdapter().notifyDataSetChanged();
                        }
                    }
                    mCanSendAlbumReq = true;
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    mCanSendAlbumReq = true;
                }
            }).exec();
        }
    }

    private void updateResources() {
        mResourcesLikes.setText(Integer.toString(CacheProfile.likes));
        mResourcesMoney.setText(Integer.toString(CacheProfile.money));
    }

    private OnUsersListEventsListener mSearchListener = new OnUsersListEventsListener() {
        @Override
        public void onEmptyList(UsersList usersList) {
            updateData(false);
        }

        @Override
        public void onPreload(UsersList usersList) {
            updateData(true);
        }

    };

    private void showEmptySearchDialog() {
        Debug.log("Search:: showEmptySearchDialog");
        mProgressBar.setVisibility(View.GONE);
        mImageSwitcher.setVisibility(View.GONE);
        mRetryView.setVisibility(View.VISIBLE);
    }
}
