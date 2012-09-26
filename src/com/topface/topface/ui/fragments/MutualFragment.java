package com.topface.topface.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.Static;
import com.topface.topface.data.FeedSympathy;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedSympathyRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.adapters.MutualListAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.SwapAnimation;

import java.util.LinkedList;

public class MutualFragment extends BaseFragment {

    private PullToRefreshListView mListView;
    private MutualListAdapter mListAdapter;
    private AvatarManager<FeedSympathy> mAvatarManager;
    private DoubleBigButton mDoubleButton;
    private TextView mBackgroundText;
    private ImageView mBannerView;
    
    private View mToolsBar;
    private View mShowToolsBarButton;
    private View mControlsGroup;
 
    private boolean mNewUpdating;
    private boolean mIsUpdating;
    private static final int LIMIT = 40;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.ac_mutual, null);

        // Data
        Data.mutualList = new LinkedList<FeedSympathy>();
        
        // Navigation Header
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity)getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getResources().getString(R.string.dashbrd_btn_sympathy));
        mControlsGroup = view.findViewById(R.id.loControlsGroup);
        mToolsBar = view.findViewById(R.id.loToolsBar);
        mShowToolsBarButton = view.findViewById(R.id.btnNavigationFilterBar);
        mShowToolsBarButton.setVisibility(View.VISIBLE);
        mShowToolsBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mControlsGroup.startAnimation(new SwapAnimation(mControlsGroup, R.id.loToolsBar));
            }
        });
        
        ViewTreeObserver vto = mToolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int y = mToolsBar.getMeasuredHeight();
                if(y != 0) {
                    y += Static.HEADER_SHADOW_SHIFT;
                    mControlsGroup.setPadding(mControlsGroup.getPaddingLeft(), -y, mControlsGroup.getPaddingRight(), mControlsGroup.getPaddingBottom());
                    ViewTreeObserver obs = mControlsGroup.getViewTreeObserver();
                    obs.removeGlobalOnLayoutListener(this);                    
                }
            }
        });

        // ListView background
     	mBackgroundText = (TextView)view.findViewById(R.id.tvBackgroundText);
        
        // Banner
        mBannerView = (ImageView)view.findViewById(R.id.ivBanner);
        
        // Double Button
        mDoubleButton = (DoubleBigButton)view.findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.symphaty_btn_dbl_left));
        mDoubleButton.setRightText(getString(R.string.symphaty_btn_dbl_right));
        mDoubleButton.setChecked(DoubleBigButton.LEFT_BUTTON);
        mDoubleButton.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUpdating = false;
                updateData(false);
            }
        });
        mDoubleButton.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNewUpdating = true;
                updateData(false);
            }
        });

        // ListView
        mListView = (PullToRefreshListView)view.findViewById(R.id.lvSymphatyList);
        mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int position,long id) {
                FeedSympathy theFeedMutual = (FeedSympathy)parent.getItemAtPosition(position);
            	if (!mIsUpdating && theFeedMutual.isLoaderRetry()) {
            		updateUI(new Runnable() {
						public void run() {
							removeLoaderListItem();
							Data.mutualList.add(new FeedSympathy(ItemType.LOADER));
							mListAdapter.notifyDataSetChanged();
						}
					});
            		
            		updateDataHistory();
            	} else {
	                try {
	                    Intent intent = new Intent(getActivity(), UserProfileActivity.class);
	                    intent.putExtra(ProfileActivity.INTENT_USER_ID,   theFeedMutual.uid);
	                    intent.putExtra(ProfileActivity.INTENT_USER_NAME, theFeedMutual.first_name);
	                    startActivityForResult(intent, 0);
	                } catch(Exception e) {
	                    Debug.log(MutualFragment.this, "start ProfileActivity exception:" + e.toString());
	                }
            	}
            }
        });

        mListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                updateData(true);
            }
        });

        // Control creation
        mAvatarManager = new AvatarManager<FeedSympathy>(getActivity(), Data.mutualList, new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        	    if (Data.mutualList.size() > 0)
            		if (Data.mutualList.getLast().isLoader() && !mIsUpdating)
            			updateDataHistory();
        		
        		super.handleMessage(msg);
        	}
        });
        mListAdapter = new MutualListAdapter(getActivity(), mAvatarManager);
        mListView.setOnScrollListener(mAvatarManager);
        mListView.getRefreshableView().setAdapter(mListAdapter);
        
        mNewUpdating = CacheProfile.unread_mutual > 0;
        
        updateData(false);

        return view;
    }

    private void updateData(final boolean isPushUpdating) {
    	mIsUpdating = true;
        if (!isPushUpdating)
            onUpdateStart(isPushUpdating);

        mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);

        FeedSympathyRequest symphatyRequest = new FeedSympathyRequest(getActivity());
        registerRequest(symphatyRequest);
        symphatyRequest.limit = LIMIT;
        symphatyRequest.only_new = mNewUpdating;
        symphatyRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<FeedSympathy> feedSymphatyList = FeedSympathy.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Data.mutualList.clear();
                        Data.mutualList.addAll(feedSymphatyList);
                        CacheProfile.unread_mutual = 0;
                    	if (mNewUpdating) {
                     		if (FeedSympathy.unread_count > 0) {
                     			Data.mutualList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
                     		}
                     	} else {
                     		if (!(Data.mutualList.size() == 0 || Data.mutualList.size() < LIMIT / 2)) {
                     			Data.mutualList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
                     		}
                     	}

                        onUpdateSuccess(isPushUpdating);
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                    	Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                    	onUpdateFail(isPushUpdating);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
        }).exec();
    }

    private void updateDataHistory() {
    	mIsUpdating = true;
    	mNewUpdating = mDoubleButton.isRightButtonChecked();

        FeedSympathyRequest symphatyRequest = new FeedSympathyRequest(getActivity());
        registerRequest(symphatyRequest);
        symphatyRequest.limit = LIMIT;
        symphatyRequest.only_new = mNewUpdating;
        if (!mNewUpdating) {
        	if (Data.mutualList.getLast().isLoader() || Data.mutualList.getLast().isLoaderRetry()) {
        		symphatyRequest.from = Data.mutualList.get(Data.mutualList.size() - 2).id;
        	} else {
        		symphatyRequest.from = Data.mutualList.get(Data.mutualList.size() - 1).id;
        	}        	
        }
        symphatyRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<FeedSympathy> feedSymphatyList = FeedSympathy.parse(response);
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                    	removeLoaderListItem();
                    	
                    	if (feedSymphatyList.size() > 0) {
                            Data.mutualList.addAll(feedSymphatyList);
                            if (mNewUpdating) {
                        		if (FeedSympathy.unread_count > 0)
                        			Data.mutualList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
                        	} else {
                        		if (!(Data.mutualList.size() == 0 || Data.mutualList.size() < (LIMIT/2)))
                        			Data.mutualList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
                        	}
                        }
                    	
                        onUpdateSuccess(true);
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                        mIsUpdating = false;                    
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                    	onUpdateFail(true);
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mIsUpdating = false;
                    	removeLoaderListItem();
                        Data.mutualList.add(new FeedSympathy(IListLoader.ItemType.RETRY));
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).exec();
    }

    private void removeLoaderListItem() {
    	if (Data.mutualList.size() > 0 ) {
	    	if (Data.mutualList.getLast().isLoader() || Data.mutualList.getLast().isLoaderRetry()) {
	    		Data.mutualList.remove(Data.mutualList.size() - 1);
	    	}
    	}
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        mListView = null;

        if (mListAdapter != null)
            mListAdapter.release();
        mListAdapter = null;

        if (mAvatarManager != null) {
            mAvatarManager.release();
            mAvatarManager = null;
        }
    }

    protected void onUpdateStart(boolean isFlyUpdating) {
    	if (!isFlyUpdating) {
			mListView.setVisibility(View.INVISIBLE);
			mBackgroundText.setText(R.string.general_dialog_loading);
			mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(Recycle.s_Loader,
					mBackgroundText.getCompoundDrawables()[1],
					mBackgroundText.getCompoundDrawables()[2],
					mBackgroundText.getCompoundDrawables()[3]);
			((AnimationDrawable)mBackgroundText.getCompoundDrawables()[0]).start();
			mDoubleButton.setClickable(false);
    	}
	}

	@Override
	protected void onUpdateSuccess(boolean isFlyUpdating) {
		if (!isFlyUpdating) {
			mListView.setVisibility(View.VISIBLE);
			if (Data.mutualList.isEmpty()) {
				mBackgroundText.setText(R.string.symphaty_background_text);
			} else {
				mBackgroundText.setText("");
			}		
			
			if (mBackgroundText.getCompoundDrawables()[0] != null) {
				((AnimationDrawable)mBackgroundText.getCompoundDrawables()[0]).stop();
			}
			
			mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(null, 
					mBackgroundText.getCompoundDrawables()[1],
					mBackgroundText.getCompoundDrawables()[2],
					mBackgroundText.getCompoundDrawables()[3]);
			mDoubleButton.setClickable(true);
		}
	}

	@Override
	protected void onUpdateFail(boolean isFlyUpdating) {
		if (!isFlyUpdating) {
			mListView.setVisibility(View.VISIBLE);
			mBackgroundText.setText("");		
			
			if (mBackgroundText.getCompoundDrawables()[0] != null) {
				((AnimationDrawable)mBackgroundText.getCompoundDrawables()[0]).stop();
			}
			
			mBackgroundText.setCompoundDrawablesWithIntrinsicBounds(null, 
					mBackgroundText.getCompoundDrawables()[1],
					mBackgroundText.getCompoundDrawables()[2],
					mBackgroundText.getCompoundDrawables()[3]);
			mDoubleButton.setClickable(true);
		}
	}
}
