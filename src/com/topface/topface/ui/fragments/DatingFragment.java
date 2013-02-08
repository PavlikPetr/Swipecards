package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.content.*;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
import com.topface.topface.data.DatingFilter;
import com.topface.topface.data.NoviceLikes;
import com.topface.topface.data.SkipRate;
import com.topface.topface.data.search.CachableSearch;
import com.topface.topface.data.search.OnSearchEventsListener;
import com.topface.topface.data.search.Search;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.*;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditAgeFragment;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.FilterFragment;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.ui.views.NoviceLayout;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.*;
import com.topface.topface.utils.cache.SearchCacheManager;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker,
        RateController.OnRateControllerListener {

    public static final int SEARCH_LIMIT = 25;
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
    private View mDatingGroup;
    private ImageSwitcher mImageSwitcher;
    private CachableSearch mUserSearchList;
    private ProgressBar mProgressBar;
    private Novice mNovice;
    private AlphaAnimation mAlphaAnimation;
    private RateController mRateController;
    private View mNavigationHeader;
    private View mNavigationHeaderShadow;
    private RelativeLayout mDatingLoveBtnLayout;
    private ViewFlipper mViewFlipper;

    private ImageButton mRetryBtn;
    private RetryView emptySearchDialog;
    private PreloadManager mPreloadManager;

    private BroadcastReceiver mReceiver;

    private Drawable singleMutual;
    private Drawable singleDelight;
    private Drawable doubleMutual;
    private Drawable doubleDelight;

    private NoviceLayout mNoviceLayout;
    private View mDatingResources;

    private boolean hasOneSympathyOrDelight = false;
    private SearchCacheManager mCache;
    private SearchUser mCurrentUser;
    /**
     * Флаг того, что запущено обновление поиска и запускать дополнительные обновления не нужно
     */
    private boolean mUpdateInProcess;
    private BroadcastReceiver mProfileReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserSearchList = new CachableSearch();
        mUserSearchList.setOnEmptyListListener(mSearchListener);
        mPreloadManager = new PreloadManager();
        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);
        initMutualDrawables();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        View view = inflater.inflate(R.layout.ac_dating, null);

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

        setHighRatePrice(view);

        final ImageButton settingsButton = initNavigationHeader(view);
        initEmptySearchDialog(view, settingsButton);

        //Показываем последнего пользователя
        showUser(mUserSearchList.getCurrentUser());
        return view;
    }

    private void initMutualDrawables() {
        if (isAdded()) {
            singleMutual = getResources().getDrawable(R.drawable.dating_mutual_selector);
            singleDelight = getResources().getDrawable(R.drawable.dating_delight_selector);

            doubleMutual = getResources().getDrawable(R.drawable.dating_dbl_mutual_selector);
            doubleDelight = getResources().getDrawable(R.drawable.dating_dbl_delight_selector);
        }
    }

    private void setHighRatePrice(View view) {
        // Dating Love Price
        int delightPrice = CacheProfile.getOptions().price_highrate;
        TextView datingLovePrice = (TextView) view.findViewById(R.id.tvDatingLovePrice);
        if (delightPrice > 0) {
            datingLovePrice.setText(Integer.toString(CacheProfile.getOptions().price_highrate));
        } else {
            datingLovePrice.setVisibility(View.GONE);
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
        SharedPreferences preferences = getActivity().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        mNovice = Novice.getInstance(preferences);
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

    private ImageButton initNavigationHeader(View view) {
        // Navigation Header
        mNavBarController = new NavigationBarController(
                (ViewGroup) view.findViewById(R.id.loNavigationBar));
        view.findViewById(R.id.btnNavigationHome).setOnClickListener(
                (NavigationActivity) getActivity());

        setHeader(view);

        mNavigationHeader = view.findViewById(R.id.loNavigationBar);
        final ImageButton settingsButton = (ImageButton) view.findViewById(R.id.btnNavigationSettingsBar);
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
        return settingsButton;
    }

    private void initEmptySearchDialog(View view, final ImageButton settingsButton) {
        emptySearchDialog = new RetryView(getActivity());
        emptySearchDialog.setErrorMsg(App.getContext().getString(R.string.general_search_null_response_error));
        emptySearchDialog.addButton(RetryView.REFRESH_TEMPLATE + App.getContext().getString(R.string.general_dialog_retry), new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(false);
            }
        });
        emptySearchDialog.addButton(App.getContext().getString(R.string.change_filters), new OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsButton.performClick();
            }
        });
        emptySearchDialog.setVisibility(View.GONE);
        ((RelativeLayout) view.findViewById(R.id.ac_dating_container)).addView(emptySearchDialog);

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
            }
        };
    }

    private void updateFilterData() {
        //Если изменился фильтр, то мы ставим новую подпись фильтра,
        //если она отличается от той, что в поиске, то поиск будет очищен
        mUserSearchList.updateSignatureAndUpdate();
        View view = getView();
        setHeader(view);
    }

    private void updateData(final boolean isAddition) {
        if (!mUpdateInProcess) {
            lockControls();
            emptySearchDialog.setVisibility(View.GONE);
            if (!isAddition) {
                onUpdateStart(isAddition);
            }

            mUpdateInProcess = true;

            Search.log("Update start");

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
                        if (mCurrentUser != currentUser || mCurrentUser == null) {
                            showUser(currentUser);
                        }

                        //Скрываем кнопку отправки повтора
                        mRetryBtn.setVisibility(View.GONE);
                        unlockControls();
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        if (!isAddition) {
                            mImageSwitcher.setVisibility(View.GONE);
                            emptySearchDialog.setVisibility(View.VISIBLE);
                        }
                    }
                }


                @Override
                protected Search parseResponse(ApiResponse response) {
                    return new Search(response);
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    Search.log("load error" + codeError);
                    Toast.makeText(getActivity(), App.getContext().getString(R.string.general_data_error),
                            Toast.LENGTH_SHORT).show();
                    onUpdateFail(isAddition);
                    unlockControls();
                }

                @Override
                public void always(ApiResponse response) {
                    super.always(response);
                    mUpdateInProcess = false;
                }
            }).exec();
        }
    }

    private SearchRequest getSearchRequest() {
        SearchRequest searchRequest = new SearchRequest(getActivity());
        SharedPreferences preferences = App.getContext().getSharedPreferences(
                Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        searchRequest.limit = SEARCH_LIMIT;
        searchRequest.online = getFilterOnline(preferences);
        registerRequest(searchRequest);
        return searchRequest;
    }

    private boolean getFilterOnline(SharedPreferences preferences) {
        return preferences.getBoolean(
                App.getContext().getString(R.string.cache_profile_filter_online),
                false
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loDatingResources: {
                EasyTracker.getTracker().trackEvent("Dating", "BuyClick", "", 1L);
                Intent intent = new Intent(getActivity(), ContainerActivity.class);
                intent.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_BUYING_FRAGMENT);
                startActivity(intent);
            }
            break;
            case R.id.btnDatingLove: {
                if (mCurrentUser != null) {
                    if (mUserSearchList.isEnded()) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        if (CacheProfile.money > 0) {
                            CacheProfile.money = CacheProfile.money - CacheProfile.getOptions().price_highrate;
                        }
                        mRateController.onRate(mCurrentUser.id, 10,
                                mCurrentUser.mutual ? RateRequest.DEFAULT_MUTUAL
                                        : RateRequest.DEFAULT_NO_MUTUAL);

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
                    if (mUserSearchList.isEnded()) {
                        updateData(true);
                        return;
                    } else {
                        lockControls();
                        mRateController.onRate(mCurrentUser.id, 9,
                                mCurrentUser.mutual ? RateRequest.DEFAULT_MUTUAL
                                        : RateRequest.DEFAULT_NO_MUTUAL);

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
                ((NavigationActivity) getActivity()).onExtraFragment(
                        ProfileFragment.newInstance(mUserSearchList.get(mUserSearchList.getSearchPosition()).id, ProfileFragment.TYPE_USER_PROFILE));

                EasyTracker.getTracker().trackEvent("Dating", "Additional", "Profile", 1L);
            }
            break;
            case R.id.btnDatingChat: {
                Intent intent = new Intent(getActivity(), ContainerActivity.class);

                intent.putExtra(ChatFragment.INTENT_USER_ID, mCurrentUser.id);
                intent.putExtra(ChatFragment.INTENT_USER_NAME, mCurrentUser.first_name);
                intent.putExtra(ChatFragment.INTENT_USER_SEX, mCurrentUser.sex);
                intent.putExtra(ChatFragment.INTENT_USER_AGE, mCurrentUser.age);
                intent.putExtra(ChatFragment.INTENT_USER_CITY, mCurrentUser.city.name);
                intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
                getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);

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
        if (currUser != null) {
            lockControls();
            if (currUser.city != null) {
                mUserInfoCity.setText(currUser.city.name);
            }
            mUserInfoStatus.setText(currUser.status);
            mUserInfoName.setText(currUser.getNameAndAge());

            Resources res = App.getContext().getResources();

            if (isAdded()) {
                if (currUser.online) {
                    mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(
                            res.getDrawable(R.drawable.im_online), null, null, null);
                } else {
                    mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(
                            res.getDrawable(R.drawable.im_offline), null, null, null);
                }

                if (currUser.sex == Static.BOY) {
                    mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, res
                            .getDrawable(R.drawable.dating_man_selector), null, null);
                } else if (currUser.sex == Static.GIRL) {
                    mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, res
                            .getDrawable(R.drawable.dating_woman_selector), null, null);
                }
            }

            // buttons drawables
            mMutualBtn.setCompoundDrawablesWithIntrinsicBounds(null, currUser.mutual ? doubleMutual
                    : singleMutual, null, null);
            mMutualBtn.setText(currUser.mutual ? App.getContext().getString(R.string.general_mutual)
                    : App.getContext().getString(R.string.general_sympathy));

            mDelightBtn.setCompoundDrawablesWithIntrinsicBounds(null,
                    currUser.mutual ? doubleDelight : singleDelight, null, null);

            // photos
            mImageSwitcher.setData(currUser.photos);
            mImageSwitcher.setCurrentItem(0, true);
            mCurrentPhotoPrevPos = 0;
            setCounter(mCurrentPhotoPrevPos);
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
            mNoviceLayout.setLayoutRes(R.layout.novice_sympathy, new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mMutualBtn.performClick();
                }
            });
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
        mUserInfoName.setVisibility(mCurrentUser != null ? View.VISIBLE : View.GONE);
        mUserInfoCity.setVisibility(mCurrentUser != null ? View.VISIBLE : View.GONE);
        mUserInfoStatus.setVisibility(View.VISIBLE);

        boolean enabled = false;
        if (!mUserSearchList.isEnded() && mCurrentUser != null) {
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
        mDatingGroup.setVisibility(View.GONE);
        mNavigationHeader.setVisibility(View.GONE);
        mNavigationHeaderShadow.setVisibility(View.GONE);
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
        updateResources();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
            lockControls();
            updateFilterData();
            updateData(false);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setHeader(View view) {
        String plus = CacheProfile.dating.age_end == FilterFragment.webAbsoluteMaxAge ? "+" : "";
        int age = CacheProfile.dating.age_end == FilterFragment.webAbsoluteMaxAge ? EditAgeFragment.absoluteMax : CacheProfile.dating.age_end;
        Context context = App.getContext();
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(context.getString(
                CacheProfile.dating.sex == Static.BOY ? R.string.dating_header_guys
                        : R.string.dating_header_girls, CacheProfile.dating.age_start,
                age) + plus);

        TextView subTitle = (TextView) view.findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);

        subTitle.setText(getSubtitle(context));
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
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

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
}
