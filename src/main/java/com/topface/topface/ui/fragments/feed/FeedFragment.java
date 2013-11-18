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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BlackListAddManyRequest;
import com.topface.topface.requests.BlackListDeleteManyRequest;
import com.topface.topface.requests.BookmarkDeleteManyRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.DeleteFeedsRequest;
import com.topface.topface.requests.DeleteVisitorsRequest;
import com.topface.topface.requests.DialogDeleteManyRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.handlers.VipApiHandler;
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
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import org.json.JSONObject;

import java.util.List;

import static android.widget.AdapterView.OnItemClickListener;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment implements FeedAdapter.OnAvatarClickListener<T> {
    private static final int FEED_MULTI_SELECTION_LIMIT = 10;

    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;
    private RetryViewCreator mRetryView;
    private RelativeLayout mContainer;
    protected LockerView mLockView;

    private BroadcastReceiver readItemReceiver;

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
        mLockView = (LockerView) root.findViewById(R.id.llvFeedLoading);
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
        GCMUtils.cancelNotification(getActivity(), getTypeForGCM());
        return root;
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
    public void onResume() {
        super.onResume();
        if (getListAdapter().isNeedUpdate() || needUpdate) {
            updateData(false, true);
        }
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
    }

    protected void init() {
    }

    private void initBackground(View view) {
        // ListView background
        mBackgroundText = (TextView) view.findViewById(R.id.tvBackgroundText);

        mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(
                mBackgroundText.getCompoundDrawables()[0],
                getBackIcon(),
                mBackgroundText.getCompoundDrawables()[2],
                mBackgroundText.getCompoundDrawables()[3]
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(readItemReceiver);
    }

    protected abstract Drawable getBackIcon();

    abstract protected int getTypeForGCM();

    abstract protected int getTypeForCounters(); //TODO: Надо сделать что-то единообразное для того и того. Возможно стоит вынести типы фидов в константы

    protected int getLayout() {
        return R.layout.ac_feed;
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

        mListAdapter = getNewAdapter();
        getListAdapter().setOnAvatarClickListener(this);
        //Пауза загрузки изображений при прокрутке списка
        mListView.setOnScrollListener(
                new PauseOnScrollListener(
                        DefaultImageLoader.getInstance().getImageLoader(),
                        Static.PAUSE_DOWNLOAD_ON_SCROLL,
                        Static.PAUSE_DOWNLOAD_ON_FLING,
                        getListAdapter()
                )
        );

        ImageView iv = new ImageView(getActivity().getApplicationContext());
        iv.setBackgroundResource(R.drawable.im_header_item_list_bg);
        iv.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mListView.getRefreshableView().addHeaderView(iv);

        mListView.getRefreshableView().setAdapter(getListAdapter());
        mListView.getRefreshableView().setOnItemClickListener(getOnItemClickListener());
        mListView.getRefreshableView().setOnTouchListener(getListViewOnTouchListener());
        mListView.getRefreshableView().setOnItemLongClickListener(getOnItemLongClickListener());
    }

    /**
     * Метод возвращает новый инстанс адаптера
     *
     * @return адаптер фида
     */
    abstract protected FeedAdapter<T> getNewAdapter();

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
                if (getListAdapter().isMultiSelectionMode()) {
                    getListAdapter().onSelection((int) itemPosition);
                } else {
                    T item = (T) parent.getItemAtPosition(position);
                    if (item != null) {
                        if (!mIsUpdating && item.isRetrier()) {
                            updateUI(new Runnable() {
                                public void run() {
                                    getListAdapter().showLoaderItem();
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
                    ((ActionBarActivity) getActivity()).startSupportActionMode(mActionActivityCallback);
                    getListAdapter().startMultiSelection(getMultiSelectionLimit());
                    getListAdapter().onSelection((int) itemPosition);
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
            getListAdapter().setMultiSelectionListener(new MultiselectionController.IMultiSelectionListener() {
                @Override
                public void onSelected(int size) {
                    mActionMode.setTitle(Utils.getQuantityString(R.plurals.selected, size, size));
                }
            });
            getListAdapter().notifyDataSetChanged();
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
            switch (item.getItemId()) {
                case R.id.delete_feed:
                    onDeleteFeedItems(getListAdapter().getSelectedFeedIds(), getListAdapter().getSelectedItems());
                    break;
                case R.id.add_to_black_list:
                    onAddToBlackList(getListAdapter().getSelectedUsersIds(), getListAdapter().getSelectedItems());
                    break;
                case R.id.delete_from_blacklist:
                    onRemoveFromBlackList(getListAdapter().getSelectedUsersIds(), getListAdapter().getSelectedItems());
                    break;
                case R.id.delete_from_bookmarks:
                    onDeleteBookmarksItems(getListAdapter().getSelectedUsersIds(), getListAdapter().getSelectedItems());
                    break;
                case R.id.delete_dialogs:
                    onDeleteDialogItems(getListAdapter().getSelectedUsersIds(), getListAdapter().getSelectedItems());
                    break;
                case R.id.delete_visitors:
                    onDeleteVisitors(getListAdapter().getSelectedFeedIds(), getListAdapter().getSelectedItems());
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

    protected int getContextMenuLayoutRes() {
        return R.menu.feed_context_menu;
    }

    // CAB actions
    private void onRemoveFromBlackList(List<Integer> usersIds, final List<T> items) {
        mLockView.setVisibility(View.VISIBLE);
        new BlackListDeleteManyRequest(usersIds, getActivity())
                .callback(new VipApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        if (isAdded()) {
                            getListAdapter().removeItems(items);
                        }
                    }

                    @Override
                    public void always(IApiResponse response) {
                        if (isAdded()) {
                            if (mLockView != null) {
                                mLockView.setVisibility(View.GONE);
                            }
                        }
                    }

                }).exec();
    }

    private void onAddToBlackList(List<Integer> ids, final List<T> items) {
        new BlackListAddManyRequest(ids, getActivity())
                .callback(new VipApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        if (getListAdapter() != null) {
                            getListAdapter().removeItems(items);
                        }
                    }
                }).exec();
    }

    private void onDeleteFeedItems(List<String> ids, final List<T> items) {
        mLockView.setVisibility(View.VISIBLE);
        DeleteFeedsRequest dr = getDeleteRequest(ids, getActivity());
        if (dr == null) return;
        dr.callback(new SimpleApiHandler() {
            @Override
            public void success(IApiResponse response) {
                if (isAdded()) {
                    getListAdapter().removeItems(items);
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

    protected abstract DeleteFeedsRequest getDeleteRequest(List<String> ids, Context context);

    private void onDeleteBookmarksItems(final List<Integer> usersIds, final List<T> items) {
        BookmarkDeleteManyRequest request = new BookmarkDeleteManyRequest(getActivity(), usersIds);
        request.callback(new SimpleApiHandler() {
            @Override
            public void success(IApiResponse response) {
                getListAdapter().removeItems(items);
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

    protected void onDeleteDialogItems(final List<Integer> usersIds, final List<T> items) {
        new DialogDeleteManyRequest(usersIds, getActivity())
                .callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        getListAdapter().removeItems(items);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Debug.log(response.toString());
                        if (codeError != ErrorCodes.PREMIUM_ACCESS_ONLY) {
                            Utils.showErrorMessage(getActivity());
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

    private void onDeleteVisitors(List<String> selectedIds, final List<T> selectedItems) {
        new DeleteVisitorsRequest(selectedIds, getActivity())
                .callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        getListAdapter().removeItems(selectedItems);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Debug.log(response.toString());
                        if (codeError != ErrorCodes.PREMIUM_ACCESS_ONLY) {
                            Utils.showErrorMessage(getActivity());
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
            if (getListAdapter().isMultiSelectionMode()) {
                getListAdapter().onSelection(item);
            } else {
                startActivity(
                        ContainerActivity.getProfileIntent(item.user.id, item.id, getActivity())
                );
            }
        }
    }

    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        if (isBlockOnClosing() && !CacheProfile.premium) {
            showUpdateErrorMessage(ErrorCodes.PREMIUM_ACCESS_ONLY);
            return;
        }
        mIsUpdating = true;
        onUpdateStart(isPushUpdating || isHistoryLoad);

        FeedRequest request = getRequest();
        registerRequest(request);

        FeedItem lastItem = getListAdapter().getLastFeedItem();
        FeedItem firstItem = getListAdapter().getFirstItem();

        if (isHistoryLoad && lastItem != null) {
            request.to = lastItem.id;
        }
        if (isPushUpdating && firstItem != null) {
            request.from = firstItem.id;
        }

        final int limit = mListAdapter.getLimit();
        request.limit = limit;
        request.unread = isShowUnreadItems();
        request.callback(new DataApiHandler<FeedListData<T>>() {

            @Override
            protected FeedListData<T> parseResponse(ApiResponse response) {
                return getFeedList(response.jsonResult);
            }

            @Override
            protected void success(FeedListData<T> data, IApiResponse response) {
                if (isHistoryLoad) {
                    getListAdapter().addData(data);
                } else if (isPushUpdating) {
                    if (makeItemsRead) {
                        makeAllItemsRead();
                    }
                    if (data.items.size() > 0) {
                        if (getListAdapter().getCount() >= limit) {
                            data.more = true;
                        }

                        getListAdapter().addDataFirst(data);
                    }
                } else {
                    getListAdapter().setData(data);
                }
                onUpdateSuccess(isPushUpdating || isHistoryLoad);
                mListView.onRefreshComplete();
                mListView.setVisibility(View.VISIBLE);
                mIsUpdating = false;
            }

            @Override
            public void fail(final int codeError, IApiResponse response) {
                Activity activity = getActivity();
                if (activity != null) {
                    if (isHistoryLoad) {
                        getListAdapter().showRetryItem();
                    }
                    showUpdateErrorMessage(codeError);

                    //Если это не ошибка доступа, то показываем стандартное сообщение об ошибке
                    if (codeError != ErrorCodes.PREMIUM_ACCESS_ONLY) {
                        Utils.showErrorMessage(activity);
                    }
                    onUpdateFail(isPushUpdating || isHistoryLoad);
                    mListView.onRefreshComplete();
                    mIsUpdating = false;
                }
            }

            @Override
            protected boolean isShowPremiumError() {
                return !isForPremium();
            }
        }).exec();
    }

    protected boolean isForPremium() {
        return false;
    }

    private void showUpdateErrorMessage(int codeError) {
        mListView.setVisibility(View.INVISIBLE);
        switch (codeError) {
            case ErrorCodes.PREMIUM_ACCESS_ONLY:
                onEmptyFeed();
                break;
            default:
                mRetryView.setVisibility(View.VISIBLE);
                onFilledFeed();
                break;
        }
    }

    protected FeedAdapter<T> getListAdapter() {
        return mListAdapter;
    }

    protected boolean isShowUnreadItems() {
        return mDoubleButton != null && mDoubleButton.isRightButtonChecked();
    }

    protected abstract FeedListData<T> getFeedList(JSONObject response);

    private FeedRequest getRequest() {
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

            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                Drawable drawable = mBackgroundText.getCompoundDrawables()[0];
                if (drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).stop();
                }
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(getLoader0(),
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
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

    protected void onEmptyFeed() {
        ViewStub stub = getEmptyFeedViewStub();
        if (mInflated == null && stub != null) {
            mInflated = stub.inflate();
            initEmptyFeedView(mInflated);
        }
        if (mInflated != null) mInflated.setVisibility(View.VISIBLE);
        if (mBackgroundText != null) mBackgroundText.setVisibility(View.GONE);
    }

    protected abstract void initEmptyFeedView(View inflated);

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
            Drawable drawable = mBackgroundText.getCompoundDrawables()[0];
            if (drawable != null && drawable instanceof AnimationDrawable) {
                ((AnimationDrawable) drawable).stop();
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(getLoader0(),
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
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
            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(getLoader(),
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
            Drawable drawable = mBackgroundText.getCompoundDrawables()[0];
            if (drawable instanceof AnimationDrawable) {
                ((AnimationDrawable) drawable).start();
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
            });
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
        for (FeedItem item : getListAdapter().getData()) {
            if (TextUtils.equals(item.id, id) && item.unread) {
                item.unread = false;
                getListAdapter().notifyDataSetChanged();
            }
        }
    }

    private void updateDataAfterReceivingCounters(String lastMethod) {
        if (!lastMethod.equals(CountersManager.NULL_METHOD) && lastMethod.equals(getRequest().getServiceName())) {
            int counters = CountersManager.getInstance(getActivity()).getCounter(getTypeForCounters());
            if (counters > 0) {
                needUpdate = false;
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

    protected boolean isBlockOnClosing() {
        return false;
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
}
