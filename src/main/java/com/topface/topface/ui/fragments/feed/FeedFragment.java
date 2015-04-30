package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.topface.PullToRefreshBase;
import com.topface.PullToRefreshListView;
import com.topface.framework.imageloader.DefaultImageLoader;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.banners.BannersController;
import com.topface.topface.banners.IPageWithAds;
import com.topface.topface.banners.PageInfo;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.BlackListAndBookmarkHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedAnimatedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.adapters.MultiselectionController;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.BackgroundProgressBarController;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ad.NativeAd;
import com.topface.topface.utils.gcmutils.GCMUtils;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

import static android.widget.AdapterView.OnItemClickListener;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment
        implements FeedAdapter.OnAvatarClickListener<T>, IPageWithAds {
    private static final int FEED_MULTI_SELECTION_LIMIT = 100;

    private static final String FEEDS = "FEEDS";
    private static final String POSITION = "POSITION";
    private static final String HAS_AD = "HAS_AD";
    private static final String FEED_AD = "FEED_AD";

    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private BackgroundProgressBarController mBackgroundController = new BackgroundProgressBarController();
    protected boolean mIsUpdating;
    private RetryViewCreator mRetryView;
    private RelativeLayout mContainer;
    protected View mLockView;

    private BroadcastReceiver mReadItemReceiver;
    private BroadcastReceiver mBlacklistedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(BlackListAndBookmarkHandler.TYPE) &&
                    intent.getSerializableExtra(BlackListAndBookmarkHandler.TYPE)
                            .equals(BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST) && isAdded()) {
                int[] ids = intent.getIntArrayExtra(BlackListAndBookmarkHandler.FEED_IDS);
                boolean hasValue = intent.hasExtra(BlackListAndBookmarkHandler.VALUE);
                boolean value = intent.getBooleanExtra(BlackListAndBookmarkHandler.VALUE, false);
                if (ids != null && hasValue) {
                    if (value == whetherDeleteIfBlacklisted()) {
                        getListAdapter().removeByUserIds(ids);
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

    private BannersController mBannersController;
    private BroadcastReceiver mProfileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!CacheProfile.show_ad) {
                getListAdapter().removeAdItems();
            }
        }
    };

    private boolean isDeletable = true;
    private ViewStub mEmptyScreenStub;
    private boolean needUpdate = false;

    private ActionMode mActionMode;
    private FeedRequest.UnreadStatePair mLastUnreadState = new FeedRequest.UnreadStatePair();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);

        View root = inflater.inflate(getLayout(), null);
        mContainer = (RelativeLayout) root.findViewById(R.id.feedContainer);
        initNavigationBar();
        mLockView = root.findViewById(R.id.llvFeedLoading);
        mLockView.setVisibility(View.GONE);
        init();

        initViews(root);
        restoreInstanceState(saved);
        mReadItemReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String itemId = intent.getStringExtra(ChatFragment.INTENT_ITEM_ID);
                int userId = intent.getIntExtra(ChatFragment.INTENT_USER_ID, 0);
                if (userId == 0) {
                    if (!TextUtils.isEmpty(itemId)) {
                        makeItemReadWithFeedId(itemId);
                    } else {
                        String lastMethod = intent.getStringExtra(CountersManager.METHOD_INTENT_STRING);
                        if (!TextUtils.isEmpty(lastMethod)) {
                            updateDataAfterReceivingCounters(lastMethod);
                        }
                    }
                } else {
                    makeItemReadUserId(userId, intent.getIntExtra(ChatFragment.LOADED_MESSAGES, 0));
                }
            }
        };
        IntentFilter filter = new IntentFilter(ChatFragment.MAKE_ITEM_READ);
        filter.addAction(ChatFragment.MAKE_ITEM_READ_BY_UID);
        filter.addAction(CountersManager.UPDATE_COUNTERS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReadItemReceiver, filter);
        for (int type : getTypesForGCM()) {
            GCMUtils.cancelNotification(getActivity(), type);
        }
        return root;
    }

    @SuppressWarnings("unchecked")
    protected void restoreInstanceState(Bundle saved) {
        if (saved != null) {
            if (CacheProfile.show_ad) {
                mListAdapter.setHasFeedAd(saved.getBoolean(HAS_AD));
                mListAdapter.setFeedAd(saved.<NativeAd>getParcelable(FEED_AD));
            }
            Parcelable[] feeds = saved.getParcelableArray(FEEDS);
            FeedList<T> feedsList = new FeedList<>();
            for (Parcelable p : feeds) {
                T feed = (T) p;
                if (feed.isAd() && !CacheProfile.show_ad) {
                    continue;
                }
                feedsList.add((T) p);
            }
            mListAdapter.setData(feedsList);
            mListView.getRefreshableView().setSelection(saved.getInt(POSITION, 0));
            if (!mListAdapter.isEmpty()) {
                mBackgroundController.hide();
            }
        }
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
        initListView(root);
        initRetryViews();
        initViewStubForEmptyFeed(root);
    }

    protected void initViewStubForEmptyFeed(View root) {
        mEmptyScreenStub = (ViewStub) root.findViewById(R.id.stubForEmptyFeed);
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFloatBlock();
    }

    protected void initFloatBlock() {
        if (!getListAdapter().isNeedFeedAd()) {
            mBannersController = new BannersController(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerGcmReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBlacklistedReceiver, new IntentFilter(BlackListAndBookmarkHandler.UPDATE_USER_CATEGORY));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
    }

    @Override
    public void onResume() {
        super.onResume();
        FeedAdapter<T> adapter = getListAdapter();
        if (adapter.isNeedUpdate() || needUpdate) {
            updateData(false, true);
        }
        // try update list if last visible item is loader,
        // and loading was probably interrupted
        adapter.loadOlderItemsIfNeeded(
                mListView.getRefreshableView().getFirstVisiblePosition(),
                mListView.getRefreshableView().getChildCount(),
                adapter.getCount());
        if (!adapter.isEmpty()) {
            adapter.refreshAdItem();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        finishMultiSelection();
        if (mListView.isRefreshing()) {
            mListView.onRefreshComplete();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBannersController != null) {
            mBannersController.onDestroy();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReadItemReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBlacklistedReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileUpdateReceiver);
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
            outState.putInt(POSITION, mListView.getRefreshableView().getFirstVisiblePosition());
            outState.putBoolean(HAS_AD, mListAdapter.hasFeedAd());
            outState.putParcelable(FEED_AD, mListAdapter.getFeedAd());
        }
    }

    protected void init() {
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

    private void initListView(View view) {
        // ListView    	

        mListView = (PullToRefreshListView) view.findViewById(R.id.lvFeedList);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                updateData(true, true);
            }
        });

        mListAdapter = createNewAdapter();

        FeedAnimatedAdapter animationAdapter = new FeedAnimatedAdapter(mListAdapter);
        animationAdapter.setAbsListView(mListView.getRefreshableView());

        FeedAdapter<T> adapter = getListAdapter();
        adapter.setOnAvatarClickListener(this);
        //Пауза загрузки изображений при прокрутке списка
        mListView.setOnScrollListener(
                new PauseOnScrollListener(
                        DefaultImageLoader.getInstance(App.getContext()).getImageLoader(),
                        Static.PAUSE_DOWNLOAD_ON_SCROLL,
                        Static.PAUSE_DOWNLOAD_ON_FLING,
                        adapter
                )
        );

        mListView.getRefreshableView().setAdapter(animationAdapter);
        mListView.getRefreshableView().setOnItemClickListener(getOnItemClickListener());
        mListView.getRefreshableView().setOnTouchListener(getListViewOnTouchListener());
        mListView.getRefreshableView().setOnItemLongClickListener(getOnItemLongClickListener());
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

    protected OnItemClickListener getOnItemClickListener() {
        return new OnItemClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long itemPosition) {
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
        };
    }

    protected AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long itemPosition) {
                if (isDeletable) {
                    FeedAdapter<T> adapter = getListAdapter();
                    ((ActionBarActivity) getActivity()).startSupportActionMode(mActionActivityCallback);
                    adapter.startMultiSelection(getMultiSelectionLimit());
                    adapter.onSelection((int) itemPosition);
                    return true;
                }
                return false;
            }

        };
    }

    protected int getMultiSelectionLimit() {
        return FEED_MULTI_SELECTION_LIMIT;
    }

    public void finishMultiSelection() {
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    private ActionMode.Callback mActionActivityCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            FeedAdapter<T> adapter = getListAdapter();
            adapter.setMultiSelectionListener(new MultiselectionController.IMultiSelectionListener() {
                @Override
                public void onSelected(int size, boolean overlimit) {
                    if (overlimit) {
                        Toast.makeText(App.getContext(), R.string.maximum_number_of_users, Toast.LENGTH_LONG).show();
                    }
                    if (mActionMode != null) {
                        mActionMode.setTitle(Utils.getQuantityString(R.plurals.selected, size, size));
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
                    onAddToBlackList(adapter.getSelectedUsersIds());
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
        }
    };

    /**
     * В данный момент у нас проблема с id в диалогах, поэтому в DialogsFragment этот метод переопределен
     */
    protected List<String> getSelectedFeedIds(FeedAdapter<T> adapter) {
        return adapter.getSelectedFeedIds();
    }

    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu;
    }

    private void onAddToBlackList(List<Integer> ids) {
        ApiRequest r = new BlackListAddRequest(ids, getActivity()).
                callback(new BlackListAndBookmarkHandler(getActivity(),
                        BlackListAndBookmarkHandler.ActionTypes.BLACK_LIST,
                        ids,
                        true));
        r.handler.setOnCompleteAction(new ApiHandler.CompleteAction() {
            @Override
            public void onCompleteAction() {
                if (mLockView != null) {
                    mLockView.setVisibility(View.GONE);
                }
            }
        });
        mLockView.setVisibility(View.VISIBLE);
        r.exec();
    }

    private void onDeleteFeedItems(List<String> ids, final List<T> items) {
        DeleteAbstractRequest dr = getDeleteRequest(ids);
        //Если удаление не поддерживается данным потомком,
        //то у нас ошибка с показом меню (есть кнопка там, где удаление не поддерживается)
        //и нужно сообщить пользователю, что удалить не получится
        if (dr == null) {
            Utils.showErrorMessage();
            return;
        }
        mLockView.setVisibility(View.VISIBLE);
        if (dr.handler != null) {
            dr.handler.setOnCompleteAction(new ApiHandler.CompleteAction() {
                @Override
                public void onCompleteAction() {
                    if (mLockView != null) {
                        mLockView.setVisibility(View.GONE);
                    }
                }
            });
            dr.exec();
            return;
        }
        dr.callback(new SimpleApiHandler() {
            @Override
            public void success(IApiResponse response) {
                if (isAdded()) {
                    getListAdapter().removeItems(items);
                    if (getListAdapter().getData().size() == 0) {
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

    protected OnTouchListener getListViewOnTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        };
    }

    protected void onFeedItemClick(FeedItem item) {
        //Open chat activity
        if (!item.user.isEmpty()) {
            Intent intent = ChatActivity.createIntent(getActivity(), item.user, item.id);
            getActivity().startActivityForResult(intent, ChatActivity.INTENT_CHAT);
        }
    }

    public void onAvatarClick(T item, View view) {
        if (isAdded()) {
            FeedAdapter<T> adapter = getListAdapter();
            if (adapter.isMultiSelectionMode()) {
                adapter.onSelection(item);
            } else {
                startActivity(CacheProfile.getOptions().autoOpenGallery.createIntent(item.user.id, item.user.photosCount, item.id, item.user.photo, getActivity()));
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
                    || hasUnread())
                    && !mIsUpdating) {
                updateData(false, true);
            }
        }
    }

    /**
     * @return true if got unread feeds, calculated from counters
     */
    private boolean hasUnread() {
        return (getUnreadCounter() > 0);
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
        if (getUserVisibleHint()) {
            needUpdate = false;
            mIsUpdating = true;
            onUpdateStart(isPullToRefreshUpdating || isHistoryLoad);

            final FeedRequest request = getRequest();
            registerRequest(request);

            final FeedAdapter<T> adapter = getListAdapter();
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
                }

                @Override
                public void fail(final int codeError, IApiResponse response) {
                    processFailUpdate(codeError, isHistoryLoad, adapter, isPullToRefreshUpdating);
                }

                @Override
                public void always(IApiResponse response) {
                    super.always(response);
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
            if (!processErrors(codeError)) {
                Utils.showErrorMessage();
            }
            onUpdateFail(isPullToRefreshUpdating || isHistoryLoad);
            mListView.onRefreshComplete();
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
            removeOldDublicates(data);
            adapter.addData(data);
        } else if (isPullToRefreshUpdating) {
            if (makeItemsRead) {
                makeAllItemsRead();
            }
            if (data.items.size() > 0) {
                if (adapter.getCount() >= limit) {
                    data.more = true;
                }
                removeOldDublicates(data);
                adapter.addDataFirst(data);
            } else {
                adapter.notifyDataSetChanged();
            }
        } else {
            adapter.setData(data);
        }
        onUpdateSuccess(isPullToRefreshUpdating || isHistoryLoad);
        mListView.onRefreshComplete();
        mListView.setVisibility(View.VISIBLE);
    }

    protected void removeOldDublicates(FeedListData<T> data) {
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
        mListView.setVisibility(View.INVISIBLE);
        switch (codeError) {
            case ErrorCodes.PREMIUM_ACCESS_ONLY:
            case ErrorCodes.BLOCKED_SYMPATHIES:
            case ErrorCodes.BLOCKED_PEOPLE_NEARBY:
                onEmptyFeed(codeError);
                return true;
            default:
                mRetryView.setVisibility(View.VISIBLE);
                onFilledFeed();
                return false;
        }
    }

    protected FeedAdapter<T> getListAdapter() {
        return mListAdapter;
    }

    protected abstract FeedListData<T> getFeedList(JSONObject response);

    protected FeedRequest getRequest() {
        return new FeedRequest(getFeedService(), getActivity()).setPreviousUnreadState(mLastUnreadState);
    }

    protected abstract FeedRequest.FeedService getFeedService();

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mRetryView.setVisibility(View.GONE);
        }
        if (getListAdapter().isEmpty()) {
            onEmptyFeed();
        } else {
            onFilledFeed();
        }
    }

    protected void onFilledFeed() {
        ViewStub stub = getEmptyFeedViewStub();
        if (stub != null) stub.setVisibility(View.GONE);
    }

    private View mInflated;

    protected void onEmptyFeed(int errorCode) {
        ViewStub stub = getEmptyFeedViewStub();
        if (mInflated == null && stub != null) {
            mInflated = stub.inflate();
            initEmptyFeedView(mInflated, errorCode);
        }
        if (mInflated != null) {
            mInflated.setVisibility(mListAdapter != null && mListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
            initEmptyFeedView(mInflated, errorCode);
        }
        mBackgroundController.hide();
    }

    protected void onEmptyFeed() {
        onEmptyFeed(ErrorCodes.RESULT_OK);
    }

    protected abstract void initEmptyFeedView(View inflated, int errorCode);

    protected void initEmptyFeedView(View inflated) {
        initEmptyFeedView(inflated, ErrorCodes.RESULT_OK);
    }

    protected abstract int getEmptyFeedLayout();

    protected void makeAllItemsRead() {
        getListAdapter().makeAllItemsRead();
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        if (!getListAdapter().isEmpty()) {
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

    private void updateDataAfterReceivingCounters(String lastMethod) {
        if (!lastMethod.equals(CountersManager.NULL_METHOD) && lastMethod.equals(getRequest().getServiceName())) {
            int counters = CountersManager.getInstance(getActivity()).getCounter(getFeedType());
            if (counters > 0) {
                updateData(true, false);
            }
        }
    }

    protected static void initButtonForBlockedScreen(Button button, String buttonText, View.OnClickListener listener) {
        initButtonForBlockedScreen(null, null, button, buttonText, listener);
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

    public PullToRefreshListView getListView() {
        return mListView;
    }

    public void setDeletable(boolean state) {
        isDeletable = state;
    }

    public RetryViewCreator getRetryView() {
        return mRetryView;
    }
}
