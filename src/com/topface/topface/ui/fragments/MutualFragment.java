package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Recycle;
import com.topface.topface.data.FeedSympathy;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedSympathyRequest;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.MutualListAdapter;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MutualFragment extends BaseFragment {

    private boolean mNewUpdating;
//    private TextView mFooterView;
    private PullToRefreshListView mListView;
    private MutualListAdapter mListAdapter;
    private AvatarManager<FeedSympathy> mAvatarManager;
    private DoubleBigButton mDoubleButton;
    private LockerView mLoadingLocker;
    private TextView mBackgroundText;
    private ImageView mBannerView;
    
    private View mToolsBar;
    private View mBuBa;
    private View mFuBa;
    
    private boolean mIsUpdating = false;
    // Constants
    private static final int LIMIT = 40;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View view = inflater.inflate(R.layout.ac_sympathy, null);

        mFuBa = view.findViewById(R.id.loFuBa);
        mToolsBar = view.findViewById(R.id.loToolsBar);
        mBuBa = view.findViewById(R.id.btnBuBa);
        mBuBa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mToolsBar.startAnimation(new ExpandAnimation(mToolsBar, 300));
            }
        });
        
        ViewTreeObserver vto = mToolsBar.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = mToolsBar.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);
                //mFuBa.setPadding(mFuBa.getPaddingLeft(), -mToolsBar.getHeight(), mFuBa.getPaddingRight(), mFuBa.getPaddingBottom());
            }
        });
        

        // Data
        Data.sympathyList = new LinkedList<FeedSympathy>();

        // Progress
        mLoadingLocker = (LockerView)view.findViewById(R.id.llvSympathyLoading);

        // ListView background
     	mBackgroundText = (TextView) view.findViewById(R.id.tvBackgroundText);
        
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
            	if (!mIsUpdating && Data.sympathyList.get(position).isLoaderRetry()) {
            		updateUI(new Runnable() {
						public void run() {
							removeLoaderListItem();
							Data.sympathyList.add(new FeedSympathy(ItemType.LOADER));
							mListAdapter.notifyDataSetChanged();
						}
					});
            		
            		updateDataHistory();
            	} else {
	                try {
	                    Intent intent = new Intent(getActivity(), UserProfileActivity.class);
	                    intent.putExtra(ProfileActivity.INTENT_USER_ID, Data.sympathyList.get(position).uid);
	                    intent.putExtra(ProfileActivity.INTENT_USER_NAME, Data.sympathyList.get(position).first_name);
	                    startActivityForResult(intent, 0);
	                } catch(Exception e) {
	                    Debug.log(MutualFragment.this, "start ProfileActivity exception:" + e.toString());
	                }
            	}
            }
        });
        mListView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData(true);
            }
        });

        // Footer
