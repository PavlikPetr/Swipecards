package com.topface.topface.ui.fragments.feed;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.R;
import com.topface.topface.Recycle;
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
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.ui.views.RetryView;
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
    private LockerView lockView;

    protected String[] editButtonsNames;

    private final int DELETE_BUTTON = 0;

    private FloatBlock mFloatBlock;

    protected boolean isDeletable = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(getLayout(), null);
        mContainer = (RelativeLayout) view.findViewById(R.id.feedContainer);
        editButtonsNames = new String[]{getString(R.string.default_delete_title)};
        // Navigation bar
        mNavBarController = new NavigationBarController((ViewGroup) view.findViewById(R.id.loNavigationBar));
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getTitle());

        lockView = (LockerView)view.findViewById(R.id.llvFeedLoading);
        lockView.setVisibility(View.GONE);

        init();

        initBackground(view);
        initFilter(view);
        initListView(view);
        if (mListAdapter.isNeedUpdate()) {
            updateData(false);
        }

        mFloatBlock = new FloatBlock(getActivity(), this, (ViewGroup) view);

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

    protected int getLayout() {
        return R.layout.ac_feed;
    }

    private void initListView(View view) {
        // ListView    	

        mListView = (PullToRefreshListView) view.findViewById(R.id.lvFeedList);
        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                updateData(true);
            }
        });

        mListAdapter = getAdapter();
        mListAdapter.setOnAvatarClickListener(this);
        mListView.setOnScrollListener(mListAdapter);
        mListView.getRefreshableView().setAdapter(mListAdapter);

        mListView.getRefreshableView().setOnItemClickListener(getOnItemClickListener());
        mListView.getRefreshableView().setOnTouchListener(getListViewOnTouchListener());
        mListView.getRefreshableView().setOnItemLongClickListener(getOnItemLongClickListener());
    }

    abstract protected FeedAdapter<T> getAdapter();

    protected FeedAdapter.Updater getUpdaterCallback() {
        return new FeedAdapter.Updater() {
            @Override
            public void onFeedUpdate() {
                if (!mIsUpdating) {
                    updateData(false, true);
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
                    updateData(false, true);
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
                if(isDeletable){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.default_spinner_title).setItems(editButtonsNames,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DELETE_BUTTON:
                                   lockView.setVisibility(View.VISIBLE);
                                   deleteItem((int)id);
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

    private void deleteItem(final int position) {
        DeleteRequest dr = new DeleteRequest(getActivity());
        dr.id = mListAdapter.getItem(position).id;
        registerRequest(dr);
        dr.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) throws NullPointerException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        lockView.setVisibility(View.GONE);
                        FeedList<T> mFeedList = mListAdapter.getData();
                        mFeedList.remove(position);
                        mListAdapter.setData(mFeedList);
                    }
                });
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                Debug.log(response.toString());
                lockView.setVisibility(View.GONE);
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
        //Open chat activity
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID, item.user.id);
        intent.putExtra(ChatActivity.INTENT_USER_NAME, item.user.first_name);
        intent.putExtra(ChatActivity.INTENT_USER_SEX, item.user.sex);
        intent.putExtra(ChatActivity.INTENT_USER_AGE, item.user.age);
        intent.putExtra(ChatActivity.INTENT_USER_CITY, item.user.city.name);
        intent.putExtra(ChatActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
        startActivity(intent);
    }

    public void onAvatarClick(T item, View view) {
        // Open profile activity
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.INTENT_USER_ID, item.user.id);
        intent.putExtra(UserProfileActivity.INTENT_USER_NAME, item.user.first_name);
        intent.putExtra(UserProfileActivity.INTENT_PREV_ENTITY, this.getClass().getSimpleName());
        startActivity(intent);
    }

    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad) {
        mIsUpdating = true;
        onUpdateStart(isPushUpdating || isHistoryLoad);

        FeedRequest request = getRequest();
        registerRequest(request);

        FeedItem lastItem = mListAdapter.getLastFeedItem();
        FeedItem firstItem = mListAdapter.getFirstItem();

        if (isHistoryLoad && lastItem != null) {
            request.to = lastItem.id;
        }
        if (isPushUpdating) {
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
                        } else if(isPushUpdating){
                            mListAdapter.addDataFirst(dialogList);
                        } else{
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
            public void fail(int codeError, ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isHistoryLoad) {
                            mListAdapter.showRetryItem();
                        }
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        onUpdateFail(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
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

    protected void updateData(boolean isPushUpdating) {
        updateData(isPushUpdating, false);
    }

    @SuppressWarnings("deprecation")
    protected void initFilter(View view) {
        new FilterBlock((ViewGroup) view, R.id.loControlsGroup, R.id.btnNavigationSettingsBar, R.id.loToolsBar);
        initDoubleButton(view);
    }

    protected void initDoubleButton(View view) {
        // Double Button
        mDoubleButton = (DoubleBigButton) view.findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.btn_dbl_left));
        mDoubleButton.setRightText(getString(R.string.btn_dbl_right));
        mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mDoubleButton.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(false);
            }
        });
        mDoubleButton.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData(false);
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
                mBackgroundText.setText("");
            }

            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader_0,
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
            setFilterSwitcherState(true);
        }
    }

    abstract protected int getEmptyFeedText();

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mBackgroundText.setText("");
            createUpdateErrorMessage();
            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader_0,
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
            mBackgroundText.setText(R.string.general_dialog_loading);
            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader,
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

    private void createUpdateErrorMessage() {
        if (updateErrorMessage == null) {
            updateErrorMessage = new RetryView(getActivity().getApplicationContext());
            updateErrorMessage.init(getActivity().getLayoutInflater());
            updateErrorMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryButtonClick();
                }
            });
            mContainer.addView(updateErrorMessage);
        } else {
            updateErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    private void retryButtonClick() {
        updateErrorMessage.setVisibility(View.GONE);
        updateData(false);
    }

}
