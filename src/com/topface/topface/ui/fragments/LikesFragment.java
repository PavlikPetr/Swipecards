package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.FeedLike;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.BannerRequest;
import com.topface.topface.requests.FeedLikesRequest;
import com.topface.topface.ui.ChatActivity;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class LikesFragment extends BaseFragment {
	// Data
	private boolean mNewUpdating;
	private PullToRefreshListView mListView;
	private LikesListAdapter mListAdapter;
	private AvatarManager<FeedLike> mAvatarManager;
	private DoubleBigButton mDoubleButton;
	private ProgressBar mProgressBar;
	private ImageView mBannerView;
	private boolean mIsUpdating = false;
	// Constants
	private static final int LIMIT = 40;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
	    super.onCreateView(inflater, container, saved);
	    
    View view = inflater.inflate(R.layout.ac_likes, null);
		
		
		// Data
		Data.likesList = new LinkedList<FeedLike>();

		// Progress
		mProgressBar = (ProgressBar) view.findViewById(R.id.prsLikesLoading);

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
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
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
								Intent intent = new Intent(getActivity(),	ProfileActivity.class);
								intent.putExtra(ProfileActivity.INTENT_USER_ID, Data.likesList.get(position).uid);
								intent.putExtra(ChatActivity.INTENT_USER_URL, Data.likesList.get(position).getSmallLink());
								intent.putExtra(ProfileActivity.INTENT_USER_NAME, Data.likesList.get(position).first_name);
								intent.putExtra(ProfileActivity.INTENT_MUTUAL_ID, Data.likesList.get(position).id);
								startActivity(intent);
							} catch (Exception e) {
								Debug.log(
										LikesFragment.this,
										"start ProfileActivity exception:"
												+ e.toString());
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

		// Control creating
		mAvatarManager = new AvatarManager<FeedLike>(getActivity(),
				Data.likesList, new Handler() {
					@Override
					public void handleMessage(Message msg) {
						if (Data.likesList.getLast().isLoader() && !mIsUpdating)
							updateDataHistory();

						super.handleMessage(msg);
					}
				});
		mListAdapter = new LikesListAdapter(getActivity(),
				mAvatarManager);
		mListView.setOnScrollListener(mAvatarManager);
		mListView.setAdapter(mListAdapter);

		mNewUpdating = CacheProfile.unread_likes > 0 ? true : false;
		CacheProfile.unread_likes = 0;
		return view;
	}

	private void updateData(boolean isPushUpdating) {
		mIsUpdating = true;
		if (!isPushUpdating)
			mProgressBar.setVisibility(View.VISIBLE);

		mDoubleButton.setChecked(mNewUpdating ? DoubleBigButton.RIGHT_BUTTON
				: DoubleBigButton.LEFT_BUTTON);

		FeedLikesRequest likesRequest = new FeedLikesRequest(getActivity());
		likesRequest.limit = LIMIT;
		likesRequest.only_new = mNewUpdating;
		likesRequest.callback(new ApiHandler() {
			@Override
			public void success(ApiResponse response) {
				Data.likesList.clear();
				Data.likesList.addAll(FeedLike.parse(response));
				updateUI(new Runnable() {
					@Override
					public void run() {
						if (mNewUpdating) {
							if (FeedLike.unread_count > 0) {
								Data.likesList.add(new FeedLike(
										IListLoader.ItemType.LOADER));
							}
						} else {
							if (!(Data.likesList.size() == 0 || Data.likesList
									.size() < LIMIT / 2)) {
								Data.likesList.add(new FeedLike(
										IListLoader.ItemType.LOADER));
							}
						}

						mProgressBar.setVisibility(View.GONE);
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
						mProgressBar.setVisibility(View.GONE);
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

		FeedLikesRequest likesRequest = new FeedLikesRequest(
				getActivity());
		likesRequest.limit = LIMIT;
		likesRequest.only_new = mNewUpdating;
		if (!mNewUpdating) {
			if (Data.likesList.getLast().isLoader()
					|| Data.likesList.getLast().isLoaderRetry()) {
				likesRequest.from = Data.likesList
						.get(Data.likesList.size() - 2).id;
			} else {
				likesRequest.from = Data.likesList
						.get(Data.likesList.size() - 1).id;
			}
		}
		likesRequest.callback(new ApiHandler() {
			@Override
			public void success(ApiResponse response) {
				final LinkedList<FeedLike> feedLikesList = FeedLike
						.parse(response);

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

						mProgressBar.setVisibility(View.GONE);
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
						mProgressBar.setVisibility(View.GONE);
						Toast.makeText(getActivity()  ,
								getString(R.string.general_data_error),
								Toast.LENGTH_SHORT).show();
						mIsUpdating = false;
						removeLoaderListItem();
						Data.likesList.add(new FeedLike(
								IListLoader.ItemType.RETRY));
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
	
    @Override
    public void clearLayout() {
        Debug.log(this, "LikesActivity::clearLayout");
        mListView.setVisibility(View.INVISIBLE);
    }


    @Override
    public void fillLayout() {
        Debug.log(this, "LikesActivity::fillLayout");

        updateBanner(mBannerView, BannerRequest.LIKE);
        updateData(false);
    }
}
