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
import com.topface.topface.data.search.CachableSearch;
import com.topface.topface.data.search.OnSearchEventsListener;
import com.topface.topface.data.search.Search;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditAgeFragment;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.FilterFragment;
import com.topface.topface.ui.views.*;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.*;

import java.util.ArrayList;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker,
        RateController.OnRateControllerListener {

    public static final int SEARCH_LIMIT = 30;
    public static final int DEFAULT_PRELOAD_ALBUM_RANGE = 2;
    public static final String INVITE_POPUP = "INVITE_POPUP";
    private int mCurrentPhotoPrevPos;
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
    private ImageSwitcher mImageSwitcher;
    private CachableSearch mUserSearchList;
    private ProgressBar mProgressBar;
    private Novice mNovice;
    private AlphaAnimation mAlphaAnimation;
    private RateController mRateController;
    private RelativeLayout mDatingLoveBtnLayout;
    private ViewFlipper mViewFlipper;
    private RetryViewCreator mRetryView;

    private ImageButton mRetryBtn;
    private PreloadManager mPreloadManager;

    private BroadcastReceiver mReceiver;

    private Drawable singleMutual;
    private Drawable singleDelight;
    private Drawable doubleMutual;
    private Drawable doubleDelight;

    private NoviceLayout mNoviceLayout;
    private View mDatingResources;

    public static final int PHOTOS_LIMIT = 5;

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
    private ActionBar mActionBar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreloadManager = new PreloadManager();
        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);
        initMutualDrawables();
    }

    @Override
    protected void inBackroundThread() {
        super.inBackroundThread();
        SharedPreferences preferences = getActivity().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        mNovice = Novice.getInstance(preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        View view = inflater.inflate(R.layout.ac_dating, null);

        mActionBar = getActionBar(view);

        mRetryBtn = (ImageButton) view.findViewById(R.id.btnUpdate);
        mRetryBtn.setOnClickListener(this);

        mViewFlipper = (ViewFlipper) view.findViewById(R.id.vfFlipper);

        // Rate Controller
        mRateController = new RateController(getActivity());
        mRateController.setOnRateControllerListener(this);

        // Dating controls
        mDatingGroup = view.findViewById(R.id.loDatingGroup);
        mDatingLoveBtnLayout = (RelativeLayout) view.findViewById(R.id.loDatingLove);

        // User Info
        mUserInfoName = ((TextView) view.findViewById(R.id.tvDatingUserName));
        mUserInfoCity = ((TextView) view.findViewById(R.id.tvDatingUserCity));
        mUserInfoStatus = ((TextView) view.findViewById(R.id.tvDatingUserStatus));

        // Counter
        mCounter = ((TextView) view.findViewById(R.id.tvDatingCounter));

        // Progress
        mProgressBar = (ProgressBar) view.findViewById(R.id.prsDatingLoading);

        initResources(view);
        initControlButtons(view);
        initDatingAlbum(view);
        initNewbieLayout(view);
        new Thread(new Runnable() {
            @Override
            public void run() {
                checkInvitePopup();
            }
        }).start();

        mDatingLovePrice = (TextView) view.findViewById(R.id.tvDatingLovePrice);

        initEmptySearchDialog(view, initNavigationHeader(view));

        return view;
    }

    private void checkInvitePopup() {
        FragmentActivity activity = getActivity();
        if (CacheProfile.canInvite && activity != null) {
            final SharedPreferences preferences = activity.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

            long date_start = preferences.getLong(INVITE_POPUP, 1);
            long date_now = new java.util.Date().getTime();

            if (date_now - date_start >= CacheProfile.getOptions().popup_timeout) {
                preferences.edit().putLong(INVITE_POPUP, date_now).commit();
                ContactsProvider provider = new ContactsProvider(activity);
                provider.getContacts(-1, 0, new ContactsProvider.GetContactsListener() {
                    @Override
                    public void onContactsReceived(ArrayList<ContactsProvider.Contact> contacts) {

                        if (isAdded()) {
                            showInvitePopup(contacts);
                        }
                    }
                });
            }
        }
    }

    private void initMutualDrawables() {
        if (isAdded()) {
            singleMutual = getResources().getDrawable(R.drawable.dating_mutual_selector);
            singleDelight = getResources().getDrawable(R.drawable.dating_delight_selector);

            doubleMutual = getResources().getDrawable(R.drawable.dating_dbl_mutual_selector);
            doubleDelight = getResources().getDrawable(R.drawable.dating_dbl_delight_selector);
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

    private void initDatingAlbum(View view) {
        // Dating Album
        mImageSwitcher = ((ImageSwitcher) view.findViewById(R.id.glrDatingAlbum));
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);
        mImageSwitcher.setUpdateHandler(mUnlockHandler);
    }

    private void initNewbieLayout(View view) {
        // Newbie
        mNoviceLayout = (NoviceLayout) view.findViewById(R.id.loNovice);
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

    private OnClickListener initNavigationHeader(View view) {
        // Navigation Header
        ActionBar actionBar = getActionBar(view);
        setHeader(view);
        actionBar.showHomeButton((NavigationActivity) getActivity());
        OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(),
                        EditContainerActivity.class);
                startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FILTER);
            }
        };
        actionBar.showSettingsButton(listener, false);
        return listener;
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

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPreloadManager != null) {
                    mPreloadManager.checkConnectionType(intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, 0));
                }
            }
        };

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
        setHeader(getView());
    }

    private void updateData(final boolean isAddition) {
        if (!mUpdateInProcess) {
            lockControls();
            hideEmptySearchDialog();
            if (!isAddition) {
                onUpdateStart(isAddition);
            }

            mUpdateInProcess = true;

            Search.log("Update start: " + (isAddition ? "addition" : "replace"));

            getSearchRequest().callback(new DataApiHandler<Search>() {

                @Override
                protected void success(Search search, ApiResponse response) {
                    Search.log("load success. Loaded " + search.size() + " users");
                    if (search.size() != 0) {
                        mImageSwitcher.setVisibility(View.VISIBLE);
                        //Добавляем новых пользователей
                        mUserSearchList.addAndUpdateSignature(search);
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


                @Override
                protected Search parseResponse(ApiResponse response) {
                    return new Search(response);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        Search.log("load error: " + response.message);
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
            mUserSearchList = new CachableSearch();
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

        FragmentActivity activity = getActivity();
        switch (view.getId()) {
            case R.id.loDatingResources: {
                EasyTracker.getTracker().trackEvent("Dating", "BuyClick", "", 1L);
                Intent intent = new Intent(activity, ContainerActivity.class);
                intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                startActivity(intent);
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
                if (mCurrentUser != null && activity != null) {
                    activity.startActivity(ContainerActivity.getProfileIntent(mCurrentUser.id, activity));
                    EasyTracker.getTracker().trackEvent("Dating", "Additional", "Profile", 1L);
                }
            }
            break;
            case R.id.btnDatingChat: {
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
        mCurrentPhotoPrevPos = 0;
        setCounter(mCurrentPhotoPrevPos);
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

        if (mNovice.isDatingCompleted())
            return;

        //TODO check flag
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

    public void showInvitePopup(ArrayList<ContactsProvider.Contact> data) {
        EasyTracker.getTracker().trackEvent("InvitesPopup", "Show", "", 0L);
        InvitesPopup popup = InvitesPopup.newInstance(data);
        ((BaseFragmentActivity) getActivity()).startFragment(popup);
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
        mActionBar.show();
        mDatingGroup.setVisibility(View.VISIBLE);
        mIsHide = false;
    }

    @Override
    public void hideControls() {
        mDatingGroup.setVisibility(View.GONE);
        mActionBar.hide();
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
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        setHighRatePrice();
        updateResources();
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
                        try {
                            CacheProfile.dating = filter.clone();
                        } catch (CloneNotSupportedException e) {
                            Debug.error(e);
                        }

                        updateFilterData();
                        updateData(false);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
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

    private void setHeader(View view) {
        if (CacheProfile.dating != null) {
            String plus = CacheProfile.dating.age_end == DatingFilter.webAbsoluteMaxAge ? "+" : "";
            int age = CacheProfile.dating.age_end == DatingFilter.webAbsoluteMaxAge ? EditAgeFragment.absoluteMax : CacheProfile.dating.age_end;
            Context context = App.getContext();
            String headerText = context.getString(
                    CacheProfile.dating.sex == Static.BOY ? R.string.dating_header_guys : R.string.dating_header_girls,
                    CacheProfile.dating.age_start, age);
            getActionBar(view).setTitleText(headerText + plus);

            getActionBar(view).setSubTitleText(getSubtitle(context));
        }
    }

    private String getSubtitle(Context context) {
        String cityString = CacheProfile.dating.city == null || CacheProfile.dating.city.isEmpty() ?
                context.getString(R.string.filter_cities_all) :
                CacheProfile.dating.city.name;

        String onlineString = DatingFilter.getOnlineField() ? context.getString(R.string.dating_online_only) : "%s";
        return String.format(onlineString, cityString);
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

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
        //При выходе из фрагмента сохраняем кэш поиска
        if (mUserSearchList != null) {
            mUserSearchList.saveCache();
        }
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

            if (position + DEFAULT_PRELOAD_ALBUM_RANGE == mLoadedCount) {
                final Photos data = ((ImageSwitcher.ImageSwitcherAdapter) mImageSwitcher.getAdapter()).getData();

                if (mNeedMore) {

                    mImageSwitcher.getAdapter().notifyDataSetChanged();
                    if (mCanSendAlbumReq) {
                        mCanSendAlbumReq = false;
                        sendAlbumRequest(data);
                    }

                }
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
        if ((mLoadedCount - 1) >= data.size()) {
            return;
        }
        int position = data.get(mLoadedCount - 1).getPosition() + 1;
        if (mUserSearchList != null && mUserSearchList.getCurrentUser() != null) {
            AlbumRequest request = new AlbumRequest(getActivity(), mUserSearchList.getCurrentUser().id, PHOTOS_LIMIT, position, AlbumRequest.MODE_SEARCH);
            final int uid = mUserSearchList.getCurrentUser().id;
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

                        if (mImageSwitcher != null) {
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

    private OnSearchEventsListener mSearchListener = new OnSearchEventsListener() {
        @Override
        public void onEmptyList(Search search) {
            updateData(false);
        }

        @Override
        public void onPreload(Search search) {
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
