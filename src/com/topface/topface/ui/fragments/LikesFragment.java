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
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedLikesRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.adapters.IListLoader;
import com.topface.topface.ui.adapters.IListLoader.ItemType;
import com.topface.topface.ui.adapters.LikesListAdapter;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.views.DoubleBigButton;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.SwapAnimation;

import java.util.LinkedList;

public class LikesFragment extends BaseFragment {

	private PullToRefreshListView mListView;
	private LikesListAdapter mListAdapter;
	private AvatarManager<FeedLike> mAvatarManager;
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
	    
	    View view = inflater.inflate(R.layout.ac_likes, null);
		
		// Data
		Data.likesList = new LinkedList<FeedLike>();
		
        // Navigation Header
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity)getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getResources().getString(R.string.dashbrd_btn_likes));
		
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
		mBackgroundText = (TextView) view.findViewById(R.id.tvBackgroundText);
		
		// Banner
		mBannerView = (ImageView) view.findViewById(R.id.ivBanner);

		// Double Button
		mDoubleButton = (DoubleBigButton) view.findViewById(R.id.btnDoubleBig);
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
		mListView = (PullToRefreshListView) view.findViewById(R.id.lvLikesList);
		mListView.getRefreshableView().setOnItemClickListener(
				new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					    FeedLike theFeedLike = (FeedLike)parent.getItemAtPosition(position);
						if (!mIsUpdating && theFeedLike.isLoaderRetry()) {
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
								Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                                intent.putExtra(ChatActivity.INTENT_USER_URL,         theFeedLike.getSmallLink());
								intent.putExtra(UserProfileActivity.INTENT_USER_ID,   theFeedLike.uid);
								intent.putExtra(UserProfileActivity.INTENT_USER_NAME, theFeedLike.first_name);
								intent.putExtra(UserProfileActivity.INTENT_MUTUAL_ID, theFeedLike.id);
								startActivity(intent);
							} catch (Exception e) {
								Debug.log(
										LikesFragment.this,
										"start UserProfileActivity exception:"
												+ e.toString());
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
		mAvatarManager = new AvatarManager<FeedLike>(getActivity(),
				Data.likesList, new Handler() {
					@Override
					public void handleMessage(Message msg) {
					    if (Data.likesList.size() > 0)
						    if (Data.likesList.getLast().isLoader() && !mIsUpdating)
							    updateDataHistory();

						super.handleMessage(msg);
					}
				});
		mListAdapter = new LikesListAdapter(getActivity(), mAvatarManager);
		mListView.setOnScrollListener(mAvatarManager);
		mListView.getRefreshableView().setAdapter(mListAdapter);

		mNewUpdating = CacheProfile.unread_likes > 0;
		
		updateData(false);

		return view;
	}

	private void updateData(final boolean isPushUpdating) {
		mIsUpdating = true;
		if (!isPushUpdating)
			onUpdateStart(isPushUpdating);

		mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON : DoubleBigButton.LEFT_BUTTON);

		FeedLikesRequest likesRequest = new FeedLikesRequest(getActivity().getApplicationContext());
		registerRequest(likesRequest);
		likesRequest.limit = LIMIT;
		likesRequest.only_new = mNewUpdating;
		likesRequest.callback(new ApiHandler() {
			@Override
			public void success(ApiResponse response) {
			    final LinkedList<FeedLike> feedLikesList = FeedLike.parse(response);
				updateUI(new Runnable() {
					@Override
					public void run() {
			            Data.likesList.clear();
			            Data.likesList.addAll(feedLikesList);
					    CacheProfile.unread_likes = 0;
						if (mNewUpdating) {
							if (FeedLike.unread_count > 0) {
								Data.likesList.add(new FeedLike(IListLoader.ItemType.LOADER));
							}
						} else {
							if (!(Data.likesList.size() == 0 || Data.likesList
									.size() < LIMIT / 2)) {
								Data.likesList.add(new FeedLike(IListLoader.ItemType.LOADER));
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
			public void fail(int codeError, ApiResponse response) {
				updateUI(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(),
								getString(R.string.general_data_error),
								Toast.LENGTH_SHORT).show();
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

		FeedLikesRequest likesRequest = new FeedLikesRequest(getActivity());
		registerRequest(likesRequest);
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
									Data.likesList.add(new FeedLike(
											IListLoader.ItemType.LOADER));
							} else {
								if (!(Data.likesList.size() == 0 || Data.likesList
										.size() < (LIMIT / 2)))
									Data.likesList.add(new FeedLike(
											IListLoader.ItemType.LOADER));
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
			public void fail(int codeError, ApiResponse response) {
				updateUI(new Runnable() {
					@Override
					public void run() {
						onUpdateFail(true);
						Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
						mIsUpdating = false;
						removeLoaderListItem();
						Data.likesList.add(new FeedLike(IListLoader.ItemType.RETRY));
						mListView.onRefreshComplete();
						mListAdapter.notifyDataSetChanged();
					}
				});
			}
		}).exec();
	}
	
    private void removeLoaderListItem() {
        if (Data.likesList.size() > 0) {
            if (Data.likesList.getLast().isLoader()
                    || Data.likesList.getLast().isLoaderRetry()) {
                Data.likesList.remove(Data.likesList.size() - 1);
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

		if (mAvatarManager != null)
			mAvatarManager.release();
		mAvatarManager = null;
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
			if (Data.likesList.isEmpty()) {
				mBackgroundText.setText(R.string.likes_background_text);
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
