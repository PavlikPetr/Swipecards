package com.topface.topface.ui.fragments.feed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DeleteRequest;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.blocks.FilterBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.ProfileNewFragment;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.NavigationBarController;
import com.topface.topface.utils.Utils;
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

    private BroadcastReceiver readItemReceiver;

    protected String[] editButtonsNames;

    private final int DELETE_BUTTON = 0;

    private FloatBlock mFloatBlock;

    protected boolean isDeletable = true;
    private Drawable mLoader0;
    private AnimationDrawable mLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(getLayout(), null);
        mContainer = (RelativeLayout) view.findViewById(R.id.feedContainer);
        editButtonsNames = new String[]{getString(R.string.general_delete_title)};
        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) view.findViewById(R.id.loNavigationBar));
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getTitle());

        mLockView = (LockerView) view.findViewById(R.id.llvFeedLoading);
        mLockView.setVisibility(View.GONE);

        init();


        initBackground(view);
        initFilter(view);
        initListView(view);
        if (mListAdapter.isNeedUpdate()) {
            updateData(false, true);
        }

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

        mFloatBlock = new FloatBlock(getActivity(), this, (ViewGroup) view);
        createUpdateErrorMessage();

        GCMUtils.cancelNotification(getActivity(), getTypeForGCM());
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        mFloatBlock.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mFloatBlock.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

        mListAdapter = getAdapter();
        mListAdapter.setOnAvatarClickListener(this);
        mListView.setOnScrollListener(mListAdapter);

        ImageView iv = new ImageView(getActivity().getApplicationContext());
        iv.setBackgroundResource(R.drawable.im_header_item_list_bg);
        iv.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mListView.getRefreshableView().addHeaderView(iv);

        mListView.getRefreshableView().setAdapter(mListAdapter);
        mListView.getRefreshableView().setOnItemClickListener(getOnItemClickListener());
        mListView.getRefreshableView().setOnTouchListener(getListViewOnTouchListener());
        mListView.getRefreshableView().setOnItemLongClickListener(getOnItemLongClickListener());

        mListView.scrollBy(0, 2);
    }

    abstract protected FeedAdapter<T> getAdapter();

    protected FeedAdapter.Updater getUpdaterCallback() {
        return new FeedAdapter.Updater() {
            @Override
            public void onFeedUpdate() {
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
                if (!mIsUpdating && item.isLoaderRetry()) {
                    updateUI(new Runnable() {
                        public void run() {
                            mListAdapter.showLoaderItem();
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
                    builder.setTitle(R.string.general_spinner_title).setItems(editButtonsNames, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DELETE_BUTTON:
                                    mLockView.setVisibility(View.VISIBLE);
                                    deleteItem((int) id);
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
                return false;
            }

        };
    }

    protected void deleteItem(final int position) {
        DeleteRequest dr = new DeleteRequest(getActivity());
        dr.id = mListAdapter.getItem(position).id;
        registerRequest(dr);
        dr.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) throws NullPointerException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLockView.setVisibility(View.GONE);
                        FeedList<T> mFeedList = mListAdapter.getData();
                        mFeedList.remove(position);
                        mListAdapter.setData(mFeedList);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                Debug.log(response.toString());
                if (codeError != ApiResponse.PREMIUM_ACCESS_ONLY) {
                    Utils.showErrorMessage(getActivity());
                }
                mLockView.setVisibility(View.GONE);
            }
        }).exec();

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
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID, item.user.id);
        intent.putExtra(ChatActivity.INTENT_USER_NAME, item.user.first_name);
        intent.putExtra(ChatActivity.INTENT_USER_SEX, item.user.sex);
        intent.putExtra(ChatActivity.INTENT_USER_AGE, item.user.age);
        intent.putExtra(ChatActivity.INTENT_USER_CITY, item.user.city.name);
        intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
        intent.putExtra(ChatActivity.INTENT_ITEM_ID, item.id);
        startActivity(intent);
    }

    public void onAvatarClick(T item, View view) {
        // Open profile activity
        if (item.unread) {
            item.unread = false;
            decrementCounters();
            mListAdapter.notifyDataSetChanged();
        }
        //TODO: switch to user fragment
        ((NavigationActivity)getActivity()).onExtraFragment(
                ProfileNewFragment.newInstance(item.user.id,ProfileNewFragment.TYPE_USER_PROFILE));

//        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
//        intent.putExtra(UserProfileActivity.INTENT_USER_ID, item.user.id);
//        intent.putExtra(UserProfileActivity.INTENT_USER_NAME, item.user.first_name);
//        intent.putExtra(UserProfileActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
//        startActivity(intent);
    }

    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad, final boolean makeItemsRead) {
        mIsUpdating = true;
        onUpdateStart(isPushUpdating || isHistoryLoad);

        FeedRequest request = getRequest();
        registerRequest(request);

        FeedItem lastItem = mListAdapter.getLastFeedItem();
        FeedItem firstItem = mListAdapter.getFirstItem();

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
                            mListAdapter.addData(dialogList);
                        } else if (isPushUpdating) {
                            if (makeItemsRead) {
                                makeAllItemsRead();
                            }
                            mListAdapter.addDataFirst(dialogList);
                        } else {
                            mListAdapter.setData(dialogList);
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
                                mListAdapter.showRetryItem();
                            }
                            if (codeError == ApiResponse.PREMIUM_ACCESS_ONLY) {
                                updateErrorMessage.showOnlyMessage(true);
                                updateErrorMessage.setErrorMsg(getString(R.string.general_premium_access_error));
                            } else {
                                updateErrorMessage.showOnlyMessage(false);
                                updateErrorMessage.setErrorMsg(getString(R.string.general_data_error));
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

    protected void setIsDeletable(boolean value) {
        isDeletable = value;
    }

    protected boolean isShowUnreadItems() {
        return mDoubleButton.isRightButtonChecked();
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

            if (mListAdapter.isEmpty()) {
                mBackgroundText.setText(getEmptyFeedText());
            } else {
                mBackgroundText.setVisibility(View.INVISIBLE);
            }

            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
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
        for (FeedItem item : mListAdapter.getData()) {
            item.unread = false;
        }
    }

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mBackgroundText.setText("");
            showUpdateErrorMessage();
            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
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
            ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).start();
            setFilterSwitcherState(false);
        }
    }

    protected void setFilterSwitcherState(boolean clickable) {
        mDoubleButton.setClickable(clickable);
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
        updateErrorMessage.setVisibility(View.VISIBLE);
    }

    private void retryButtonClick() {
        updateErrorMessage.setVisibility(View.GONE);
        updateData(false, true);
    }

    private void makeItemReadWithId(int id) {
        for (FeedItem item : mListAdapter.getData()) {
            if (item.id == id && item.unread) {
                item.unread = false;
                mListAdapter.notifyDataSetChanged();
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
            mLoader = (AnimationDrawable) getResources().getDrawable(R.drawable.loader);
        }
        return mLoader;
    }

}
