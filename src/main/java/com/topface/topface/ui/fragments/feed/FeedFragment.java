package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.topface.framework.JsonUtils;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.banners.BannersController;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.FeedUser;
import com.topface.topface.data.UnlockFunctionalityOption;
import com.topface.topface.data.UnlockFunctionalityOption.UnlockScreenCondition;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.state.CountersDataProvider;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedAnimatedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.adapters.MultiselectionController;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.BackgroundProgressBarController;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.ui.views.SwipeRefreshController;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.actionbar.OverflowMenu;
import com.topface.topface.utils.config.FeedsCache;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.OnTouch;

import static com.topface.topface.utils.CountersManager.NULL_METHOD;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment
        implements FeedAdapter.OnAvatarClickListener<T>, IPageWithAds {

    public static final boolean PAUSE_DOWNLOAD_ON_SCROLL = false;
    public static final boolean PAUSE_DOWNLOAD_ON_FLING = true;

    private static final int FEED_MULTI_SELECTION_LIMIT = 100;
    private static final int FIRST_SHOW_LIST_DELAY = 1500;

    private static final String FEEDS = "FEEDS";
    private static final String POSITION = "POSITION";
    private static final String HAS_AD = "HAS_AD";
    private static final String BLACK_LIST_USER = "black_list_user";
    private static final String FEED_AD = "FEED_AD";
    public static final String REFRESH_DIALOGS = "refresh_dialogs";
    private static final String FEED_COUNTER = "counter";
    private static final String FEED_COUNTER_CHANGED = "counter_changed";
    private static final String FEED_LAST_UNREAD_STATE = "last_unread_state";
    private int currentCounter;
    private boolean isCurrentCounterChanged;
    protected FeedAdapter<T> mListAdapter;
    protected boolean mIsUpdating;
    private SwipeRefreshLayout mSwipeRefresh;
    private BackgroundProgressBarController mBackgroundController = new BackgroundProgressBarController();
    private RetryViewCreator mRetryView;
    private BroadcastReceiver mReadItemReceiver;
    private BannersController mBannersController;
    private TextView mActionModeTitle;
    private Boolean isNeedFirstShowListDelay = null;
    private CountDownTimer mListShowDelayCountDownTimer;
    private boolean isDataFromCache;
    private boolean isDeletable = true;
    private boolean needUpdate = false;
    private BroadcastReceiver mBlacklistedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(BlackListAndBookmarkHandler.TYPE) &&
                    intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE)
                            .equals(BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST) && isAdded()) {
                int[] ids = intent.getIntArrayExtra(BlackListAndBookmarkHandler.FEED_IDS);
                int id = intent.getIntExtra(BlackListAndBookmarkHandler.FEED_ID, 0);
                boolean hasValue = intent.hasExtra(BlackListAndBookmarkHandler.VALUE);
                boolean value = intent.getBooleanExtra(BlackListAndBookmarkHandler.VALUE, false);
                if ((ListUtils.isNotEmpty(ids) || id != 0) && hasValue) {
                    if (value == whetherDeleteIfBlacklisted()) {
                        if(ListUtils.isNotEmpty(ids)) {
                            getListAdapter().removeByUserIds(ids);
                        }
                        else if(id != 0) {
                            getListAdapter().removeByUserId(id);
                        }
                    } else {
                        needUpdate = true;
                    }
                }
            }
        }
    };
    private BroadcastReceiver mGcmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isResumed()) {
                for (int type : getTypesForGCM()) {
                    GCMUtils.cancelNotification(getActivity(), type);
                }
            }
            if (getUserVisibleHint()) {
                updateData(true, false);
            } else {
                needUpdate = true;
            }
        }
    };
    CountersDataProvider mCountersDataProvider;

    @BindView(R.id.feedContainer)
    RelativeLayout mContainer;
    @BindView(R.id.lvFeedList)
    ListView mListView;
    @BindView(R.id.llvFeedLoading)
    View mLockView;
    @BindView(R.id.stubForEmptyFeed)
    ViewStub mEmptyScreenStub;

    public void saveToCache() {
        FeedList<T> data = getListAdapter().getDataForCache();
        if (data != null && !data.isEmpty()) {
            cacheData(JsonUtils.toJson(data));
        } else {
            cacheData("");
        }
    }

    private void cacheData(String value) {
        FeedsCache.FEEDS_TYPE type = getFeedsType();
        if (type == FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE) {
            return;
        }
        App.getFeedsCache().setFeedToCache(value, type).saveConfig();
    }

    private void clearCache() {
        getListAdapter().removeAllData();
        cacheData("");
    }

    @NotNull
    protected FeedsCache.FEEDS_TYPE getFeedsType() {
        return FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE;
    }

    private int mIdForRemove;
    private BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(OverflowMenu.USER_ID_FOR_REMOVE)) {
                mIdForRemove = intent.getIntExtra(OverflowMenu.USER_ID_FOR_REMOVE, -1);
            }
        }
    };

    private ActionMode mActionMode;
    private ActionMode.Callback mActionActivityCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            setToolBarVisibility(false);
            mActionMode = mode;
            mActionMode.setCustomView(getActionModeTitle());
            FeedAdapter<T> adapter = getListAdapter();
            adapter.setMultiSelectionListener(new MultiselectionController.IMultiSelectionListener() {
                @Override
                public void onSelected(int size, boolean overlimit) {
                    if (overlimit) {
                        Utils.showToastNotification(R.string.maximum_number_of_users, Toast.LENGTH_LONG);
                    }
                    if (mActionMode != null) {
                        getActionModeTitle().setText(Utils.getQuantityString(R.plurals.selected, size, size));
                    }
                }

            });
            adapter.notifyDataSetChanged();
            menu.clear();
            getActivity().getMenuInflater().inflate(getContextMenuLayoutRes(), menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            boolean result = true;
            FeedAdapter<T> adapter = getListAdapter();
            switch (item.getItemId()) {
                case R.id.delete_list_item:
                    onDeleteFeedItems(getSelectedFeedIds(adapter), adapter.getSelectedItems());
                    break;
                case R.id.add_to_black_list:
                    onAddToBlackList(adapter.getSelectedUsersIds(), adapter.getSelectedItems());
                    break;
                default:
                    result = false;
            }
            if (result) {
                if (mActionMode != null) mActionMode.finish();
            }

            return result;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getListAdapter().finishMultiSelection();
            mActionMode = null;
            setToolBarVisibility(true);
        }
    };

    private FeedRequest.UnreadStatePair mLastUnreadState = new FeedRequest.UnreadStatePair(true, false);
    private View mInflated;
    protected CountersData mCountersData = new CountersData();

    protected static void initButtonForBlockedScreen(Button button, String buttonText, View.OnClickListener listener) {
        initButtonForBlockedScreen(null, null, button, buttonText, listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRefreshReceiver, new IntentFilter(REFRESH_DIALOGS));
        View root = inflater.inflate(getLayout(), null);
        ButterKnife.bind(this, root);
        initNavigationBar();
        mLockView.setVisibility(View.GONE);
        init();
        initViews(root);
        useCacheData();
        restoreInstanceState(saved);
        mCountersDataProvider = new CountersDataProvider(new CountersDataProvider.ICountersUpdater() {
            @Override
            public void onUpdateCounters(CountersData countersData) {
                updateCounters(countersData);
            }
        });
        mReadItemReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String itemId = intent.getStringExtra(ChatFragment.INTENT_ITEM_ID);
                int userId = intent.getIntExtra(ChatFragment.INTENT_USER_ID, 0);
                if (userId == 0) {
                    if (!TextUtils.isEmpty(itemId)) {
                        makeItemReadWithFeedId(itemId);
                    }
                } else {
                    makeItemReadUserId(userId, intent.getIntExtra(ChatFragment.LOADED_MESSAGES, 0));
                }
            }
        };
        IntentFilter filter = new IntentFilter(ChatFragment.MAKE_ITEM_READ);
        filter.addAction(ChatFragment.MAKE_ITEM_READ_BY_UID);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReadItemReceiver, filter);
        for (int type : getTypesForGCM()) {
            GCMUtils.cancelNotification(getActivity(), type);
        }
        allViewsInitialized();
        return root;
    }

    protected static void initButtonForBlockedScreen(TextView textView, String text,
                                                     Button button, String buttonText,
                                                     View.OnClickListener listener) {
        if (textView != null) {
            if (TextUtils.isEmpty(text)) {
                textView.setVisibility(View.GONE);
            } else {
                textView.setVisibility(View.VISIBLE);
                textView.setText(text);
            }
        }

        if (TextUtils.isEmpty(buttonText)) {
            // Не показываем кнопку без текста
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
            button.setText(buttonText);
            button.setOnClickListener(listener);
        }
    }

    private void removeBlackListUserFromFeed() {
        if (mIdForRemove > 0) {
            getListAdapter().removeByUserId(mIdForRemove);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopListShowDelayTimer();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRefreshReceiver);
    }

    @SuppressWarnings("unchecked")
    protected void restoreInstanceState(Bundle saved) {
        if (saved != null) {
            mLastUnreadState = saved.getParcelable(FEED_LAST_UNREAD_STATE);
            isCurrentCounterChanged = saved.getBoolean(FEED_COUNTER_CHANGED);
            currentCounter = saved.getInt(FEED_COUNTER);
            mIdForRemove = saved.getInt(BLACK_LIST_USER);
            Parcelable[] feeds = saved.getParcelableArray(FEEDS);
            FeedList<T> feedsList = new FeedList<>();
            if (feeds != null) {
                for (Parcelable p : feeds) {
                    feedsList.add((T) p);
                }
            }
            mListAdapter.setData(feedsList);
            mListView.setSelection(saved.getInt(POSITION, 0));
            if (!mListAdapter.isEmpty()) {
                mBackgroundController.hide();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBannersController = new BannersController(this, App.get().getOptions());
    }

    private void registerGcmReceiver() {
        String action = getGcmUpdateAction();
        if (action != null) {
            LocalBroadcastManager.getInstance(App.getContext()).registerReceiver(mGcmReceiver, new IntentFilter(action));
        }
    }

    protected String getGcmUpdateAction() {
        return null;
    }

    private void initViews(View root) {
        initBackground(root);
        initSwipeRefresh(root);
        initListView();
        initRetryViews();
        initViewStubForEmptyFeed();
    }

    private void initSwipeRefresh(View root) {
        mSwipeRefresh = new SwipeRefreshController((SwipeRefreshLayout) root.findViewById(R.id.swipeRefresh)).getSwipeRefreshLayout();
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefresh.setRefreshing(true);
                updateData(true, true);
            }
        });
        mSwipeRefresh.setEnabled(isSwipeRefreshEnable());
    }

    protected void initViewStubForEmptyFeed() {
        try {
            mEmptyScreenStub.setLayoutResource(getEmptyFeedLayout());
        } catch (Exception ex) {
            Debug.log(ex.toString());
        }
    }

    protected ViewStub getEmptyFeedViewStub() {
        return mEmptyScreenStub;
    }

    protected void initNavigationBar() {
        setActionBarTitles(getTitle());
    }

    abstract protected Type getFeedListDataType();

    abstract protected Class getFeedListItemClass();

    private void useCacheData() {
        updateData(false, true, false);
        final FeedsCache.FEEDS_TYPE type = getFeedsType();
        if (type != FeedsCache.FEEDS_TYPE.UNKNOWN_TYPE) {
            final Type dataType = getFeedListDataType();
            String fromCacheString = App.getFeedsCache().getFeedFromCache(type);
            FeedList<T> feedList = JsonUtils.fromJson(fromCacheString, dataType);
            FeedAdapter<T> adapter = getListAdapter();
            if (feedList != null && !feedList.isEmpty()) {
                if (adapter != null && adapter.getCount() == 0) {
                    isDataFromCache = true;
                    adapter.addData(new FeedListData<>(feedList, true, getFeedListItemClass()));
                    adapter.notifyDataSetChanged();
                    showListViewWithSuccessResponse();
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerGcmReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBlacklistedReceiver, new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
    }

    protected void updateCounters(CountersData countersData) {
        mCountersData = countersData;
        updateDataAfterReceivingCounters();
    }

    @Override
    public void onResume() {
        super.onResume();
        removeBlackListUserFromFeed();
        FeedAdapter<T> adapter = getListAdapter();
        if (adapter.isNeedUpdate() || needUpdate) {
            updateData(false, true);
        }
        // try update list if last visible item is loader,
        // and loading was probably interrupted
        adapter.loadOlderItemsIfNeeded(
                mListView.getFirstVisiblePosition(),
                mListView.getChildCount(),
                adapter.getCount());
    }

    @Override
    public void onPause() {
        super.onPause();
        saveToCache();
        finishMultiSelection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCountersDataProvider != null) {
            mCountersDataProvider.unsubscribe();
        }
        if (mBannersController != null) {
            mBannersController.onDestroy();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReadItemReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBlacklistedReceiver);
        if (getGcmUpdateAction() != null) {
            LocalBroadcastManager.getInstance(App.getContext()).unregisterReceiver(mGcmReceiver);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mListAdapter != null) {
            FeedList<T> data = mListAdapter.getData();
            outState.putParcelableArray(FEEDS, data.toArray(new Parcelable[data.size()]));
            outState.putInt(POSITION, mListView != null ? mListView.getFirstVisiblePosition() : 0);
            outState.putInt(BLACK_LIST_USER, mIdForRemove);
            outState.putInt(FEED_COUNTER, currentCounter);
            outState.putBoolean(FEED_COUNTER_CHANGED, isCurrentCounterChanged);
            outState.putParcelable(FEED_LAST_UNREAD_STATE, mLastUnreadState);
        }
    }

    protected void init() {
    }

    protected void allViewsInitialized() {
    }

    private void initBackground(View view) {
        // ListView background
        mBackgroundController.setProgressBar((ProgressBar) view.findViewById(R.id.tvBackgroundText));
        mBackgroundController.startAnimation();
    }

    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_UNKNOWN};
    }

    abstract protected int getFeedType();

    protected int getLayout() {
        return R.layout.fragment_feed;
    }

    private void initListView() {
        // ListView
        mListAdapter = createNewAdapter();

        FeedAnimatedAdapter animationAdapter = new FeedAnimatedAdapter(mListAdapter);
        animationAdapter.setAbsListView(mListView);

        FeedAdapter<T> adapter = getListAdapter();
        adapter.setOnAvatarClickListener(this);
        //Пауза загрузки изображений при прокрутке списка
        mListView.setOnScrollListener(
                new PauseOnScrollListener(
                        DefaultImageLoader.getInstance(App.getContext()).getImageLoader(),
                        PAUSE_DOWNLOAD_ON_SCROLL,
                        PAUSE_DOWNLOAD_ON_FLING,
                        adapter
                )
        );
        mListView.setAdapter(animationAdapter);
    }

    /**
     * Метод возвращает новый инстанс адаптера
     *
     * @return адаптер фида
     */
    abstract protected FeedAdapter<T> createNewAdapter();

    protected LoadingListAdapter.Updater getUpdaterCallback() {
        return new LoadingListAdapter.Updater() {
            @Override
            public void onUpdate() {
                if (!mIsUpdating) {
                    updateData(false, true, false);
                }
            }
        };
    }

    @SuppressWarnings("unused")
    @OnItemClick(R.id.lvFeedList)
    protected void listOnItemClickListener(AdapterView<?> parent, int position, long itemPosition) {
        final FeedAdapter<T> adapter = getListAdapter();
        if (adapter.isMultiSelectionMode()) {
            adapter.onSelection((int) itemPosition);
        } else {
            T item = (T) parent.getItemAtPosition(position);
            if (item != null) {
                if (!mIsUpdating && item.isRetrier()) {
                    updateUI(new Runnable() {
                        public void run() {
                            adapter.showLoaderItem();
                        }
                    });
                    updateData(false, true, false);
                } else {
                    try {
                        onFeedItemClick(item);
                    } catch (Exception e) {
                        Debug.error("FeedItem click error:", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @OnItemLongClick(R.id.lvFeedList)
    protected boolean listOnItemLongClickListener(final long itemPosition) {
        if (isDeletable) {
            FeedAdapter<T> adapter = getListAdapter();
            ((AppCompatActivity) getActivity()).startSupportActionMode(mActionActivityCallback);
            adapter.startMultiSelection(getMultiSelectionLimit());
            adapter.onSelection((int) itemPosition);
            return true;
        }
        return false;
    }

    protected int getMultiSelectionLimit() {
        return FEED_MULTI_SELECTION_LIMIT;
    }

    public void finishMultiSelection() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    /**
     * В данный момент у нас проблема с id в диалогах, поэтому в DialogsFragment этот метод переопределен
     */
    protected List<String> getSelectedFeedIds(FeedAdapter<T> adapter) {
        return adapter.getSelectedFeedIds();
    }

    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu;
    }

    private void onAddToBlackList(List<Integer> ids, final List<T> items) {
        mLockView.setVisibility(View.VISIBLE);
        new BlackListAddRequest(ids, getActivity()).
                callback(new BlackListAndBookmarkHandler(getActivity(),
                        BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                        ids, true, new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        if (isAdded()) {
                            getListAdapter().removeItems(items);
                            if (getListAdapter().getData().isEmpty()) {
                                mListView.setVisibility(View.INVISIBLE);
                                onEmptyFeed();
                            }
                        }
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                    }

                    @Override
                    public void always(IApiResponse response) {
                        super.always(response);
                        if (mLockView != null) {
                            mLockView.setVisibility(View.GONE);
                        }
                    }
                })).exec();
    }

    private void onDeleteFeedItems(List<String> ids, final List<T> items) {
        DeleteAbstractRequest deleteFeedsRequest = getDeleteRequest(ids);
        //Если удаление не поддерживается данным потомком,
        //то у нас ошибка с показом меню (есть кнопка там, где удаление не поддерживается)
        //и нужно сообщить пользователю, что удалить не получится
        if (deleteFeedsRequest == null) {
            Utils.showErrorMessage();
            return;
        }
        mLockView.setVisibility(View.VISIBLE);
        deleteFeedsRequest.callback(new SimpleApiHandler() {
            @Override
            public void success(IApiResponse response) {
                if (isAdded()) {
                    getListAdapter().removeItems(items);
                    if (getListAdapter().getData().isEmpty()) {
                        mListView.setVisibility(View.INVISIBLE);
                        onEmptyFeed();
                    }
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (codeError != ErrorCodes.PREMIUM_ACCESS_ONLY) {
                    super.fail(codeError, response);
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (mLockView != null) {
                    mLockView.setVisibility(View.GONE);
                }
            }
        }).exec();
    }

    protected abstract DeleteAbstractRequest getDeleteRequest(List<String> ids);

    protected T getItem(int position) {
        return getListAdapter().getItem(position);
    }

    @SuppressWarnings("unused")
    @OnTouch(R.id.lvFeedList)
    protected boolean listOnTouchListener() {
        return false;
    }

    protected void onFeedItemClick(FeedItem item) {
        //Open chat activity
        if (!item.user.isEmpty()) {
            FeedUser user = item.user;
            Intent intent = ChatActivity.createIntent(user.id, user.sex, user.getNameAndAge(), user.city.name
                    , null, user.photo, false, item.type, user.banned);
            startActivityForResult(intent, ChatActivity.REQUEST_CHAT);
        }
    }

    public void onAvatarClick(T item, View view) {
        if (isAdded()) {
            FeedAdapter<T> adapter = getListAdapter();
            if (adapter.isMultiSelectionMode()) {
                adapter.onSelection(item);
            } else {
                startActivity(UserProfileActivity.createIntent(null, item.user.photo, item.user.id, item.id, false, true, Utils.getNameAndAge(item.user.firstName, item.user.age), item.user.city.getName()));
            }
        }
    }

    /**
     * Tries to update feed content, when tab containig this feed is selected
     * used when feed wrapped in tabbed container
     */
    public void startInitialLoadIfNeed() {
        if (getListAdapter() != null) {
            if ((getListAdapter().isNeedUpdate()
                    || needUpdate
                    || (hasUnread() && isCurrentCounterChanged()))
                    && !mIsUpdating) {
                updateData(true, true);
            }
        }
    }

    /**
     * @return true if got unread feeds, calculated from counters
     */
    private boolean hasUnread() {
        return (getUnreadCount() > 0);
    }

    private int getUnreadCount() {
        int value = getUnreadCounter();
        setCurrentCounter(value);
        return value;
    }

    /**
     * Returns unread counter, from CacheProfile
     * if there is no counter for feed - should return 0
     *
     * @return unread counter
     */
    protected abstract int getUnreadCounter();

    protected void updateData(boolean isPushUpdating, boolean makeItemsRead) {
        updateData(isPushUpdating, false, makeItemsRead);
    }

    protected void updateData(final boolean isPullToRefreshUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        if (isAdded()) {
            needUpdate = false;
            mIsUpdating = true;
            onUpdateStart(isPullToRefreshUpdating || isHistoryLoad);

            final FeedRequest request = getRequest();
            registerRequest(request);

            final FeedAdapter<T> adapter = getListAdapter();
            if (adapter == null) {
                return;
            }
            FeedItem lastItem = adapter.getLastFeedItem();
            FeedItem firstItem = adapter.getFirstItem();

            if (isHistoryLoad && lastItem != null) {
                request.to = lastItem.id;
            }
            if (isPullToRefreshUpdating && firstItem != null) {
                request.from = firstItem.id;
            }
            request.leave = isReadFeedItems();
            request.callback(new DataApiHandler<FeedListData<T>>() {

                @Override
                protected FeedListData<T> parseResponse(ApiResponse response) {
                    return getFeedList(response.jsonResult);
                }

                @Override
                protected void success(FeedListData<T> data, IApiResponse response) {
                    processSuccessUpdate(data, isHistoryLoad, isPullToRefreshUpdating, makeItemsRead, request.getLimit());
                    isDataFromCache = false;
                }

                @Override
                public void fail(final int codeError, IApiResponse response) {
                    processFailUpdate(codeError, isHistoryLoad, adapter, isPullToRefreshUpdating);
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
                    refreshCompleted();
                    mBackgroundController.hide();
                    mIsUpdating = false;
                }

                @Override
                protected boolean isShowPremiumError() {
                    return !isForPremium();
                }
            }).exec();
        }
    }

    public void refreshCompleted() {
        if (mSwipeRefresh != null && mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }
    }


    protected boolean isReadFeedItems() {
        return false;
    }

    protected void processFailUpdate(int codeError, boolean isHistoryLoad, FeedAdapter<T> adapter, boolean isPullToRefreshUpdating) {
        Activity activity = getActivity();
        if (activity != null) {
            if (isHistoryLoad) {
                adapter.showRetryItem();
            }

            //Если ошибки обработаны на уровне фрагмента,
            // то не показываем стандартную ошибку
            boolean processError = processErrors(codeError);
            if ((!processError && !App.isOnline() && isPullToRefreshUpdating) || (!processError && App.isOnline())) {
                Utils.showErrorMessage();
            }
            onUpdateFail(isPullToRefreshUpdating || isHistoryLoad);
        }
    }

    protected void processSuccessUpdate(FeedListData<T> data, boolean isHistoryLoad, boolean isPullToRefreshUpdating, boolean makeItemsRead, int limit) {
        FeedAdapter<T> adapter = getListAdapter();
        if (!data.items.isEmpty()) {
            // store unread-state of first and last items
            // to use in next requests
            if (!mLastUnreadState.wasFromInited || isPullToRefreshUpdating) {
                mLastUnreadState.from = data.items.getFirst().unread;
                mLastUnreadState.wasFromInited = true;
            }
            mLastUnreadState.to = data.items.getLast().unread;
        }

        if (isHistoryLoad) {
            if (isDataFromCache) {
                adapter.removeAllData();
            } else {
                removeOldDuplicates(data);
            }
            adapter.addData(data);
        } else if (isPullToRefreshUpdating) {
            if (makeItemsRead) {
                makeAllItemsRead();
            }
            if (data.items.size() > 0) {
                if (adapter.getCount() >= limit) {
                    data.more = true;
                }
                removeOldDuplicates(data);
                adapter.addDataFirst(data);
            } else {
                adapter.notifyDataSetChanged();
            }
        } else {
            adapter.setData(data);
        }
        onUpdateSuccess(isPullToRefreshUpdating || isHistoryLoad);
        showListViewWithSuccessResponse();
    }

    private void showListViewWithSuccessResponse() {
        if (getIsNeedFirstShowListDelay()) {
            startListShowDelayTimer();
        } else {
            showListWithoutDelay();
        }
    }

    protected void removeOldDuplicates(FeedListData<T> data) {
        Iterator<T> feedsIterator = getListAdapter().getData().iterator();
        while (feedsIterator.hasNext()) {
            T feed = feedsIterator.next();
            for (T newFeed : data.items) {
                if (considerDublicates(feed, newFeed)) {
                    feedsIterator.remove();
                    break;
                }
            }
        }
    }

    protected boolean considerDublicates(T first, T second) {
        return first.id == null ? second.id == null : first.id.equals(second.id);
    }

    protected boolean isForPremium() {
        return false;
    }

    /**
     * Process error codes from server, put view in appropriate states
     *
     * @param codeError - error code from server
     * @return true if error is processed and don't need Toast message
     */
    private boolean processErrors(int codeError) {
        switch (codeError) {
            case ErrorCodes.PREMIUM_ACCESS_ONLY:
            case ErrorCodes.BLOCKED_SYMPATHIES:
            case ErrorCodes.BLOCKED_PEOPLE_NEARBY:
                clearCache();
                mListView.setVisibility(View.INVISIBLE);
                onLockedFeed(codeError);
                return true;
            default:
                if (getListAdapter() == null || getListAdapter().getDataForCache() == null || getListAdapter().getDataForCache().size() < 1) {
                    mRetryView.setVisibility(View.VISIBLE);
                }
                onFilledFeed();
                return false;
        }
    }

    protected FeedAdapter<T> getListAdapter() {
        return mListAdapter;
    }

    protected FeedListData<T> getFeedList(JSONObject response) {
        return new FeedListData<>(response, getFeedListItemClass());
    }

    protected FeedRequest getRequest() {
        return new FeedRequest(getFeedService(), getActivity()).setPreviousUnreadState(mLastUnreadState);
    }

    protected abstract FeedRequest.FeedService getFeedService();

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            showListViewWithSuccessResponse();
            mRetryView.setVisibility(View.GONE);
        }
        FeedAdapter adapter = getListAdapter();
        if (adapter != null && adapter.isEmpty()) {
            onEmptyFeed();
        } else {
            onFilledFeed();
        }
    }

    protected void onFilledFeed() {
        ViewStub stub = getEmptyFeedViewStub();
        if (stub != null) stub.setVisibility(View.GONE);
    }

    protected void onEmptyFeed(int errorCode) {
        initGagView();
        if (mInflated != null) {
            initEmptyFeedView(mInflated, errorCode);
            mInflated.setVisibility(View.VISIBLE);
        }
        mBackgroundController.hide();
    }

    protected void onLockedFeed(int errorCode) {
        initGagView();
        if (mInflated != null) {
            initLockedFeed(mInflated, errorCode);
            mInflated.setVisibility(View.VISIBLE);
        }
        mBackgroundController.hide();
    }

    private void initGagView() {
        if (mInflated == null) {
            ViewStub stub = getEmptyFeedViewStub();
            if (stub != null) {
                mInflated = stub.inflate();
            }
        }
    }

    protected abstract void initLockedFeed(View inflated, int errorCode);

    protected void onEmptyFeed() {
        onEmptyFeed(ErrorCodes.RESULT_OK);
    }

    protected abstract void initEmptyFeedView(View inflated, int errorCode);

    protected abstract int getEmptyFeedLayout();

    protected void makeAllItemsRead() {
        baseMakeAllItemsRead();
    }

    protected void baseMakeAllItemsRead() {
        getListAdapter().makeAllItemsRead();
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            showListWithoutDelay();
        }
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        BaseAdapter adapter = getListAdapter();
        if (adapter != null && !adapter.isEmpty()) {
            onFilledFeed();
        }
        if (!isPushUpdating) {
            mListView.setVisibility(View.INVISIBLE);
            mBackgroundController.show();
        }
    }

    private void initRetryViews() {
        if (mRetryView == null) {
            mRetryView = new RetryViewCreator.Builder(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryButtonClick(mRetryView.getView());
                }
            }).backgroundColor(getResources().getColor(R.color.bg_white)).build();
            mRetryView.setVisibility(View.GONE);
            mContainer.addView(mRetryView.getView());
        }
    }

    private void retryButtonClick(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
            mBackgroundController.startAnimation();
            updateData(false, true);
        }
    }

    protected void makeItemReadWithFeedId(String id) {
        FeedAdapter<T> adapter = getListAdapter();
        for (FeedItem item : adapter.getData()) {
            if (TextUtils.equals(item.id, id) && item.unread) {
                item.unread = false;
                adapter.notifyDataSetChanged();
            }
        }
    }

    protected void makeItemReadUserId(int uid, int readMessages) {
        FeedAdapter<T> adapter = getListAdapter();
        for (FeedItem item : adapter.getData()) {
            if (item.user != null && item.user.id == uid && item.unread) {
                int unread = item.unreadCounter - readMessages;
                if (unread > 0) {
                    item.unreadCounter = unread;
                } else {
                    item.unread = false;
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void updateDataAfterReceivingCounters() {
        String lastMethod = CountersManager.getInstance(getActivity()).getLastRequestMethod();
        if (lastMethod != null && !lastMethod.equals(NULL_METHOD) && !BannerRequest.SERVICE_NAME.equals(lastMethod) &&
                lastMethod.equals(getRequest().getServiceName())) {
            int counters = getUnreadCounter();
            if (counters > 0) {
                updateData(true, false);
            }
        }
        CountersManager.getInstance(getActivity()).setLastRequestMethod(NULL_METHOD);
    }

    @Override
    public void setNeedTitles(boolean needTitles) {
        super.setNeedTitles(needTitles);
    }

    protected boolean whetherDeleteIfBlacklisted() {
        return true;
    }

    public void updateOnResume() {
        needUpdate = true;
    }

    @Override
    public PageInfo.PageName getPageName() {
        return PageInfo.PageName.UNKNOWN_PAGE;
    }

    @Override
    public ViewGroup getContainerForAd() {
        View view = getView();
        if (view != null) {
            return (ViewGroup) getView().findViewById(R.id.banner_container_for_feeds);
        }
        return null;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            startInitialLoadIfNeed();
        }
    }

    public ListView getListView() {
        return mListView;
    }

    public void setDeletable(boolean state) {
        isDeletable = state;
    }

    public RetryViewCreator getRetryView() {
        return mRetryView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ChatActivity.REQUEST_CHAT) {
            onChatActivityResult(resultCode, data);
        }
    }

    protected void onChatActivityResult(int resultCode, Intent data) {

    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        Fragment fr = getParentFragment();
        // startActivityForResult adds to requestCode (after first 16 bits) index of fragment that has invoked startActivityForResult
        // then FragmentActivity passes onActivityResult to child fragment that has that index
        // but FeedFragment that is in TabbedFeedFragment is not Activity's straight child
        // so we need to call startActivityForResult from TabbedFeedFragment for Activity call passing
        // then TabbedFeedFragment.onActivityResult will pass it to its childsF
        if (fr != null && fr instanceof TabbedFeedFragment) {
            fr.startActivityForResult(intent, requestCode);
            // otherwise current fragment is straight child of Activity
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    private void setCurrentCounter(int value) {
        setCurrentCounterChanged(value);
        currentCounter = value;
    }

    private boolean setCurrentCounterChanged(int currentValue) {
        if (!isCurrentCounterChanged) {
            isCurrentCounterChanged = currentCounter != currentValue;
        }
        return isCurrentCounterChanged;
    }

    private boolean isCurrentCounterChanged() {
        boolean state = isCurrentCounterChanged;
        isCurrentCounterChanged = false;
        return state;
    }

    protected boolean isNeedFirstShowListDelay() {
        return false;
    }


    private boolean getIsNeedFirstShowListDelay() {
        if (isNeedFirstShowListDelay == null) {
            isNeedFirstShowListDelay = isNeedFirstShowListDelay();
        }
        return isNeedFirstShowListDelay;
    }

    private void startListShowDelayTimer() {
        stopListShowDelayTimer();
        mListShowDelayCountDownTimer = new CountDownTimer(FIRST_SHOW_LIST_DELAY, FIRST_SHOW_LIST_DELAY) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                showListWithoutDelay();
            }
        }.start();
    }

    private void stopListShowDelayTimer() {
        if (mListShowDelayCountDownTimer != null) {
            mListShowDelayCountDownTimer.cancel();
        }
    }

    public void showListWithoutDelay() {
        stopListShowDelayTimer();
        if (mListView != null) {
            mListView.setVisibility(View.VISIBLE);
            isNeedFirstShowListDelay = false;
            mBackgroundController.hide();
        }
    }

    private TextView getActionModeTitle() {
        if (mActionModeTitle == null) {
            mActionModeTitle = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.action_mode_text, null);
        }
        return mActionModeTitle;
    }

    private void setToolBarVisibility(boolean isVisible) {
        BaseFragmentActivity activity = ((BaseFragmentActivity) getActivity());
        if (activity != null) {
            activity.setToolBarVisibility(isVisible);
        }
    }

    protected boolean isSwipeRefreshEnable() {
        return true;
    }

    //TODO в момент перепиливания фидов надо оставить метод инициализирующий кнопку разболокировки за выполнение задания
    // start
    protected String getUnlockFunctionalityType() {
        return Utils.EMPTY;
    }

    @Nullable
    protected UnlockScreenCondition getUnlockScreenCondition(UnlockFunctionalityOption data) {
        return null;
    }

    protected void setUnlockButtonView(final Button view) {
    }
    // end
}