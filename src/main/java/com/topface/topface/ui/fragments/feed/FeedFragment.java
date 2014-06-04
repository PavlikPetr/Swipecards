package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.topface.framework.utils.Debug;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteAbstractRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.adapters.MultiselectionController;
import com.topface.topface.ui.blocks.FilterBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;

import org.json.JSONObject;

import java.util.List;

import static android.widget.AdapterView.OnItemClickListener;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment implements FeedAdapter.OnAvatarClickListener<T> {
    private static final int FEED_MULTI_SELECTION_LIMIT = 100;

    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;
    private RetryViewCreator mRetryView;
    private RelativeLayout mContainer;
    protected View mLockView;

    private BroadcastReceiver readItemReceiver;
    private BroadcastReceiver mBlacklistedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(ContainerActivity.TYPE) &&
                    intent.getSerializableExtra(ContainerActivity.TYPE)
                            .equals(ContainerActivity.ActionTypes.BLACK_LIST) && isAdded()) {
                int[] ids = intent.getIntArrayExtra(ContainerActivity.FEED_IDS);
                boolean hasValue = intent.hasExtra(ContainerActivity.VALUE);
                boolean value = intent.getBooleanExtra(ContainerActivity.VALUE, false);
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
            for (int type : getTypesForGCM()) {
                GCMUtils.cancelNotification(getActivity(), type);
            }
            updateData(true, false);
        }
    };

    private FloatBlock mFloatBlock;

    protected boolean isDeletable = true;
    private Drawable mLoader0;
    private AnimationDrawable mLoader;
    private ViewStub mEmptyScreenStub;
    private boolean needUpdate = false;

    private ActionMode mActionMode;
    private FilterBlock mFilterBlock;

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
        readItemReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String itemId = intent.getStringExtra(ChatFragment.INTENT_ITEM_ID);
                if (itemId != null) {
                    makeItemReadWithId(itemId);
                } else {
                    needUpdate = true;
                    String lastMethod = intent.getStringExtra(CountersManager.METHOD_INTENT_STRING);
                    if (lastMethod != null) {
                        updateDataAfterReceivingCounters(lastMethod);
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ChatFragment.MAKE_ITEM_READ);
        filter.addAction(CountersManager.UPDATE_COUNTERS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(readItemReceiver, filter);
        for (int type : getTypesForGCM()) {
            GCMUtils.cancelNotification(getActivity(), type);
        }
        registerGcmReceiver();
        return root;
    }

    private void registerGcmReceiver() {
        String action = getGcmUpdateAction();
        if (action != null) {
            getActivity().registerReceiver(mGcmReceiver, new IntentFilter(action));
        }
    }

    protected String getGcmUpdateAction() {
        return null;
    }

    private void initViews(View root) {
        initBackground(root);
        initFilter(root);
        initListView(root);
        initFloatBlock((ViewGroup) root);
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

    protected void initFloatBlock(ViewGroup view) {
        mFloatBlock = new FloatBlock(this, view);
        mFloatBlock.onCreate();
    }

    protected void initNavigationBar() {
        setActionBarTitles(getTitle());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBlacklistedReceiver, new IntentFilter(ContainerActivity.UPDATE_USER_CATEGORY));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getListAdapter().isNeedUpdate() || needUpdate) {
            updateData(false, true);
        }
        getListAdapter().loadOlderItems();
        if (mFloatBlock != null) {
            mFloatBlock.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFloatBlock != null) {
            mFloatBlock.onPause();
        }
        if (mActionMode != null) mActionMode.finish();
        if (mListView.isRefreshing()) {
            mListView.onRefreshComplete();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatBlock != null) {
            mFloatBlock.onDestroy();
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBlacklistedReceiver);
    }

    protected void init() {
    }

    private void initBackground(View view) {
        // ListView background
        mBackgroundText = (TextView) view.findViewById(R.id.tvBackgroundText);

        if (mBackgroundText != null) {
            Drawable[] drawables = mBackgroundText.getCompoundDrawables();
            if (drawables != null) {
                mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(
                        drawables[0],
                        getBackIcon(),
                        drawables[2],
                        drawables[3]
                );
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(readItemReceiver);
        if (getGcmUpdateAction() != null) {
            getActivity().unregisterReceiver(mGcmReceiver);
        }
    }

    protected abstract Drawable getBackIcon();

    protected int[] getTypesForGCM() {
        return new int[]{GCMUtils.GCM_TYPE_UNKNOWN};
    }

    abstract protected int getTypeForCounters();

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
        FeedAdapter<T> adapter = getListAdapter();
        adapter.setOnAvatarClickListener(this);
        //Пауза загрузки изображений при прокрутке списка
        mListView.setOnScrollListener(
                new PauseOnScrollListener(
                        DefaultImageLoader.getInstance().getImageLoader(),
                        Static.PAUSE_DOWNLOAD_ON_SCROLL,
                        Static.PAUSE_DOWNLOAD_ON_FLING,
                        adapter
                )
        );

        ImageView iv = new ImageView(getActivity().getApplicationContext());
        iv.setBackgroundResource(R.drawable.im_header_item_list_bg);
        iv.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mListView.getRefreshableView().addHeaderView(iv);

        mListView.getRefreshableView().setAdapter(adapter);
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

    private ActionMode.Callback mActionActivityCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mActionMode = mode;
            FeedAdapter<T> adapter = getListAdapter();
            adapter.setMultiSelectionListener(new MultiselectionController.IMultiSelectionListener() {
                @Override
                public void onSelected(int size) {
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
        BlackListAddRequest r = new BlackListAddRequest(ids, getActivity());
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
            Intent intent = new Intent(getActivity(), ContainerActivity.class);
            intent.putExtra(ChatFragment.INTENT_USER_ID, item.user.id);
            intent.putExtra(ChatFragment.INTENT_USER_NAME, item.user.first_name);
            intent.putExtra(ChatFragment.INTENT_USER_SEX, item.user.sex);
            intent.putExtra(ChatFragment.INTENT_USER_AGE, item.user.age);
            intent.putExtra(ChatFragment.INTENT_USER_CITY, item.user.city.name);
            intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, ((Object) this).getClass().getSimpleName());
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, item.id);
            getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
        }
    }

    public void onAvatarClick(T item, View view) {
        if (isAdded()) {
            FeedAdapter<T> adapter = getListAdapter();
            if (adapter.isMultiSelectionMode()) {
                adapter.onSelection(item);
            } else {
                startActivity(
                        ContainerActivity.getProfileIntent(item.user.id, item.id, getActivity())
                );
            }
        }
    }

    protected void updateData(final boolean isPullToRefreshUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        needUpdate = false;
        mIsUpdating = true;
        onUpdateStart(isPullToRefreshUpdating || isHistoryLoad);

        FeedRequest request = getRequest();
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

        final int limit = adapter.getLimit();
        request.limit = limit;
        request.unread = isShowUnreadItemsSelected();
        request.callback(new DataApiHandler<FeedListData<T>>() {

            @Override
            protected FeedListData<T> parseResponse(ApiResponse response) {
                return getFeedList(response.jsonResult);
            }

            @Override
            protected void success(FeedListData<T> data, IApiResponse response) {
                processSuccessUpdate(data, isHistoryLoad, isPullToRefreshUpdating, makeItemsRead, limit);
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                processFailUpdate(codeError, isHistoryLoad, adapter, isPullToRefreshUpdating);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                mIsUpdating = false;
            }

            @Override
            protected boolean isShowPremiumError() {
                return !isForPremium();
            }
        }).exec();
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
        if (isHistoryLoad) {
            adapter.addData(data);
        } else if (isPullToRefreshUpdating) {
            if (makeItemsRead) {
                makeAllItemsRead();
            }
            if (data.items.size() > 0) {
                if (adapter.getCount() >= limit) {
                    data.more = true;
                }
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

    protected boolean isShowUnreadItemsSelected() {
        return mDoubleButton != null && mDoubleButton.isRightButtonChecked();
    }

    protected abstract FeedListData<T> getFeedList(JSONObject response);

    protected FeedRequest getRequest() {
        return new FeedRequest(getFeedService(), getActivity());
    }

    protected abstract FeedRequest.FeedService getFeedService();

    protected void updateData(boolean isPushUpdating, boolean makeItemsRead) {
        updateData(isPushUpdating, false, makeItemsRead);
    }

    protected void initFilter(View view) {
        mFilterBlock = new FilterBlock((ViewGroup) view, R.id.loControlsGroup, R.id.loToolsBar);
        initDoubleButton(view);
    }

    protected void initDoubleButton(View view) {
        if (view == null) {
            return;
        }
        // Double Button
        mDoubleButton = (DoubleBigButton) view.findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.general_dbl_all));
        mDoubleButton.setRightText(getString(R.string.general_dbl_unread));
        mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mDoubleButton.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(false, true);
            }
        });
        mDoubleButton.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(false, true);
            }
        });
    }

    @Override
    protected void onUpdateSuccess(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mRetryView.setVisibility(View.GONE);

            Drawable[] drawables = mBackgroundText.getCompoundDrawables();
            if (drawables != null) {
                if (drawables[0] != null) {
                    Drawable drawable = drawables[0];
                    if (drawable instanceof AnimationDrawable) {
                        ((AnimationDrawable) drawable).stop();
                    }
                }

                mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(getLoader0(),
                        drawables[1],
                        drawables[2],
                        drawables[3]);
            }
            setFilterSwitcherState(true);
        }

        if (getListAdapter().isEmpty()) {
            onEmptyFeed();
        } else {
            onFilledFeed();
        }
    }

    protected void onFilledFeed() {
        if (mBackgroundText != null) mBackgroundText.setVisibility(View.GONE);
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
            mInflated.setVisibility(View.VISIBLE);
            initEmptyFeedView(mInflated, errorCode);
        }
        if (mBackgroundText != null) mBackgroundText.setVisibility(View.GONE);
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
        for (FeedItem item : getListAdapter().getData()) {
            item.unread = false;
        }
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mBackgroundText.setText("");
            Drawable[] drawables = mBackgroundText.getCompoundDrawables();
            if (drawables != null) {
                Drawable drawable = drawables[0];
                if (drawable != null && drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).stop();
                }

                mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(getLoader0(),
                        drawables[1],
                        drawables[2],
                        drawables[3]);
            }
            setFilterSwitcherState(true);
        }
    }

    @Override
    protected void onUpdateStart(boolean isPushUpdating) {
        onFilledFeed();
        if (!isPushUpdating) {
            mListView.setVisibility(View.INVISIBLE);
            mBackgroundText.setVisibility(View.VISIBLE);
            mBackgroundText.setText(R.string.general_dialog_loading);
            Drawable[] drawables = mBackgroundText.getCompoundDrawables();
            if (drawables != null) {
                AnimationDrawable drawable = getLoader();
                mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(drawable,
                        drawables[1],
                        drawables[2],
                        drawables[3]);
                drawable.start();
            }
            setFilterSwitcherState(false);
        }
    }

    protected void setFilterSwitcherState(boolean clickable) {
        if (mDoubleButton != null) {
            mDoubleButton.setClickable(clickable);
        }
    }

    private void initRetryViews() {
        if (mRetryView == null) {
            mRetryView = RetryViewCreator.createDefaultRetryView(getActivity(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryButtonClick(mRetryView.getView());
                }
            }, getResources().getColor(R.color.bg_main));
            mRetryView.setVisibility(View.GONE);
            mContainer.addView(mRetryView.getView());
        }
    }

    private void retryButtonClick(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
            updateData(false, true);
        }
    }

    private void makeItemReadWithId(String id) {
        FeedAdapter<T> adapter = getListAdapter();
        for (FeedItem item : adapter.getData()) {
            if (TextUtils.equals(item.id, id) && item.unread) {
                item.unread = false;
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void updateDataAfterReceivingCounters(String lastMethod) {
        if (!lastMethod.equals(CountersManager.NULL_METHOD) && lastMethod.equals(getRequest().getServiceName())) {
            int counters = CountersManager.getInstance(getActivity()).getCounter(getTypeForCounters());
            if (counters > 0) {
                updateData(true, false);
            }
        }
    }

    private Drawable getLoader0() {
        if (mLoader0 == null && isAdded()) {
            mLoader0 = getResources().getDrawable(R.drawable.loader0);
        }
        return mLoader0;
    }


    private AnimationDrawable getLoader() {
        if (mLoader == null && isAdded()) {
            Drawable drawable = getResources().getDrawable(R.drawable.loader);
            if (drawable instanceof AnimationDrawable) {
                mLoader = (AnimationDrawable) drawable;
            }
        }
        return mLoader;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                mFilterBlock.openControls();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    protected boolean whetherDeleteIfBlacklisted() {
        return true;
    }

    public void updateOnResume() {
        needUpdate = true;
    }
}
