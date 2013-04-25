package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.requests.*;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.requests.handlers.VipApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.blocks.FilterBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import org.json.JSONObject;

import static android.widget.AdapterView.OnItemClickListener;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment implements FeedAdapter.OnAvatarClickListener<T> {
    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;
    private RetryViewCreator mRetryView;
    private RetryViewCreator mVipRetryView;
    private RelativeLayout mContainer;
    protected LockerView mLockView;

    private BroadcastReceiver readItemReceiver;

    protected String[] editButtonsNames;

    protected final int DELETE_BUTTON = 0;
    protected final int BLACK_LIST_BUTTON = 1;
    protected final int MUTUAL_BUTTON = 2;

    private FloatBlock mFloatBlock;

    protected boolean isDeletable = true;
    private Drawable mLoader0;
    private AnimationDrawable mLoader;
    private ActionBar mActionBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(getLayout(), null);
        mContainer = (RelativeLayout) root.findViewById(R.id.feedContainer);
        initNavigationBar(root);
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
    }


    protected void initFloatBlock(ViewGroup view) {
        mFloatBlock = new FloatBlock(this, view);
        mFloatBlock.onCreate();
    }

    protected void initNavigationBar(View view) {
        // Navigation bar
        mActionBar = getActionBar(view);
        mActionBar.showHomeButton((View.OnClickListener) getActivity());
        mActionBar.setTitleText(getString(getTitle()));
    }


    @Override
    public void onResume() {
        super.onResume();
        if (getListAdapter().isNeedUpdate()) {
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

    abstract protected int getTitle();

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

        mListView.scrollBy(0, 2);
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
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem item = (FeedItem) parent.getItemAtPosition(position);
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
        };
    }

    protected AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long itemPosition) {
                if (isDeletable) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.general_spinner_title).setItems(
                            getLongTapActions(),
                            getLongTapActionsListener((int) itemPosition)
                    );
                    builder.create().show();
                }
                return false;
            }

        };
    }

    protected DialogInterface.OnClickListener getLongTapActionsListener(final int position) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DELETE_BUTTON:
                        mLockView.setVisibility(View.VISIBLE);
                        onDeleteItem(position);
                        break;
                    case BLACK_LIST_BUTTON:
                        onAddToBlackList(position);
                        break;
                }
            }
        };
    }

    protected String[] getLongTapActions() {
        if (editButtonsNames == null) {
            editButtonsNames = new String[]{getString(R.string.general_delete_title), getString(R.string.black_list_add)};
        }
        return editButtonsNames;
    }

    protected void onAddToBlackList(final int position) {
        new BlackListAddRequest(getItem(position).user.id, getActivity())
                .callback(new VipApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        if (getListAdapter() != null) {
                            getListAdapter().removeItem(position);
                        }
                    }
                }).exec();
    }

    protected void onDeleteItem(final int position) {
        DeleteRequest dr = new DeleteRequest(getItem(position).id, getActivity());
        dr.callback(new SimpleApiHandler() {
            @Override
            public void success(ApiResponse response) {
                if (isAdded()) {
                    mLockView.setVisibility(View.GONE);
                    getListAdapter().removeItem(position);
                }
            }

            @Override
            public void always(ApiResponse response) {
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
            intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
            intent.putExtra(ChatFragment.INTENT_ITEM_ID, item.id);
            getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
        }
    }

    public void onAvatarClick(T item, View view) {
        if (isAdded()) {
            startActivity(
                    ContainerActivity.getProfileIntent(item.user.id, item.id, getActivity())
            );
        }
    }

    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
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
        request.limit = FeedAdapter.LIMIT;
        request.unread = isShowUnreadItems();
        request.callback(new DataApiHandler<FeedListData<T>>() {

            @Override
            protected FeedListData<T> parseResponse(ApiResponse response) {
                return getFeedList(response.jsonResult);
            }

            @Override
            protected void success(FeedListData<T> data, ApiResponse response) {
                if (isHistoryLoad) {
                    getListAdapter().addData(data);
                } else if (isPushUpdating) {
                    if (makeItemsRead) {
                        makeAllItemsRead();
                    }
                    if (data.items.size() > 0) {
                        if (getListAdapter().getCount() >= FeedAdapter.LIMIT) {
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
                if (mActionBar != null) {
                    mActionBar.refreshNotificators();
                }
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {
                Activity activity = getActivity();
                if (activity != null) {
                    if (isHistoryLoad) {
                        getListAdapter().showRetryItem();
                    }
                    showUpdateErrorMessage(codeError);

                    //Если это не ошибка доступа, то показываем стандартное сообщение об ошибке
                    if (codeError != ApiResponse.PREMIUM_ACCESS_ONLY) {
                        Utils.showErrorMessage(activity);
                    }
                    onUpdateFail(isPushUpdating || isHistoryLoad);
                    mListView.onRefreshComplete();
//                    mListView.setVisibility(View.VISIBLE);
                    mIsUpdating = false;
                }
            }

        }).exec();
    }

    private void showUpdateErrorMessage(int codeError) {
        mListView.setVisibility(View.INVISIBLE);
        switch (codeError) {
            case ApiResponse.PREMIUM_ACCESS_ONLY:
                if (FeedFragment.this instanceof VisitorsFragment) {
                    mVipRetryView.setText(getString(R.string.buying_vip_info));
                } else {
                    mVipRetryView.setText(getString(R.string.general_premium_access_error));
                }
                mVipRetryView.getView().setVisibility(View.VISIBLE);
                break;

            default:
                mRetryView.setVisibility(View.VISIBLE);
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
        new FilterBlock((ViewGroup) view, R.id.loControlsGroup, mActionBar, R.id.loToolsBar);
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
            mVipRetryView.setVisibility(View.GONE);
            if (getListAdapter().isEmpty()) {
                mBackgroundText.setText(getEmptyFeedText());
            } else {
                mBackgroundText.setVisibility(View.INVISIBLE);
            }

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
    }

    abstract protected int getEmptyFeedText();

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

    protected void decrementCounters() {

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

        if (mVipRetryView == null) {
            mVipRetryView = RetryViewCreator.createBlueButtonRetryView(getActivity(), Static.EMPTY,
                    getString(R.string.buying_vip_status), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                    startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                }
            }
            );
            mVipRetryView.setVisibility(View.GONE);
            mContainer.addView(mVipRetryView.getView());
        }
    }

    private void showUpdateErrorMessage(RetryViewCreator view) {
        if (view != null) {
            mListView.setVisibility(View.INVISIBLE);
            view.setVisibility(View.VISIBLE);
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
                decrementCounters();
            }
        }
    }

    private void updateDataAfterReceivingCounters(String lastMethod) {
        if (!lastMethod.equals(CountersManager.NULL_METHOD) && !lastMethod.equals(getRequest().getServiceName())) {
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

}
