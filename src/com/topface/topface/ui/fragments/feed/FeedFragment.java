package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.*;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.LoadingListAdapter;
import com.topface.topface.ui.blocks.FilterBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.ProfileFragment;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;
import org.json.JSONObject;

import static android.widget.AdapterView.OnItemClickListener;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment implements FeedAdapter.OnAvatarClickListener<T> {
    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;
    private RetryView updateErrorMessage;
    private RelativeLayout mContainer;
    protected LockerView mLockView;

    protected static boolean mEditMode;

    private BroadcastReceiver readItemReceiver;

    protected String[] editButtonsNames;

    private final int DELETE_BUTTON = 0;
    private final int BLACK_LIST_BUTTON = 1;

    private FloatBlock mFloatBlock;

    protected boolean isDeletable = true;
    private Drawable mLoader0;
    private AnimationDrawable mLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(getLayout(), null);
        mContainer = (RelativeLayout) view.findViewById(R.id.feedContainer);
        initNavigationBar(view);

        mLockView = (LockerView) view.findViewById(R.id.llvFeedLoading);
        mLockView.setVisibility(View.GONE);

        init();


        initBackground(view);
        initFilter(view);
        initListView(view);

        readItemReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int itemId = intent.getIntExtra(ChatActivity.INTENT_ITEM_ID, -1);
                if (itemId != -1) {
                    makeItemReadWithId(itemId);
                } else {
                    String lastMethod = intent.getStringExtra(CountersManager.METHOD_INTENT_STRING);
                    if (lastMethod != null) {
                        updateDataAfterReceivingCounters(lastMethod);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(ChatActivity.MAKE_ITEM_READ);
        filter.addAction(CountersManager.UPDATE_COUNTERS);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(readItemReceiver, filter);

        initFloatBlock((ViewGroup) view);
        createUpdateErrorMessage();

        GCMUtils.cancelNotification(getActivity(), getTypeForGCM());
        return view;
    }

    protected void initFloatBlock(ViewGroup view) {
        mFloatBlock = new FloatBlock(getActivity(), this, view);
    }

    protected void initNavigationBar(View view) {
        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) view.findViewById(R.id.loNavigationBar));
        Activity activity = getActivity();
        if (activity instanceof View.OnClickListener) {
            view.findViewById(R.id.btnNavigationHome).setOnClickListener((View.OnClickListener) activity);
        }
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getTitle());
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mFloatBlock != null) {
            mFloatBlock.onResume();
        }
        if (getListAdapter().isNeedUpdate()) {
            updateData(false, true);
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
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(readItemReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(readItemReceiver);
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
        mListView.setOnScrollListener(getListAdapter());

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
        };
    }

    protected AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                if (isDeletable) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.general_spinner_title).setItems(
                            getLongTapActions(),
                            getLongTapActionsListener((int) id)
                    );
                    builder.create().show();
                }
                return false;
            }

        };
    }

    protected DialogInterface.OnClickListener getLongTapActionsListener(final int id) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DELETE_BUTTON:
                        mLockView.setVisibility(View.VISIBLE);
                        onDeleteItem(id);
                        break;
                    case BLACK_LIST_BUTTON:
                        onAddToBlackList(id);
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

    private void onAddToBlackList(final int position) {
        new BlackListAddRequest(getItem(position).user.id, getActivity())
                .callback(new VipApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getListAdapter() != null) {
                                    getListAdapter().removeItem(position);
                                }
                            }
                        });
                    }
                }).exec();
    }

    protected void onDeleteItem(final int position) {
        DeleteRequest dr = new DeleteRequest(getActivity());
        dr.id = getItem(position).id;
        registerRequest(dr);
        dr.callback(new SimpleApiHandler() {
            @Override
            public void success(ApiResponse response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLockView.setVisibility(View.GONE);
                        getListAdapter().removeItem(position);
                    }
                });
            }

            @Override
            public void always(ApiResponse response) {
                super.always(response);
                mLockView.setVisibility(View.GONE);
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
        //Open mailchat activity

        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, item.user.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, item.user.first_name);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, item.user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, item.user.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, item.user.city.name);
        intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
        intent.putExtra(ChatFragment.INTENT_ITEM_ID, item.id);
        getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);