//        mFooterView = new TextView(getApplicationContext());
//        mFooterView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                updateDataHistory();
//            }
//        });
//        mFooterView.setBackgroundResource(R.drawable.item_list_selector);
//        mFooterView.setText(getString(R.string.general_footer_previous));
//        mFooterView.setTextColor(Color.DKGRAY);
//        mFooterView.setGravity(Gravity.CENTER);
//        mFooterView.setTypeface(Typeface.DEFAULT_BOLD);
//        mFooterView.setVisibility(View.GONE);
//        mListView.getRefreshableView().addFooterView(mFooterView);

        // Control creating
        mAvatarManager = new AvatarManager<FeedSympathy>(getActivity(), Data.sympathyList, new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		if (Data.sympathyList.getLast().isLoader() && !mIsUpdating)
        			updateDataHistory();
        		
        		super.handleMessage(msg);
        	}
        });
        mListAdapter = new MutualListAdapter(getActivity(), mAvatarManager);
        mListView.setOnScrollListener(mAvatarManager);
        mListView.setAdapter(mListAdapter);

        mNewUpdating = CacheProfile.unread_mutual > 0 ? true : false;
        CacheProfile.unread_mutual = 0;
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
                Data.sympathyList.clear();
                Data.sympathyList.addAll(FeedSympathy.parse(response));               
                
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                    	if (mNewUpdating) {
                     		if (FeedSympathy.unread_count > 0) {
                     			Data.sympathyList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
                     		}
                     	} else {
                     		if (!(Data.sympathyList.size() == 0 || Data.sympathyList.size() < LIMIT / 2)) {
                     			Data.sympathyList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
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
        	if (Data.sympathyList.getLast().isLoader() || Data.sympathyList.getLast().isLoaderRetry()) {
        		symphatyRequest.from = Data.sympathyList.get(Data.sympathyList.size() - 2).id;
        	} else {
        		symphatyRequest.from = Data.sympathyList.get(Data.sympathyList.size() - 1).id;
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
                            Data.sympathyList.addAll(feedSymphatyList);
                            if (mNewUpdating) {
                        		if (FeedSympathy.unread_count > 0)
                        			Data.sympathyList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
                        	} else {
                        		if (!(Data.sympathyList.size() == 0 || Data.sympathyList.size() < (LIMIT/2)))
                        			Data.sympathyList.add(new FeedSympathy(IListLoader.ItemType.LOADER));
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
                        Data.sympathyList.add(new FeedSympathy(IListLoader.ItemType.RETRY));
                        mListView.onRefreshComplete();
                        mListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).exec();
    }

    private void removeLoaderListItem() {
    	if (Data.sympathyList.size() > 0 ) {
	    	if (Data.sympathyList.getLast().isLoader() || Data.sympathyList.getLast().isLoaderRetry()) {
	    		Data.sympathyList.remove(Data.sympathyList.size() - 1);
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

    @Override
    public void clearLayout() {
        Debug.log(this, "SympathyActivity::clearLayout");
        mListView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void fillLayout() {
        Debug.log(this, "SympathyActivity::fillLayout");

        updateBanner(mBannerView, BannerRequest.SYMPATHY);
        updateData(false);
    }

    protected void onUpdateStart(boolean isFlyUpdating) {
//		mLoadingLocker.setVisibility(View.VISIBLE);
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
//		mLoadingLocker.setVisibility(View.GONE);
		if (!isFlyUpdating) {
			mListView.setVisibility(View.VISIBLE);
			if (Data.sympathyList.isEmpty()) {
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
//		mLoadingLocker.setVisibility(View.GONE);
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
	
	
	
	public class ExpandAnimation extends Animation {
	    private View mAnimatedView;
	    private LinearLayout.LayoutParams mViewLayoutParams;
	    private int mMarginStart, mMarginEnd;
	    private boolean mIsVisibleAfter = false;
	    private boolean mWasEndedAlready = false;
       
	    /**
	     * Initialize the animation
	     * @param view The layout we want to animate
	     * @param duration The duration of the animation, in ms
	     */
	    public ExpandAnimation(View view, int duration) {

	        setDuration(duration);
	        mAnimatedView = view;
	        mViewLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();

	        // decide to show or hide the view
	        mIsVisibleAfter = (view.getVisibility() == View.VISIBLE);
	        
	        //mToolsBar = view.findViewById(R.id.loToolsBar);
	        mMarginStart = mViewLayoutParams.bottomMargin;
	        mMarginEnd = (mMarginStart == 0 ? (0 - view.getHeight()) : 0);
//	        mMarginStart = mAnimatedView.getPaddingTop();
	        Debug.log("mMarginStart:"+mMarginStart);
//	        mMarginEnd = (mMarginStart == 0 ? (0 - mToolsBar.getHeight()) : 0);
	        Debug.log("mToolsBar.getHeight():"+view.getHeight());
	        Debug.log("mMarginEnd:"+mMarginEnd);

	        view.setVisibility(View.VISIBLE);
	    }

	    @Override
	    protected void applyTransformation(float interpolatedTime, Transformation t) {
	        super.applyTransformation(interpolatedTime, t);

	        if (interpolatedTime < 1.0f) {

	            int y = (int) ((mMarginEnd - mMarginStart) * interpolatedTime);
	            // Calculating the new bottom margin, and setting it
	            mViewLayoutParams.bottomMargin = mMarginStart + y;
	            
	            
	            Debug.log("y:"+y);
	            Debug.log("time:"+interpolatedTime);
	            
//	            mFuBa.setPadding(mFuBa.getPaddingLeft(), y, mFuBa.getPaddingRight(), mFuBa.getPaddingBottom());

	            // Invalidating the layout, making us seeing the changes we made
	            mAnimatedView.requestLayout();

	        // Making sure we didn't run the ending before (it happens!)
	        } else if (!mWasEndedAlready) {
	            mViewLayoutParams.bottomMargin = mMarginEnd;
	            //mFuBa.setPadding(mFuBa.getPaddingLeft(), mMarginEnd, mFuBa.getPaddingRight(), mFuBa.getPaddingBottom());
	            mAnimatedView.requestLayout();

	            if (mIsVisibleAfter) {
	                mAnimatedView.setVisibility(View.GONE);
	            }
	            mWasEndedAlready = true;
	        }
	    }
	}
	
	
}
