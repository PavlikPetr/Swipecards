package com.topface.topface.ui.frames;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedLikesRequest;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.p2r.PullToRefreshBase.OnRefreshListener;
import com.topface.topface.ui.p2r.PullToRefreshListView;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class LikesActivity extends FrameActivity {
    // Data
    private boolean mNewUpdating;
//    private TextView mFooterView;
//    private int mFooterHeight;
    private PullToRefreshListView mListView;
    private LikesListAdapter mListAdapter;
    private AvatarManager<FeedLike> mAvatarManager;
    private DoubleBigButton mDoubleButton;
    private ProgressBar mProgressBar;
    private ImageView mBannerView;
    private boolean mIsUpdating = false;
    // Constants
    private static final int LIMIT = 44;
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_likes);
        Debug.log(this, "+onCreate");

        // Data
        Data.likesList = new LinkedList<FeedLike>();

        // Progress
        mProgressBar = (ProgressBar)findViewById(R.id.prsLikesLoading);

        // Banner
        mBannerView = (ImageView)findViewById(R.id.ivBanner);

        // Double Button
        mDoubleButton = (DoubleBigButton)findViewById(R.id.btnDoubleBig);
        mDoubleButton.setLeftText(getString(R.string.likes_btn_dbl_left));
        mDoubleButton.setRightText(getString(R.string.likes_btn_dbl_right));
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
        mListView = (PullToRefreshListView)findViewById(R.id.lvLikesList);
        mListView.getRefreshableView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,View view,int position,long id) {            	
            	if (!mIsUpdating && Data.likesList.get(position).isLoaderRetry()) {
            		updateUI(new Runnable() {
						public void run() {
							removeLoaderListItem();
							Data.likesList.add(new FeedLike(ItemType.LOADER));
							mListAdapter.notifyDataSetChanged();
						}
					});
            		
            		updateDataHistory();
            	} else {
	                try {
	                    // Open profile activity
	                    Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
	                    intent.putExtra(ProfileActivity.INTENT_USER_ID, Data.likesList.get(position).uid);
	                    intent.putExtra(ProfileActivity.INTENT_USER_NAME, Data.likesList.get(position).first_name);
	                    intent.putExtra(ProfileActivity.INTENT_MUTUAL_ID, Data.likesList.get(position).id);
	                    startActivity(intent);
	                } catch(Exception e) {
	                    Debug.log(LikesActivity.this, "start ProfileActivity exception:" + e.toString());
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
//        mFooterHeight = getResources().getDrawable(R.drawable.item_list_selector).getMinimumHeight();
//        onLikeFeedsExausted();
//        mListView.getRefreshableView().addFooterView(mFooterView);

        // Control creating
        mAvatarManager = new AvatarManager<FeedLike>(getApplicationContext(), Data.likesList, new LikesLoadingHandler());
        mListAdapter = new LikesListAdapter(getApplicationContext(), mAvatarManager);
        mListView.setOnScrollListener(mAvatarManager);
        mListView.setAdapter(mListAdapter);

        mNewUpdating = CacheProfile.unread_likes > 0 ? true : false;
        CacheProfile.unread_likes = 0;
    }
    //---------------------------------------------------------------------------
    private void updateData(boolean isPushUpdating) {
    	mIsUpdating = true;
        if (!isPushUpdating)
            mProgressBar.setVisibility(View.VISIBLE);

        mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);

        FeedLikesRequest likesRequest = new FeedLikesRequest(getApplicationContext());
        likesRequest.limit = LIMIT;
        likesRequest.only_new = mNewUpdating;
        likesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Data.likesList.clear();
                Data.likesList.addAll(FeedLike.parse(response));
                if (mNewUpdating) {
            		if (FeedLike.unread_count > 0) {
            			Data.likesList.add(new FeedLike(IListLoader.ItemType.LOADER));
            		}
            	} else {
            		if (!(Data.likesList.size() == 0 || Data.likesList.size() < LIMIT / 2)) {
            			Data.likesList.add(new FeedLike(IListLoader.ItemType.LOADER));
            		}
            	}
                updateUI(new Runnable() {
                    @Override
                    public void run() {
//                        if (mNewUpdating) {
//                            mFooterView.setVisibility(View.GONE);
//                            onLikeFeedsExausted();
//                        } else {
//                            mFooterView.setVisibility(View.VISIBLE);
//                            onLikeFeedsExists();
//                        }
//                        
//                        if (Data.likesList.size() == 0 || Data.likesList.size() < LIMIT / 2) {
//                            mFooterView.setVisibility(View.GONE);
//                            onLikeFeedsExausted();
//                        }
                    	
                        mProgressBar.setVisibility(View.GONE);                        
                        mListAdapter.notifyDataSetChanged();
                        mListView.onRefreshComplete();
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
                        Toast.makeText(LikesActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                        mListView.onRefreshComplete();
                        mListView.setVisibility(View.VISIBLE);
                        mIsUpdating = false;
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void updateDataHistory() {
    	mIsUpdating = true;
    	mNewUpdating = mDoubleButton.isRightButtonChecked();    	
    	
//        mProgressBar.setVisibility(View.VISIBLE);
        FeedLikesRequest likesRequest = new FeedLikesRequest(getApplicationContext());
        likesRequest.limit = LIMIT;
        likesRequest.only_new = mNewUpdating;
        if (!mNewUpdating) {
        	if (Data.likesList.getLast().isLoader() || Data.likesList.getLast().isLoaderRetry()) {
        		likesRequest.from = Data.likesList.get(Data.likesList.size() - 2).id;
        	} else {
        		likesRequest.from = Data.likesList.get(Data.likesList.size() - 1).id;
        	}        	
        }
        likesRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                final LinkedList<FeedLike> feedLikesList = FeedLike.parse(response);                                
                
                updateUI(new Runnable() {
                    @Override
                    public void run() {                    	
                    	removeLoaderListItem();

                    	if (feedLikesList.size() > 0) {
                            Data.likesList.addAll(feedLikesList);
                            if (mNewUpdating) {
                        		if (FeedLike.unread_count > 0)
                        			Data.likesList.add(new FeedLike(IListLoader.ItemType.LOADER));
                        	} else {
                        		if (!(Data.likesList.size() == 0 || Data.likesList.size() < (LIMIT/2)))
                        			Data.likesList.add(new FeedLike(IListLoader.ItemType.LOADER));
                        	}
                        }
                    	
                        mProgressBar.setVisibility(View.GONE);
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
//                        mFooterView.setVisibility(View.GONE);
//                    	onLikeFeedsExausted();
                    	mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(LikesActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();                        
                        mIsUpdating = false;
                    	updateUI(new Runnable() {
                            @Override
                            public void run() {
                            	removeLoaderListItem();
                            	Data.likesList.add(new FeedLike(IListLoader.ItemType.RETRY));
                            	mListView.onRefreshComplete();
                                mListAdapter.notifyDataSetChanged();
                            }
                    	});                    	
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    // FrameActivity
    //---------------------------------------------------------------------------
    @Override
    public void clearLayout() {
        Debug.log(this, "LikesActivity::clearLayout");
        mListView.setVisibility(View.INVISIBLE);
    }
    //---------------------------------------------------------------------------
    @Override
    public void fillLayout() {
        Debug.log(this, "LikesActivity::fillLayout");

        updateBanner(mBannerView, BannerRequest.LIKE);
        updateData(false);
    }
    //---------------------------------------------------------------------------
//    private void onLikeFeedsExausted() {
//    	mFooterView.setBackgroundResource(android.R.color.transparent);
//    	mFooterView.setText(Static.EMPTY);
//        mFooterView.setHeight(mFooterHeight);
//        mFooterView.setEnabled(false);
//    }
    //---------------------------------------------------------------------------
//    private void onLikeFeedsExists() {        
//        mFooterView.setBackgroundResource(R.drawable.item_list_selector);
//        mFooterView.setText(getString(R.string.general_footer_previous));        
//        mFooterView.setEnabled(true);
//    }
    //---------------------------------------------------------------------------
    @Override
    public void release() {
        mListView = null;

        if (mListAdapter != null)
            mListAdapter.release();
        mListAdapter = null;

        if (mAvatarManager != null)
            mAvatarManager.release();
        mAvatarManager = null;
    }
    //---------------------------------------------------------------------------
    private void removeLoaderListItem() {
    	if (Data.likesList.getLast().isLoader() || Data.likesList.getLast().isLoaderRetry()) {
    		Data.likesList.remove(Data.likesList.size() - 1);
    	}
    }
    //---------------------------------------------------------------------------
    class LikesLoadingHandler extends Handler {
    	@Override
    	public void handleMessage(Message msg) {
    		if (Data.likesList.getLast().isLoader() && !mIsUpdating)
    			updateDataHistory();
    		
    		super.handleMessage(msg);
    	}
    }
    //---------------------------------------------------------------------------
}
