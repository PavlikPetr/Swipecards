<<<<<<< HEAD
package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.SwapAnimation;
import org.json.JSONObject;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment implements FeedAdapter.OnAvatarClickListener<T> {
    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(getLayout(), null);

        // Home Button
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        // Set title
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getTitle());

        init();
        
        initBackground(view);
        initFilter(view);
        initListView(view);        
        if (mListAdapter.isNeedUpdate()) {
            updateData(false);
        }
        
        return view;
    }

    protected void init(){
    	
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

    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
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
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {				
				return false;
			}
    		
		};
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
        intent.putExtra(UserProfileActivity.INTENT_MUTUAL_ID, item.id);
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
        if (isHistoryLoad && lastItem != null) {
            request.before = lastItem.id;
        }
        request.limit = FeedAdapter.LIMIT;
        request.unread = mDoubleButton.isRightButtonChecked();
        request.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                final FeedListData<T> dialogList = getFeedList(response.jsonResult);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isHistoryLoad) {
                            mListAdapter.addData(dialogList.items);
                        } else {
                            mListAdapter.setData(dialogList.items);
                        }
                        onUpdateSuccess(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
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

    protected abstract FeedListData<T> getFeedList(JSONObject response);

    private FeedRequest getRequest() {
        return new FeedRequest(getFeedService(), getActivity());
    }

    protected abstract FeedRequest.FeedService getFeedService();

    protected void updateData(boolean isPushUpdating) {
        updateData(isPushUpdating, false);
    }

    @SuppressWarnings("deprecation")
    private void initFilter(View view) {
        final View controlGroup = view.findViewById(R.id.loControlsGroup);
        View showToolsBarButton = view.findViewById(R.id.btnNavigationSettingsBar);
        showToolsBarButton.setVisibility(View.VISIBLE);
        showToolsBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlGroup.startAnimation(new SwapAnimation(controlGroup, R.id.loToolsBar));
            }
        });

        final View toolsBar = view.findViewById(R.id.loToolsBar);
        ViewTreeObserver vto = toolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {            
            @Override
            public void onGlobalLayout() {
                int y = toolsBar.getMeasuredHeight();
                if (y != 0) {
                    y += Static.HEADER_SHADOW_SHIFT;
                    controlGroup.setPadding(controlGroup.getPaddingLeft(), -y, controlGroup.getPaddingRight(), controlGroup.getPaddingBottom());
                    ViewTreeObserver obs = controlGroup.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

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
            mDoubleButton.setClickable(true);
        }
    }

    abstract protected int getEmptyFeedText();

    @Override
    protected void onUpdateFail(boolean isPushUpdating) {
        if (!isPushUpdating) {
            mListView.setVisibility(View.VISIBLE);
            mBackgroundText.setText("");

            if (mBackgroundText.getCompoundDrawables()[0] != null) {
                ((AnimationDrawable) mBackgroundText.getCompoundDrawables()[0]).stop();
            }

            mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader_0,
                    mBackgroundText.getCompoundDrawables()[1],
                    mBackgroundText.getCompoundDrawables()[2],
                    mBackgroundText.getCompoundDrawables()[3]);
            mDoubleButton.setClickable(true);
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
            mDoubleButton.setClickable(false);
        }
    }

}
=======
package com.topface.topface.ui.fragments.feed;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.FeedAdapter;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.RetryView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.SwapAnimation;
import org.json.JSONObject;

public abstract class FeedFragment<T extends FeedItem> extends BaseFragment implements FeedAdapter.OnAvatarClickListener<T> {
    protected PullToRefreshListView mListView;
    protected FeedAdapter<T> mListAdapter;
    private TextView mBackgroundText;
    protected DoubleBigButton mDoubleButton;
    protected boolean mIsUpdating;
    private RetryView updateErrorMessage;
    private RelativeLayout mContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(getLayout(), null);
        mContainer = (RelativeLayout)view.findViewById(R.id.feedContainer);
        // Home Button
        view.findViewById(R.id.btnNavigationHome).setOnClickListener((NavigationActivity) getActivity());
        // Set title
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getTitle());

        init();
        
        initBackground(view);
        initFilter(view);
        initListView(view);        
        if (mListAdapter.isNeedUpdate()) {
            updateData(false);
        }
        
        return view;
    }

    protected void init(){
    	
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

    protected AdapterView.OnItemClickListener getOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
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
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {				
				return false;
			}
    		
		};
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
        startActivity(intent);
    }

    public void onAvatarClick(T item, View view) {
        // Open profile activity
        Intent intent = new Intent(getActivity(), UserProfileActivity.class);
        intent.putExtra(UserProfileActivity.INTENT_MUTUAL_ID, item.id);
        intent.putExtra(UserProfileActivity.INTENT_USER_ID, item.user.id);
        intent.putExtra(UserProfileActivity.INTENT_USER_NAME, item.user.first_name);
        startActivity(intent);
    }

    protected void updateData(final boolean isPushUpdating, final boolean isHistoryLoad) {
        mIsUpdating = true;
        onUpdateStart(isPushUpdating || isHistoryLoad);

        FeedRequest request = getRequest();
        registerRequest(request);

        FeedItem lastItem = mListAdapter.getLastFeedItem();
        if (isHistoryLoad && lastItem != null) {
            request.before = lastItem.id;
        }
        request.limit = FeedAdapter.LIMIT;
        request.unread = mDoubleButton.isRightButtonChecked();
        request.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                final FeedListData<T> dialogList = getFeedList(response.jsonResult);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        if (isHistoryLoad) {
                            mListAdapter.addData(dialogList.items);
                        } else {
                            mListAdapter.setData(dialogList.items);
                        }
                        onUpdateSuccess(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
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
                        onUpdateFail(isPushUpdating || isHistoryLoad);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
        }).exec();
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
    private void initFilter(View view) {
        final View controlGroup = view.findViewById(R.id.loControlsGroup);
        View showToolsBarButton = view.findViewById(R.id.btnNavigationSettingsBar);
        showToolsBarButton.setVisibility(View.VISIBLE);
        showToolsBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlGroup.startAnimation(new SwapAnimation(controlGroup, R.id.loToolsBar));
            }
        });

        final View toolsBar = view.findViewById(R.id.loToolsBar);
        ViewTreeObserver vto = toolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {            
            @Override
            public void onGlobalLayout() {
                int y = toolsBar.getMeasuredHeight();
                if (y != 0) {
                    y += Static.HEADER_SHADOW_SHIFT;
                    controlGroup.setPadding(controlGroup.getPaddingLeft(), -y, controlGroup.getPaddingRight(), controlGroup.getPaddingBottom());
                    ViewTreeObserver obs = controlGroup.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);
                }
            }
        });

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
            mDoubleButton.setClickable(true);
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
            mDoubleButton.setClickable(true);
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
            mDoubleButton.setClickable(false);
        }
    }

    private void createUpdateErrorMessage() {
        if(updateErrorMessage == null){
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
>>>>>>> 93a222b6f3781cd2e5d8ebd976408b1354d84ab6
