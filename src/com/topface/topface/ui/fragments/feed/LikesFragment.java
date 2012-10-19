package com.topface.topface.ui.fragments.feed;

import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;

import com.topface.topface.R;
import com.topface.topface.data.AbstractFeedItem;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedRequest;
import com.topface.topface.ui.adapters.FeedList;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.adapters.LikesListAdapter.OnMutualListener;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.SwipeGestureListener;

public class LikesFragment extends FeedFragment<FeedLike> {

	private RateController mRateController;		
	
	
	@Override
	protected void init() {
		mRateController = new RateController(getActivity());		
	}
	
    @Override
    protected int getTitle() {
        return R.string.dashbrd_btn_likes;
    }

    @Override
    protected LikesListAdapter getAdapter() {
    	LikesListAdapter adapter = new LikesListAdapter(getActivity(), getUpdaterCallback());
    	adapter.setOnMutualListener(new OnMutualListener() {
			
			@Override
			public void onMutual(int userId, int rate,int mutualId) {
				mRateController.onRate(userId, rate, mutualId);
			}
		});
        return adapter;
    }    
    
    @Override
    protected OnItemClickListener getOnItemClickListener() {    	
    	return null;
    }    
    
    @Override
    protected OnTouchListener getListViewOnTouchListener() {
    	final GestureDetector gd = new GestureDetector(getActivity().getApplicationContext(), 
    			new SwipeGestureListener(getActivity().getApplicationContext(),mListView.getRefreshableView(), 
	    			new SwipeGestureListener.SwipeListener() {
				
						@Override
						public void onSwipeR2L(int position) {
							((LikesListAdapter)mListAdapter).setSelectedForMutual(position);
						}
						
						@Override
						public void onSwipeL2R(int position) {
							((LikesListAdapter)mListAdapter).setSelectedForMutual(-1);							
						}

						@Override
						public void onTap(int position) {
							
							AbstractFeedItem item = (AbstractFeedItem) mListView.getRefreshableView().getItemAtPosition(position);
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
					})
    			);
    	
    	return new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gd.onTouchEvent(event);
			}
		};
    }
    
    @Override
    protected int getEmptyFeedText() {
        return R.string.likes_background_text;
    }

    @Override
    protected Drawable getBackIcon() {
        return getResources().getDrawable(R.drawable.likes_back_icon);
    }

    @Override
    protected FeedRequest.FeedService getFeedService() {
        return FeedRequest.FeedService.LIKES;
    }

    @Override
    protected FeedList<FeedLike> parseResponse(ApiResponse response) {
        return FeedLike.parse(response);
    }

}