//        Intent intent = new Intent(getActivity(), ChatActivity.class);
//        intent.putExtra(ChatActivity.INTENT_USER_ID, item.user.id);
//        intent.putExtra(ChatActivity.INTENT_USER_NAME, item.user.first_name);
//        intent.putExtra(ChatActivity.INTENT_USER_SEX, item.user.sex);
//        intent.putExtra(ChatActivity.INTENT_USER_AGE, item.user.age);
//        intent.putExtra(ChatActivity.INTENT_USER_CITY, item.user.city.name);
//        intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
//        intent.putExtra(ChatActivity.INTENT_ITEM_ID, item.id);
//        getActivity().startActivityForResult(intent, ChatActivity.INTENT_CHAT_REQUEST);
    }

    public void onAvatarClick(T item, View view) {
        // Open profile activity
        if (item.unread) {
            item.unread = false;
            decrementCounters();
            getListAdapter().notifyDataSetChanged();
        }
        FragmentActivity activity = getActivity();
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).onExtraFragment(
                    ProfileFragment.newInstance(item.user.id, ProfileFragment.TYPE_USER_PROFILE));
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
        request.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                final FeedListData<T> dialogList = getFeedList(response.jsonResult);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isHistoryLoad) {
                            getListAdapter().addData(dialogList);
                        } else if (isPushUpdating) {
                            if (makeItemsRead) {
                                makeAllItemsRead();
                            }
                            mListAdapter.addDataFirst(dialogList);
                        } else {
                            getListAdapter().setData(dialogList);
                        }
                        onUpdateSuccess(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                        if (mNavBarController != null) mNavBarController.refreshNotificators();
                    }
                });
            }

            @Override
            public void fail(final int codeError, ApiResponse response) {

                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Activity activity = getActivity();
                        if (activity != null) {
                            if (isHistoryLoad) {
                                getListAdapter().showRetryItem();
                            }
                            if (updateErrorMessage != null) {
                                switch (codeError) {
                                    case ApiResponse.PREMIUM_ACCESS_ONLY:
                                        updateErrorMessage.showOnlyMessage(false);
                                        updateErrorMessage.addBlueButton(getString(R.string.buying_vip_status), new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(getActivity().getApplicationContext(), ContainerActivity.class);
                                                startActivityForResult(intent, ContainerActivity.INTENT_BUY_VIP_FRAGMENT);
                                            }
                                        });
                                        if (FeedFragment.this instanceof VisitorsFragment) {
                                            updateErrorMessage.setErrorMsg(getString(R.string.buying_vip_info));
                                        } else {
                                            updateErrorMessage.setErrorMsg(getString(R.string.general_premium_access_error));
                                        }
                                        break;
                                    default:
                                        updateErrorMessage.showOnlyMessage(false);
                                        updateErrorMessage.setErrorMsg(getString(R.string.general_data_error));
                                        break;
                                }
                            }
                            Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                            onUpdateFail(isPushUpdating || isHistoryLoad);
                            mListView.onRefreshComplete();
                            mListView.setVisibility(View.VISIBLE);
                            mIsUpdating = false;
                        }
                    }
                });
            }
        }).exec();
    }

    protected FeedAdapter<T> getListAdapter() {
        return mListAdapter;
    }

    protected void setIsDeletable(boolean value) {
        isDeletable = value;
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
        new FilterBlock((ViewGroup) view, R.id.loControlsGroup, R.id.btnNavigationSettingsBar, R.id.loToolsBar);
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
            updateErrorMessage.setVisibility(View.GONE);
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
            showUpdateErrorMessage();
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

    private void createUpdateErrorMessage() {
        if (updateErrorMessage == null) {
            updateErrorMessage = new RetryView(getActivity().getApplicationContext());
            updateErrorMessage.setErrorMsg(getString(R.string.general_data_error));
            updateErrorMessage.addButton(RetryView.REFRESH_TEMPLATE + getString(R.string.general_dialog_retry), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryButtonClick();
                }
            });
            mContainer.addView(updateErrorMessage);
            updateErrorMessage.setVisibility(View.GONE);
        }
    }

    private void showUpdateErrorMessage() {
        if (updateErrorMessage != null) {
            updateErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    private void retryButtonClick() {
        if (updateErrorMessage != null) {
            updateErrorMessage.setVisibility(View.GONE);
            updateData(false, true);
        }
    }

    private void makeItemReadWithId(int id) {
        for (FeedItem item : getListAdapter().getData()) {
            if (item.id == id && item.unread) {
                item.unread = false;
                getListAdapter().notifyDataSetChanged();
                decrementCounters();
            }
        }
    }

    private void updateDataAfterReceivingCounters(String lastMethod) {
        if (!lastMethod.equals(CountersManager.NULL_METHOD) && !lastMethod.equals(getRequest().getServiceName())) {
            if (CountersManager.getInstance(getActivity()).getCounter(getTypeForCounters()) > 0) {
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
